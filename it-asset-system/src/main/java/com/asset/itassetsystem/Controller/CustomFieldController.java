package com.asset.itassetsystem.controller;

import com.asset.itassetsystem.common.Result;
import com.asset.itassetsystem.entity.SysCustomFieldDef;
import com.asset.itassetsystem.entity.SysCustomFieldValue;
import com.asset.itassetsystem.mapper.SysCustomFieldDefMapper;
import com.asset.itassetsystem.mapper.SysCustomFieldValueMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/custom-field")
public class CustomFieldController {

    @Autowired
    private SysCustomFieldDefMapper fieldDefMapper;

    @Autowired
    private SysCustomFieldValueMapper fieldValueMapper;

    private String getSite(HttpServletRequest request) {
        String site = request.getHeader("X-Site");
        if (site != null) {
            try {
                site = URLDecoder.decode(site, StandardCharsets.UTF_8.name());
            } catch (Exception ignored) {
            }
        }
        return site != null ? site : "苏州";
    }

    /**
     * 字段定义列表
     */
    @GetMapping("/def-list")
    public Result<List<SysCustomFieldDef>> defList(HttpServletRequest request,
                                                   @RequestParam(required = false) String targetEntity) {
        String site = getSite(request);
        LambdaQueryWrapper<SysCustomFieldDef> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysCustomFieldDef::getSite, site);
        wrapper.orderByAsc(SysCustomFieldDef::getSortOrder);
        return Result.success(fieldDefMapper.selectList(wrapper));
    }

    /**
     * 新增字段定义
     */
    @PostMapping("/def-save")
    public Result<String> defSave(@RequestBody SysCustomFieldDef fieldDef,
                                  HttpServletRequest request) {
        fieldDef.setSite(getSite(request));
        fieldDef.setCreateTime(LocalDateTime.now());
        fieldDefMapper.insert(fieldDef);
        return Result.success("字段定义保存成功");
    }

    /**
     * 删除字段定义（级联删除values）
     */
    @PostMapping("/def-delete")
    public Result<String> defDelete(@RequestParam Long fieldId) {
        // 级联删除所有关联的字段值
        LambdaQueryWrapper<SysCustomFieldValue> valWrapper = new LambdaQueryWrapper<>();
        valWrapper.eq(SysCustomFieldValue::getFieldId, fieldId);
        fieldValueMapper.delete(valWrapper);

        // 删除定义本身
        fieldDefMapper.deleteById(fieldId);
        return Result.success("字段定义已删除");
    }

    /**
     * 获取某资产的字段值列表
     */
    @GetMapping("/values")
    public Result<List<Map<String, Object>>> values(@RequestParam String entityType,
                                                     @RequestParam Long entityId) {
        // 查询所有字段定义（不考虑站点过滤，资产编辑时展示所有字段）
        List<SysCustomFieldDef> defs = fieldDefMapper.selectList(null);

        // 查询该实体的已有值
        LambdaQueryWrapper<SysCustomFieldValue> valWrapper = new LambdaQueryWrapper<>();
        valWrapper.eq(SysCustomFieldValue::getEntityType, entityType);
        valWrapper.eq(SysCustomFieldValue::getEntityId, entityId);
        Map<Long, String> valMap = fieldValueMapper.selectList(valWrapper).stream()
                .collect(Collectors.toMap(SysCustomFieldValue::getFieldId, SysCustomFieldValue::getFieldValue, (a, b) -> b));

        // 组装返回
        List<Map<String, Object>> result = new ArrayList<>();
        for (SysCustomFieldDef def : defs) {
            Map<String, Object> map = new HashMap<>();
            map.put("fieldId", def.getFieldId());
            map.put("fieldName", def.getFieldName());
            map.put("fieldType", def.getFieldType());
            map.put("fieldOptions", def.getFieldOptions());
            map.put("isRequired", def.getIsRequired());
            map.put("fieldValue", valMap.getOrDefault(def.getFieldId(), ""));
            result.add(map);
        }
        return Result.success(result);
    }

    /**
     * 批量保存字段值（按 entityType+entityId 先删旧的再批量插入）
     */
    @PostMapping("/save-values")
    public Result<String> saveValues(@RequestBody List<Map<String, Object>> values) {
        if (values == null || values.isEmpty()) {
            return Result.success("保存成功");
        }

        // 获取 entityType 和 entityId（取第一条的）
        String entityType = String.valueOf(values.get(0).get("entityType"));
        Long entityId = Long.valueOf(String.valueOf(values.get(0).get("entityId")));

        // 删除该实体的旧值
        LambdaQueryWrapper<SysCustomFieldValue> delWrapper = new LambdaQueryWrapper<>();
        delWrapper.eq(SysCustomFieldValue::getEntityType, entityType);
        delWrapper.eq(SysCustomFieldValue::getEntityId, entityId);
        fieldValueMapper.delete(delWrapper);

        // 批量插入新值
        for (Map<String, Object> val : values) {
            SysCustomFieldValue v = new SysCustomFieldValue();
            v.setEntityType(entityType);
            v.setEntityId(entityId);
            v.setFieldId(Long.valueOf(String.valueOf(val.get("fieldId"))));
            v.setFieldValue(String.valueOf(val.getOrDefault("fieldValue", "")));
            fieldValueMapper.insert(v);
        }

        return Result.success("保存成功");
    }
}
