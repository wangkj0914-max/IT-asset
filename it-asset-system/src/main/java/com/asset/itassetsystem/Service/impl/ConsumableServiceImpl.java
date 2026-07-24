package com.asset.itassetsystem.service.impl;

import com.asset.itassetsystem.entity.Consumable;
import com.asset.itassetsystem.entity.ConsumableRecord;
import com.asset.itassetsystem.mapper.ConsumableMapper;
import com.asset.itassetsystem.mapper.ConsumableRecordMapper;
import com.asset.itassetsystem.service.ConsumableService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ConsumableServiceImpl extends ServiceImpl<ConsumableMapper, Consumable> implements ConsumableService {

    @Autowired private ConsumableRecordMapper recordMapper;

    @Override @Transactional
    public void stockIn(Long id, int qty, String operator) {
        Consumable c = getById(id);
        if (c == null) throw new RuntimeException("耗材不存在");
        c.setCurrentStock(c.getCurrentStock() + qty);
        updateById(c);
        ConsumableRecord r = new ConsumableRecord();
        r.setConsumableId(id); r.setType(1); r.setQuantity(qty); r.setOperatorName(operator);
        recordMapper.insert(r);
    }

    @Override @Transactional
    public void stockOut(Long id, int qty, String operator) {
        Consumable c = getById(id);
        if (c == null) throw new RuntimeException("耗材不存在");
        if (c.getCurrentStock() < qty) throw new RuntimeException("库存不足，当前库存: " + c.getCurrentStock());
        c.setCurrentStock(c.getCurrentStock() - qty);
        updateById(c);
        ConsumableRecord r = new ConsumableRecord();
        r.setConsumableId(id); r.setType(2); r.setQuantity(qty); r.setOperatorName(operator);
        recordMapper.insert(r);
    }

    @Override
    public List<ConsumableRecord> getRecords(Long consumableId) {
        return recordMapper.selectList(new LambdaQueryWrapper<ConsumableRecord>()
            .eq(ConsumableRecord::getConsumableId, consumableId).orderByDesc(ConsumableRecord::getCreateTime));
    }
}
