package com.asset.itassetsystem.mapper;

import com.asset.itassetsystem.entity.SysDepartment;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import java.util.List;

/**
 * 部门 Mapper 接口
 */
@Mapper
public interface SysDepartmentMapper extends BaseMapper<SysDepartment> {
    
    /**
     * 查询所有部门
     */
    List<SysDepartment> selectAll();
}
