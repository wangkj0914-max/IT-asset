package com.asset.itassetsystem.service.impl;

import com.asset.itassetsystem.entity.ConsumableUseRecord;
import com.asset.itassetsystem.mapper.ConsumableUseRecordMapper;
import com.asset.itassetsystem.service.ConsumableUseRecordService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

@Service
public class ConsumableUseRecordServiceImpl extends ServiceImpl<ConsumableUseRecordMapper, ConsumableUseRecord> implements ConsumableUseRecordService {}
