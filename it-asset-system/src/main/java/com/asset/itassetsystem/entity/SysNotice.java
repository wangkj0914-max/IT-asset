package com.asset.itassetsystem.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("sys_notice")
public class SysNotice {
    @TableId(type = IdType.AUTO)
    private Long noticeId;
    private String title;
    private String content;
    private Integer noticeType; // 1-通知 2-公告
    private Integer status;     // 1-发布 0-草稿
    private Long createUserId;
    private String createUserName;
    private String site;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
