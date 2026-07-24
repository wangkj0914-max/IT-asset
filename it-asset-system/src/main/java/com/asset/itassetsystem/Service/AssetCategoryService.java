package com.asset.itassetsystem.service;

import com.asset.itassetsystem.entity.AssetCategory;
import com.baomidou.mybatisplus.extension.service.IService;
import java.util.List;

/**
 * 资产分类服务接口
 */
public interface AssetCategoryService extends IService<AssetCategory> {
    
    /**
     * 查询所有分类（树形结构）
     */
    List<AssetCategory> listAll(String site);
    
    /**
     * 查询一级分类
     */
    List<AssetCategory> listParentCategories();
}
