package com.asset.itassetsystem.service.impl;

import com.asset.itassetsystem.entity.SysOperationLog;
import com.asset.itassetsystem.mapper.SysOperationLogMapper;
import com.asset.itassetsystem.service.SysOperationLogService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

@Service
public class SysOperationLogServiceImpl extends ServiceImpl<SysOperationLogMapper, SysOperationLog> implements SysOperationLogService {
}
