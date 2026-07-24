package com.asset.itassetsystem.service.impl;

import com.asset.itassetsystem.entity.AssetCategory;
import com.asset.itassetsystem.mapper.AssetCategoryMapper;
import com.asset.itassetsystem.service.AssetCategoryService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import java.util.List;

/**
 * 资产分类服务实现类
 */
@Service
public class AssetCategoryServiceImpl extends ServiceImpl<AssetCategoryMapper, AssetCategory> implements AssetCategoryService {

    @Override
    public List<AssetCategory> listAll(String site) {
        LambdaQueryWrapper<AssetCategory> wrapper = new LambdaQueryWrapper<>();
        if (site != null && !site.isEmpty()) {
            wrapper.eq(AssetCategory::getSite, site);
        }
        wrapper.orderByAsc(AssetCategory::getSortOrder, AssetCategory::getCategoryId);
        return list(wrapper);
    }

    @Override
    public List<AssetCategory> listParentCategories() {
        LambdaQueryWrapper<AssetCategory> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AssetCategory::getParentId, 0L);
        wrapper.orderByAsc(AssetCategory::getSortOrder);
        return list(wrapper);
    }
}
