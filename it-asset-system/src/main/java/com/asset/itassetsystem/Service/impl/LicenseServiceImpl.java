package com.asset.itassetsystem.service.impl;

import com.asset.itassetsystem.entity.License;
import com.asset.itassetsystem.mapper.LicenseMapper;
import com.asset.itassetsystem.service.LicenseService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.util.List;

@Service
public class LicenseServiceImpl extends ServiceImpl<LicenseMapper, License> implements LicenseService {
    @Override
    public List<License> getExpiring(int days, String site) {
        LocalDate threshold = LocalDate.now().plusDays(days);
        var w = new LambdaQueryWrapper<License>()
            .le(License::getExpireDate, threshold).ge(License::getExpireDate, LocalDate.now());
        if (site != null && !site.isEmpty()) w.eq(License::getSite, site);
        return list(w);
    }
}
