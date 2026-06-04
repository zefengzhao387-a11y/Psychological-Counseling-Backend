package org.example.consultation.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.example.common.base.BaseEntity;

/**
 * 结案报告
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ClosingReport extends BaseEntity {

    private Long appointmentId;
    private Long counselorId;
    private String studentNo;
    private String studentName;
    private String gender;
    private String department;
    private String phone;
    private Integer problemType;
    /** 咨询总次数 */
    private Integer totalSessions;
    /** 咨询效果自评 */
    private String selfEvaluation;
    /** Word 文件路径 */
    private String filePath;
}
