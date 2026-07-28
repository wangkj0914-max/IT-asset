package com.asset.itassetsystem.service.impl;

import com.asset.itassetsystem.entity.StatusLabel;
import com.asset.itassetsystem.mapper.StatusLabelMapper;
import com.asset.itassetsystem.service.StatusLabelService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

@Service
public class StatusLabelServiceImpl extends ServiceImpl<StatusLabelMapper, StatusLabel> implements StatusLabelService {
}
