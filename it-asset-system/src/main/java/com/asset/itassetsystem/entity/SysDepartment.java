package com.asset.itassetsystem.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 部门实体类
 */
@Data
@TableName("sys_department")
public class SysDepartment {
    @TableId(type = IdType.AUTO)
    private Long deptId;
    
    private String site;
    private String deptName;
    private Long parentId;
    private String deptCode;
    private String manager;
    private String phone;
    private Integer sortOrder;
    private Integer status;
    private LocalDateTime createTime;
}
