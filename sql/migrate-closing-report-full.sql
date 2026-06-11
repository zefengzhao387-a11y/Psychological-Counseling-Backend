-- 补全 psy_consultation.closing_report 表结构（与 init-all.sql 对齐）
USE psy_consultation;

ALTER TABLE closing_report
    ADD COLUMN student_grade VARCHAR(32) DEFAULT NULL COMMENT '年级' AFTER gender,
    ADD COLUMN student_major VARCHAR(64) DEFAULT NULL COMMENT '专业' AFTER department,
    ADD COLUMN student_email VARCHAR(64) DEFAULT NULL COMMENT '电子邮箱' AFTER phone,
    ADD COLUMN consultation_method VARCHAR(16) NOT NULL DEFAULT '面对面' COMMENT '咨询方式' AFTER problem_type,
    ADD COLUMN first_consultation_date DATETIME DEFAULT NULL COMMENT '首次咨询日期' AFTER consultation_method,
    ADD COLUMN closing_date DATETIME DEFAULT NULL COMMENT '结案日期' AFTER first_consultation_date,
    ADD COLUMN total_hours DECIMAL(6,2) NOT NULL DEFAULT 0.00 COMMENT '总咨询时长' AFTER total_sessions,
    ADD COLUMN closing_reason VARCHAR(32) DEFAULT NULL COMMENT '结案原因' AFTER total_hours,
    ADD COLUMN closing_reason_detail VARCHAR(512) DEFAULT NULL COMMENT '结案原因详细说明' AFTER closing_reason,
    ADD COLUMN case_summary TEXT DEFAULT NULL COMMENT '个案摘要' AFTER closing_reason_detail,
    ADD COLUMN counseling_outcome TEXT DEFAULT NULL COMMENT '咨询效果评估' AFTER self_evaluation,
    ADD COLUMN follow_up_plan VARCHAR(512) DEFAULT NULL COMMENT '后续跟进计划' AFTER counseling_outcome,
    ADD COLUMN referral_info VARCHAR(512) DEFAULT NULL COMMENT '转介信息' AFTER follow_up_plan,
    ADD COLUMN risk_level VARCHAR(8) DEFAULT '低' COMMENT '风险评估等级' AFTER referral_info,
    ADD COLUMN risk_note VARCHAR(512) DEFAULT NULL COMMENT '风险备注' AFTER risk_level,
    ADD COLUMN status VARCHAR(16) NOT NULL DEFAULT '草稿' COMMENT '状态' AFTER risk_note,
    ADD COLUMN reviewer_id BIGINT DEFAULT NULL COMMENT '审核人ID' AFTER status,
    ADD COLUMN reviewer_name VARCHAR(32) DEFAULT NULL COMMENT '审核人姓名' AFTER reviewer_id,
    ADD COLUMN review_comment VARCHAR(512) DEFAULT NULL COMMENT '审核意见' AFTER reviewer_name,
    ADD COLUMN review_date DATETIME DEFAULT NULL COMMENT '审核日期' AFTER review_comment;
