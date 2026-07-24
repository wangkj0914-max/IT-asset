package com.asset.itassetsystem.service;

import com.asset.itassetsystem.entity.Consumable;
import com.asset.itassetsystem.entity.ConsumableRecord;
import com.baomidou.mybatisplus.extension.service.IService;

public interface ConsumableService extends IService<Consumable> {
    void stockIn(Long id, int qty, String operator);
    void stockOut(Long id, int qty, String operator);
    java.util.List<ConsumableRecord> getRecords(Long consumableId);
}
