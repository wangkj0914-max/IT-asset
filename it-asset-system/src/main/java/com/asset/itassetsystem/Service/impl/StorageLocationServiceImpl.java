package com.asset.itassetsystem.service.impl;

import com.asset.itassetsystem.entity.StorageLocation;
import com.asset.itassetsystem.mapper.StorageLocationMapper;
import com.asset.itassetsystem.service.StorageLocationService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

@Service
public class StorageLocationServiceImpl extends ServiceImpl<StorageLocationMapper, StorageLocation> implements StorageLocationService {
}
