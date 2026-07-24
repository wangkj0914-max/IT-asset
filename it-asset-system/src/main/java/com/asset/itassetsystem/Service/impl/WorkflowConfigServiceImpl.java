package com.asset.itassetsystem.service.impl;

import com.asset.itassetsystem.entity.WorkflowConfig;
import com.asset.itassetsystem.mapper.WorkflowConfigMapper;
import com.asset.itassetsystem.service.WorkflowConfigService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

@Service
public class WorkflowConfigServiceImpl extends ServiceImpl<WorkflowConfigMapper, WorkflowConfig> implements WorkflowConfigService {}
