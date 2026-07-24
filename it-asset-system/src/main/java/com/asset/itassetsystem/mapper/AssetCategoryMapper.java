package com.asset.itassetsystem.mapper;

import com.asset.itassetsystem.entity.AssetCategory;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import java.util.List;

/**
 * 资产分类 Mapper 接口
 */
@Mapper
public interface AssetCategoryMapper extends BaseMapper<AssetCategory> {
    
    /**
     * 查询所有一级分类
     */
    List<AssetCategory> selectParentCategories();
}
