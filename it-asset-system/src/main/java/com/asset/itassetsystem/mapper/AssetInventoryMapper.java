package com.asset.itassetsystem.mapper;

import com.asset.itassetsystem.entity.AssetInventory;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 资产盘点 Mapper
 */
@Mapper
public interface AssetInventoryMapper extends BaseMapper<AssetInventory> {
}
