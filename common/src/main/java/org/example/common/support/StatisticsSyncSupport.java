package org.example.common.support;

import cn.hutool.core.bean.BeanUtil;
import lombok.extern.slf4j.Slf4j;
import org.example.common.feign.StatisticsFeignClient;
import org.example.common.feign.dto.ClosingReportSyncDTO;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Component;

/**
 * 结案报告同步至统计服务
 */
@Slf4j
@Component
@ConditionalOnBean(StatisticsFeignClient.class)
public class StatisticsSyncSupport {

    private final StatisticsFeignClient statisticsFeignClient;

    public StatisticsSyncSupport(StatisticsFeignClient statisticsFeignClient) {
        this.statisticsFeignClient = statisticsFeignClient;
    }

    public void syncClosingReport(Object reportEntity) {
        if (reportEntity == null) {
            return;
        }
        try {
            ClosingReportSyncDTO dto = new ClosingReportSyncDTO();
            BeanUtil.copyProperties(reportEntity, dto);
            statisticsFeignClient.syncClosingReport(dto);
        } catch (Exception e) {
            log.error("结案报告同步统计服务失败（不影响主流程）", e);
        }
    }
}
