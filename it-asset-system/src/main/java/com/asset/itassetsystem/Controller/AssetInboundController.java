package com.asset.itassetsystem.controller;

import com.asset.itassetsystem.common.CommonUtil;
import com.asset.itassetsystem.common.Result;
import com.asset.itassetsystem.entity.AssetInbound;
import com.asset.itassetsystem.service.AssetInboundService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * 资产入库控制器
 */
@RestController
@RequestMapping("/inbound")
public class AssetInboundController {

    @Autowired
    private AssetInboundService assetInboundService;
    @Autowired
    private HttpServletRequest httpRequest;

    private String getCurrentUser() {
        Object u = httpRequest.getAttribute("username");
        return u != null ? u.toString() : "system";
    }

    /**
     * 分页查询入库记录
     *
     * <p>站点条件（{@code site}）下推到 SQL 查询条件中（{@code LambdaQueryWrapper.eq}），
     * 保证分页 total 与记录一致，避免「先分页再内存过滤」导致的总数错乱与跨页记录丢失。
     */
    @GetMapping({"/list-all", "/page"})
    public Result<Map<String, Object>> listAll(
            @RequestParam(required = false) String assetName,
            @RequestParam(required = false) String inboundNo,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String site,
            @RequestParam(defaultValue = "1") Long current,
            @RequestParam(defaultValue = "10") Long size) {

        var page = assetInboundService.pageInbounds(current, size, assetName, inboundNo, status, site);
        return Result.success(CommonUtil.buildPageResult(page));
    }

    /**
     * 提交入库申请
     */
    @PostMapping("/apply")
    public Result<String> apply(@RequestBody AssetInbound inbound) {
        try {
            // 显式解析站点：body -> query 参数 -> token 站点 attribute -> 均空则报错。
            // 绝不允许站点为空继续落库（否则会被 DB 列默认值静默兜底到错误站点）。
            String site = resolveSite(inbound);
            if (!StringUtils.hasText(site)) {
                return Result.fail("未指定站点，无法入库");
            }
            inbound.setSite(site.trim());
            assetInboundService.apply(inbound, getCurrentUser());
            return Result.success("入库申请提交成功");
        } catch (Exception e) {
            return Result.fail(e.getMessage());
        }
    }

    /**
     * 解析入库记录所属站点，按以下优先级依次取值，返回第一个非空值：
     * <ol>
     *   <li>{@code inbound.getSite()} —— 请求体中的站点。
     *       管理员（role=2）为前端当前选择值；非管理员已被
     *       {@code SiteOverrideRequestBodyAdvice} 强制覆盖为 token 站点；</li>
     *   <li>{@code httpRequest.getParameter("site")} —— query 参数。
     *       非管理员已被 {@code SiteAwareRequestWrapper} 强制为 token 站点；</li>
     *   <li>{@code httpRequest.getAttribute("site")} —— {@code JwtInterceptor}
     *       写入的 token 站点，作为最后兜底。</li>
     * </ol>
     * 三者均为空时返回 {@code null}，由调用方返回明确错误，绝不静默落某个站点。
     *
     * @param inbound 请求体反序列化后的入库记录（可为 null）
     * @return 解析出的站点（已 trim），无法解析时返回 {@code null}
     */
    private String resolveSite(AssetInbound inbound) {
        // 1) 请求体
        if (inbound != null && StringUtils.hasText(inbound.getSite())) {
            return inbound.getSite().trim();
        }
        // 2) query 参数（可能被 SiteAwareRequestWrapper 改写为 token 站点）
        String paramSite = httpRequest.getParameter("site");
        if (StringUtils.hasText(paramSite)) {
            return paramSite.trim();
        }
        // 3) token 站点 attribute（JwtInterceptor 写入）
        Object attrSite = httpRequest.getAttribute("site");
        if (attrSite != null && StringUtils.hasText(attrSite.toString())) {
            return attrSite.toString().trim();
        }
        return null;
    }

    /**
     * 审核入库
     */
    @PostMapping("/audit")
    public Result<String> audit(
            @RequestParam Long inboundId,
            @RequestParam Boolean approved) {
        try {
            assetInboundService.audit(inboundId, approved, getCurrentUser());
            return Result.success(approved ? "审核通过，资产已入库" : "已拒绝");
        } catch (Exception e) {
            return Result.fail(e.getMessage());
        }
    }
}
