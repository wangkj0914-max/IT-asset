package com.asset.itassetsystem.service.impl;

import com.asset.itassetsystem.dto.ScrapApplyDTO;
import com.asset.itassetsystem.entity.AssetInfo;
import com.asset.itassetsystem.entity.AssetScrapRecord;
import com.asset.itassetsystem.mapper.AssetScrapRecordMapper;
import com.asset.itassetsystem.service.AssetInfoService;
import com.asset.itassetsystem.service.AssetScrapService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 资产报废服务实现类
 */
@Service
public class AssetScrapServiceImpl extends ServiceImpl<AssetScrapRecordMapper, AssetScrapRecord> implements AssetScrapService {

    @Autowired
    private AssetInfoService assetInfoService;

    @Autowired
    private com.asset.itassetsystem.mapper.SysUserMapper sysUserMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean apply(ScrapApplyDTO dto, String operator) {
        AssetInfo asset = assetInfoService.getById(dto.getAssetId());
        if (asset == null) {
            throw new RuntimeException("资产不存在");
        }

        AssetScrapRecord record = new AssetScrapRecord();
        record.setAssetId(dto.getAssetId());
        record.setAssetCode(asset.getAssetCode());  // 冗余资产编号
        record.setAssetName(asset.getAssetName());  // 冗余资产名称
        record.setScrapReason(dto.getScrapReason());
        record.setScrapType(dto.getScrapType());
        record.setOriginalPrice(dto.getOriginalPrice());
        record.setResidualValue(dto.getResidualValue());
        record.setApplyUserName(operator);  // ��请人
        record.setApplyDepartment(getUserDepartment(operator));
        record.setApproveStatus(0); // 待审批
        record.setRemark(dto.getRemark());
        
        boolean saved = save(record);
        
        if (saved && "admin".equals(operator)) {
            // 管理员申请直接通过
            approve(record.getScrapId(), true, operator);
        }
        
        return saved;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean approve(Long scrapId, boolean approved, String approver) {
        AssetScrapRecord record = getById(scrapId);
        if (record == null) {
            throw new RuntimeException("报废记录不存在");
        }

        record.setApproveUser(approver);
        record.setApproveStatus(approved ? 1 : 2);
        record.setApproveTime(LocalDateTime.now());
        
        boolean updated = updateById(record);
        
        if (updated && approved) {
            // 更新资产状态为已报废
            AssetInfo asset = assetInfoService.getById(record.getAssetId());
            if (asset != null) {
                asset.setStatus(3); // 已报废
                assetInfoService.updateById(asset);
            }
        }
        
        return updated;
    }

    @Override
    public List<AssetScrapRecord> listByAssetId(Long assetId) {
        return lambdaQuery()
            .eq(AssetScrapRecord::getAssetId, assetId)
            .orderByDesc(AssetScrapRecord::getCreateTime)
            .list();
    }

    @Override
    public List<AssetScrapRecord> listPending() {
        return lambdaQuery()
            .eq(AssetScrapRecord::getApproveStatus, 0)
            .orderByDesc(AssetScrapRecord::getCreateTime)
            .list();
    }

    @Override
    public Object listPage(Integer pageNum, Integer pageSize, Integer scrapType, Integer approveStatus) {
        Page<AssetScrapRecord> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<AssetScrapRecord> wrapper = new LambdaQueryWrapper<>();
        
        if (scrapType != null) {
            wrapper.eq(AssetScrapRecord::getScrapType, scrapType);
        }
        if (approveStatus != null) {
            wrapper.eq(AssetScrapRecord::getApproveStatus, approveStatus);
        }
        
        wrapper.orderByDesc(AssetScrapRecord::getCreateTime);
        
        Page<AssetScrapRecord> resultPage = page(page, wrapper);
        
        return new PageResult<>(
            resultPage.getRecords(),
            resultPage.getTotal(),
            resultPage.getCurrent(),
            resultPage.getSize()
        );
    }

    @Data
    public static class PageResult<T> {
        private List<T> records;
        private Long total;
        private Long current;
        private Long size;

        public PageResult(List<T> records, Long total, Long current, Long size) {
            this.records = records;
            this.total = total;
            this.current = current;
            this.size = size;
        }
    }

    /**
     * 从用户表获取用户的部门
     */
    private String getUserDepartment(String username) {
        if (username == null || username.isEmpty()) return "";
        com.asset.itassetsystem.entity.SysUser user = sysUserMapper.selectOne(
            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<com.asset.itassetsystem.entity.SysUser>()
                .eq(com.asset.itassetsystem.entity.SysUser::getUsername, username)
        );
        return user != null && user.getDepartment() != null ? user.getDepartment() : "";
    }
}
