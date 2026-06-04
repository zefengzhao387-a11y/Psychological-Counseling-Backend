package org.example.appointment.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.example.appointment.dto.StudentSearchVO;
import org.example.appointment.entity.FirstVisitForm;

import java.util.List;

public interface FirstVisitFormService extends IService<FirstVisitForm> {

    /** 学生提交首访登记表（含计分逻辑） */
    FirstVisitForm submit(FirstVisitForm form);

    /** 确认知情同意书 */
    void confirmConsent(Long formId, Long studentId);

    /** 按学号或姓名模糊搜索学生（返回去重后的学生列表及其最新登记表） */
    List<StudentSearchVO> searchByKeyword(String keyword);

    /** 获取某学生最新的登记表 */
    FirstVisitForm getLatestByStudentId(Long studentId);
}
