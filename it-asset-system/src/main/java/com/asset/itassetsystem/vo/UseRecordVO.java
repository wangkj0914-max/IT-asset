package com.asset.itassetsystem.vo;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 领用记录 VO（包含资产名称）
 */
@Data
public class UseRecordVO {
    private Long recordId;
    private Long assetId;
    private String assetName;      // 资产名称（从资产表关联）
    private String assetCode;      // 资产编号
    private String site;           // 站点
    private Long userId;
    private String department;
    private String contactPerson;
    private String contactPhone;
    private Integer useType;       // 1-领用 2-归还 3-调拨
    private LocalDateTime useDate;
    private LocalDateTime expectedReturnDate;
    private LocalDateTime actualReturnDate;
    private Integer overdueStatus; // 0-正常 1-已逾期 2-已关闭
    private LocalDateTime returnDate;
    private String approveUser;
    private Integer approveStatus; // 0-待审批 1-已通过 2-已拒绝
    private LocalDateTime approveTime;
    private String remark;
}
