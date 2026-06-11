package org.example.common.feign;

import org.example.common.feign.dto.AppointmentProgressDTO;
import org.example.common.result.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * 咨询服务 Feign 客户端（appointment-service 调用）
 */
@FeignClient(name = "consultation-service", url = "${service.consultation-url:http://localhost:8083}")
public interface ConsultationFeignClient {

    /** 初访员已评估的预约 ID 列表 */
    @GetMapping("/api/v1/consultation/result/evaluated-appointment-ids")
    R<List<Long>> listEvaluatedAppointmentIds(@RequestParam("visitorId") Long visitorId);

    /** 全部已评估的初访预约 ID（服务间同步状态用） */
    @GetMapping("/api/v1/consultation/result/internal/evaluated-appointment-ids")
    R<List<Long>> listAllEvaluatedAppointmentIds();

    /** 按初访预约 ID 批量查询咨询/结案进度 */
    @GetMapping("/api/v1/consultation/progress/by-appointments")
    R<List<AppointmentProgressDTO>> listProgressByAppointments(@RequestParam("ids") String ids);
}
