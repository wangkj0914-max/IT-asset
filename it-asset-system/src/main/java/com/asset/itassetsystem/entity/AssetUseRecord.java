package com.asset.itassetsystem.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 资产领用记录实体类
 */
@Data
@TableName("asset_use_record")
public class AssetUseRecord {
    @TableId(type = IdType.AUTO)
    private Long recordId;
    
    private Long assetId;
    private Long userId;
    private String department;
    private String contactPerson;
    private String contactPhone;
    private Integer useType; // 1-领用 2-归还 3-调拨
    private LocalDateTime useDate;
    private LocalDateTime expectedReturnDate;
    private LocalDateTime actualReturnDate;
    private Integer overdueStatus; // 0-正常 1-已逾期 2-已关闭
    private LocalDateTime returnDate;
    private String approveUser;
    private Integer approveStatus; // 0-待审批 1-已通过 2-已拒绝
    private LocalDateTime approveTime;
    private String remark;
    private String site;
    private LocalDateTime createTime;
}
