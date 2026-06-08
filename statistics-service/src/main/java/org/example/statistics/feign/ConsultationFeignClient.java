package org.example.statistics.feign;

import org.example.common.result.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;

/**
 * consultation-service Feign 客户端
 * <p>
 * 用于 statistics-service 调用 consultation-service 获取咨询记录、结案报告等数据
 * </p>
 */
@FeignClient(
        name = "consultation-service",
        url = "${service.consultation-url:http://localhost:8083}"
)
public interface ConsultationFeignClient {

    /**
     * 根据 ID 查询结案报告详情
     */
    @GetMapping("/api/v1/consultation/report/{id}")
    R<Map<String, Object>> getReportById(@PathVariable Long id);

    /**
     * 触发 Word 文档生成
     */
    @PostMapping("/api/v1/consultation/report/{id}/word")
    R<String> generateWord(@PathVariable Long id);

    /**
     * 分页查询结案报告列表（供汇总查询使用）
     */
    @GetMapping("/api/v1/consultation/report/list")
    R<Map<String, Object>> listReports(
            @RequestParam(required = false) String studentNo,
            @RequestParam(required = false) String studentName,
            @RequestParam(required = false) Long counselorId,
            @RequestParam(required = false) Integer problemType,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String gender,
            @RequestParam(required = false) String department,
            @RequestParam(required = false) String studentGrade,
            @RequestParam(defaultValue = "1") Long page,
            @RequestParam(defaultValue = "100") Long size);

    /**
     * 简单条件查询结案报告
     */
    @GetMapping("/api/v1/consultation/report")
    R<List<Map<String, Object>>> queryReports(
            @RequestParam(required = false) String studentNo,
            @RequestParam(required = false) String studentName,
            @RequestParam(required = false) Long counselorId,
            @RequestParam(required = false) Integer problemType);
}
