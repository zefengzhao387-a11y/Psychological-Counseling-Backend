package org.example.statistics.dto;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 统计分析 - 多条件筛选查询参数模型
 *
 * <p>支持的筛选维度：</p>
 * <ul>
 *   <li>学生维度：学号、姓名、院系、年级</li>
 *   <li>咨询师维度：咨询师ID</li>
 *   <li>问题维度：问题类型、咨询方式</li>
 *   <li>时间维度：首次咨询日期范围、结案日期范围</li>
 *   <li>状态维度：报告状态、结案原因、风险等级</li>
 * </ul>
 */
@Data
public class StatisticsQueryDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    // ==================== 学生维度 ====================

    /** 学号（模糊查询） */
    private String studentNo;

    /** 学生姓名（模糊查询） */
    private String studentName;

    /** 学生性别 */
    private String gender;

    /** 院系 */
    private String department;

    /** 年级 */
    private String studentGrade;

    // ==================== 咨询师维度 ====================

    /** 咨询师ID */
    private Long counselorId;

    // ==================== 问题维度 ====================

    /** 问题类型 */
    private Integer problemType;

    /** 咨询方式 */
    private String consultationMethod;

    // ==================== 时间维度 ====================

    /** 首次咨询日期 - 起始 */
    private LocalDateTime firstConsultationStart;

    /** 首次咨询日期 - 结束 */
    private LocalDateTime firstConsultationEnd;

    /** 结案日期 - 起始 */
    private LocalDateTime closingDateStart;

    /** 结案日期 - 结束 */
    private LocalDateTime closingDateEnd;

    // ==================== 状态维度 ====================

    /** 报告状态：草稿、已提交、已审核、已驳回 */
    private String status;

    /** 结案原因 */
    private String closingReason;

    /** 风险评估等级：低、中、高 */
    private String riskLevel;

    // ==================== 分页 ====================

    /** 当前页码（默认1） */
    private Long page = 1L;

    /** 每页条数（默认10） */
    private Long size = 10L;

    // ==================== 导出标识 ====================

    /** 是否导出Excel（true时返回全部数据不分页） */
    private Boolean export = false;
}
