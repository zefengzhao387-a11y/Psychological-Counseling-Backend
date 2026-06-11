package org.example.consultation.service;

import org.example.common.feign.dto.AppointmentProgressDTO;

import java.util.List;

public interface ConsultationProgressService {

    List<AppointmentProgressDTO> listByAppointmentIds(List<Long> appointmentIds);
}
