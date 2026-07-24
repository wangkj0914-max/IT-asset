package com.asset.itassetsystem.service.impl;

import com.asset.itassetsystem.entity.AssetInfo;
import com.asset.itassetsystem.mapper.AssetInfoMapper;
import com.asset.itassetsystem.service.AssetInfoService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

@Service // 标记为Spring业务组件
// 继承ServiceImpl<Mapper, Entity>，实现IService接口
public class AssetInfoServiceImpl extends ServiceImpl<AssetInfoMapper, AssetInfo> implements AssetInfoService {
    // 基础版无需自定义方法，直接使用父类的save/remove/update/list等方法
}