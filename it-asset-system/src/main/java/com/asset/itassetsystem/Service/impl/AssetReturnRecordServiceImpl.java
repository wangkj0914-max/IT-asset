package com.asset.itassetsystem.service.impl;

import com.asset.itassetsystem.entity.AssetReturnRecord;
import com.asset.itassetsystem.mapper.AssetReturnRecordMapper;
import com.asset.itassetsystem.service.AssetReturnRecordService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

@Service
public class AssetReturnRecordServiceImpl extends ServiceImpl<AssetReturnRecordMapper, AssetReturnRecord> implements AssetReturnRecordService {}
