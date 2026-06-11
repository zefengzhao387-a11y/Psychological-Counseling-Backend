package org.example.common.feign;

import org.example.common.feign.dto.CounselorBriefDTO;
import org.example.common.feign.dto.FirstVisitAppointmentBriefDTO;
import org.example.common.feign.dto.StudentProfileBriefDTO;
import org.example.common.feign.dto.TimeSlotBriefDTO;
import org.example.common.result.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;

import java.util.List;

/**
 * 预约服务 Feign 客户端（consultation-service 调用）
 */
@FeignClient(name = "appointment-service", url = "${service.appointment-url:http://localhost:8082}")
public interface AppointmentFeignClient {

    /** 初访评估完成后标记预约为已完成 */
    @PutMapping("/api/v1/appointment/first-visit/{id}/complete")
    R<Void> completeFirstVisit(@PathVariable("id") Long appointmentId);

    /** 时间段配置列表 */
    @GetMapping("/api/v1/appointment/time-config")
    R<List<TimeSlotBriefDTO>> listTimeConfig();

    /** 按学生 ID 获取登记表中的姓名学号（微服务内部调用） */
    @GetMapping("/api/v1/appointment/form/student/{studentId}/profile")
    R<StudentProfileBriefDTO> getStudentProfile(@PathVariable("studentId") Long studentId);

    /** 服务间：获取初访预约简要信息 */
    @GetMapping("/api/v1/appointment/first-visit/internal/{id}")
    R<FirstVisitAppointmentBriefDTO> getAppointmentBrief(@PathVariable("id") Long id);
}
