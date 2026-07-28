package com.asset.itassetsystem.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 状态标签实体类
 * 可部署/已部署/不可部署/已归档
 */
@Data
@TableName("status_label")
public class StatusLabel {
    @TableId(type = IdType.AUTO)
    private Long statusLabelId;

    private String statusName;    // 状态名称
    private Integer statusType;   // 状态类型: 0=可部署, 1=已部署, 2=不可部署, 3=已归档
    private String color;         // 显示颜色: primary/success/warning/danger/info
    private String site;          // 所属站点
    private Integer isDefault;    // 是否默认状态
    private String remark;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
