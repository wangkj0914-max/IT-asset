package com.asset.itassetsystem.mapper;

import com.asset.itassetsystem.entity.AssetInfo;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

// 移除@Mapper注解，由启动类的@MapperScan统一扫描
public interface AssetInfoMapper extends BaseMapper<AssetInfo> {
}