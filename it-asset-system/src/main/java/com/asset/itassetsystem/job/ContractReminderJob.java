package com.asset.itassetsystem.job;

import com.asset.itassetsystem.entity.SysContract;
import com.asset.itassetsystem.service.MailService;
import com.asset.itassetsystem.service.SysContractService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Component
public class ContractReminderJob {
    private static final Logger log = LoggerFactory.getLogger(ContractReminderJob.class);

    @Autowired
    private SysContractService contractService;

    @Autowired(required = false)
    private MailService mailService;

    @Value("${contract.remind.days:30,15,7}")
    private String remindDays;

    @Value("${contract.remind.email:}")
    private String remindEmail;

    @Scheduled(cron = "${contract.remind.cron:0 0 9 * * ?}")
    public void checkExpiringContracts() {
        if (mailService == null || remindEmail == null || remindEmail.isEmpty()) {
            log.debug("邮件服务或提醒邮箱未配置，跳过合同到期提醒");
            return;
        }
        List<SysContract> contracts = contractService.list(
            new LambdaQueryWrapper<SysContract>()
                .eq(SysContract::getStatus, 0)
                .isNotNull(SysContract::getExpiryDate)
        );
        if (contracts.isEmpty()) return;

        StringBuilder sb = new StringBuilder("<h3>📋 合同到期提醒</h3><table border='1' cellpadding='8' cellspacing='0' style='border-collapse:collapse'>");
        sb.append("<tr style='background:#F1F5F9'><th>合同编号</th><th>合同名称</th><th>供应商</th><th>到期日期</th><th>剩余天数</th></tr>");

        int count = 0;
        LocalDate today = LocalDate.now();
        for (SysContract c : contracts) {
            if (c.getExpiryDate() == null) continue;
            long days = ChronoUnit.DAYS.between(today, c.getExpiryDate());
            for (String d : remindDays.split(",")) {
                if (days == Long.parseLong(d.trim())) {
                    sb.append(String.format("<tr%s><td>%s</td><td>%s</td><td>%s</td><td>%s</td><td style='color:#DC2626;font-weight:bold'>%d天</td></tr>",
                        days <= 7 ? " style='background:#FEF2F2'" : "",
                        c.getContractNo(), c.getContractName(), c.getSupplier(),
                        c.getExpiryDate(), days));
                    count++;
                }
            }
        }
        sb.append("</table><p style='color:#64748B'>请及时处理到期合同。</p>");

        if (count > 0) {
            for (String email : remindEmail.split(",")) {
                mailService.send(email.trim(), "合同到期提醒 — " + count + " 份合同即将到期", sb.toString());
            }
            log.info("已发送合同到期提醒，共 {} 份", count);
        }
    }
}
