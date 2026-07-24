package com.asset.itassetsystem.service;

import com.asset.itassetsystem.entity.License;
import com.baomidou.mybatisplus.extension.service.IService;
import java.util.List;

public interface LicenseService extends IService<License> {
    List<License> getExpiring(int days, String site);
}
