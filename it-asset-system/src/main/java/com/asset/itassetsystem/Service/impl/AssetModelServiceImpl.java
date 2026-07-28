package com.asset.itassetsystem.service.impl;

import com.asset.itassetsystem.entity.AssetModel;
import com.asset.itassetsystem.mapper.AssetModelMapper;
import com.asset.itassetsystem.service.AssetModelService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

@Service
public class AssetModelServiceImpl extends ServiceImpl<AssetModelMapper, AssetModel> implements AssetModelService {
}
