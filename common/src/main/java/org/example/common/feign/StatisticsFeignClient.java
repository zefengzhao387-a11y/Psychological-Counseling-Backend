package org.example.common.feign;

import org.example.common.feign.dto.ClosingReportSyncDTO;
import org.example.common.result.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * 统计服务 Feign 客户端
 */
@FeignClient(name = "statistics-service", url = "${service.statistics-url:http://localhost:8084}")
public interface StatisticsFeignClient {

    @PostMapping("/api/v1/statistics/report/sync")
    R<Void> syncClosingReport(@RequestBody ClosingReportSyncDTO dto);
}
