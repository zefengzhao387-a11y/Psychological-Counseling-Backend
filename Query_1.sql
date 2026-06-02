-- =====================================================
-- 心理预约系统 — 全部数据库 & 表结构 DDL
-- 用法：在 MySQL 中直接执行此文件即可建库建表
-- =====================================================

-- ---------- 创建数据库 ----------
CREATE DATABASE IF NOT EXISTS psy_user DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
CREATE DATABASE IF NOT EXISTS psy_appointment DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
CREATE DATABASE IF NOT EXISTS psy_consultation DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
CREATE DATABASE IF NOT EXISTS psy_statistics DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
CREATE DATABASE IF NOT EXISTS psy_notification DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;

-- =====================================================
-- 1. psy_user（用户服务）
-- =====================================================
USE psy_user;

-- 1.1 用户表
DROP TABLE IF EXISTS `sys_user`;
CREATE TABLE `sys_user` (
                            `id`            BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
                            `user_no`       VARCHAR(32)  NOT NULL COMMENT '学号/工号',
                            `username`      VARCHAR(32)  NOT NULL COMMENT '姓名',
                            `password`      VARCHAR(128) NOT NULL COMMENT '密码（MD5 加密）',
                            `phone`         VARCHAR(16)  DEFAULT NULL COMMENT '手机号',
                            `gender`        VARCHAR(4)   DEFAULT NULL COMMENT '性别：男/女',
                            `department`    VARCHAR(64)  DEFAULT NULL COMMENT '院系',
                            `role_code`     TINYINT      NOT NULL COMMENT '角色：1学生 2初访员 3心理助理 4咨询师 5中心管理员',
                            `create_time`   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                            `update_time`   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                            `deleted`       TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除：0未删除 1已删除',
                            PRIMARY KEY (`id`),
                            UNIQUE KEY `uk_user_no` (`user_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 1.2 咨询师/初访员信息表
DROP TABLE IF EXISTS `counselor_info`;
CREATE TABLE `counselor_info` (
                                  `id`             BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
                                  `user_id`        BIGINT       NOT NULL COMMENT '关联 sys_user.id',
                                  `name`           VARCHAR(32)  NOT NULL COMMENT '姓名',
                                  `gender`         VARCHAR(4)   DEFAULT NULL COMMENT '性别',
                                  `phone`          VARCHAR(16)  DEFAULT NULL COMMENT '联系电话',
                                  `email`          VARCHAR(64)  DEFAULT NULL COMMENT '邮箱',
                                  `type`           TINYINT      NOT NULL COMMENT '类型：1初访员 2咨询师',
                                  `qualification`  VARCHAR(128) DEFAULT NULL COMMENT '专业资质',
                                  `specialty`      VARCHAR(256) DEFAULT NULL COMMENT '擅长领域（逗号分隔）',
                                  `status`         TINYINT      NOT NULL DEFAULT 1 COMMENT '状态：1在职 2离职',
                                  `create_time`    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                  `update_time`    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                                  `deleted`        TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除',
                                  PRIMARY KEY (`id`),
                                  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='咨询师/初访员信息表';

-- ---------- 初始数据：管理员 + 示例用户 ----------
INSERT INTO `sys_user` (`user_no`, `username`, `password`, `phone`, `gender`, `department`, `role_code`) VALUES
                                                                                                             ('admin001', '系统管理员', MD5('123456'), '13800000001', '男', '心理健康中心', 5),
                                                                                                             ('fv001',     '初访员张三',   MD5('123456'), '13800000002', '女', '心理健康中心', 2),
                                                                                                             ('pa001',     '助理李四',     MD5('123456'), '13800000003', '女', '心理健康中心', 3),
                                                                                                             ('co001',     '咨询师王五',   MD5('123456'), '13800000004', '男', '心理健康中心', 4),
                                                                                                             ('stu001',    '学生小明',     MD5('123456'), '13800000005', '男', '计算机学院',   1);

INSERT INTO `counselor_info` (`user_id`, `name`, `gender`, `phone`, `email`, `type`, `qualification`, `specialty`, `status`) VALUES
                                                                                                                                 (2, '初访员张三', '女', '13800000002', 'zhang@school.edu.cn', 1, '国家二级心理咨询师', '学业压力,情绪管理', 1),
                                                                                                                                 (3, '助理李四',   '女', '13800000003', 'li@school.edu.cn',   1, '助理心理咨询师',   '人际关系',     1),
                                                                                                                                 (4, '咨询师王五', '男', '13800000004', 'wang@school.edu.cn', 2, '国家二级心理咨询师', '职业规划,恋爱心理', 1);


-- =====================================================
-- 2. psy_appointment（预约服务）
-- =====================================================
USE psy_appointment;

-- 2.1 时间段配置表
DROP TABLE IF EXISTS `time_config`;
CREATE TABLE `time_config` (
                               `id`              BIGINT      NOT NULL AUTO_INCREMENT COMMENT '主键',
                               `slot_name`       VARCHAR(32) NOT NULL COMMENT '时间段名称，如"08:00-09:00"',
                               `start_time`      TIME        NOT NULL COMMENT '开始时间',
                               `end_time`        TIME        NOT NULL COMMENT '结束时间',
                               `interval_minutes` INT        NOT NULL DEFAULT 10 COMMENT '来访间隔（分钟）',
                               `create_time`     DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                               `update_time`     DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                               `deleted`         TINYINT     NOT NULL DEFAULT 0 COMMENT '逻辑删除',
                               PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='时间段配置表';

-- 2.2 值班安排表
DROP TABLE IF EXISTS `duty_schedule`;
CREATE TABLE `duty_schedule` (
                                 `id`              BIGINT   NOT NULL AUTO_INCREMENT COMMENT '主键',
                                 `counselor_id`    BIGINT   NOT NULL COMMENT '老师ID（关联 sys_user.id）',
                                 `counselor_type`  TINYINT  NOT NULL COMMENT '老师类型：1初访员 2咨询师',
                                 `duty_date`       DATE     NOT NULL COMMENT '值班日期',
                                 `time_slot_id`    BIGINT   NOT NULL COMMENT '时间段ID（关联 time_config.id）',
                                 `max_appointments` INT     NOT NULL DEFAULT 4 COMMENT '该时段最大预约数',
                                 `booked_count`    INT      NOT NULL DEFAULT 0 COMMENT '已预约数',
                                 `create_time`     DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                 `update_time`     DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                                 `deleted`         TINYINT  NOT NULL DEFAULT 0 COMMENT '逻辑删除',
                                 PRIMARY KEY (`id`),
                                 KEY `idx_counselor_date` (`counselor_id`, `duty_date`),
                                 KEY `idx_date_slot` (`duty_date`, `time_slot_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='值班安排表';

-- 2.3 首访登记表
DROP TABLE IF EXISTS `first_visit_form`;
CREATE TABLE `first_visit_form` (
                                    `id`             BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
                                    `student_id`     BIGINT       NOT NULL COMMENT '学生用户ID',
                                    `student_name`   VARCHAR(32)  NOT NULL COMMENT '学生姓名',
                                    `student_no`     VARCHAR(32)  NOT NULL COMMENT '学号',
                                    `gender`         VARCHAR(4)   DEFAULT NULL COMMENT '性别',
                                    `department`     VARCHAR(64)  DEFAULT NULL COMMENT '院系',
                                    `phone`          VARCHAR(16)  NOT NULL COMMENT '联系电话',
    -- 问卷计分（简化设计：存储各题分数 + 总分）
                                    `questionnaire`  JSON         DEFAULT NULL COMMENT '问卷答案 JSON（含每题分数）',
                                    `total_score`    INT          DEFAULT 0 COMMENT '问卷总分',
                                    `is_urgent`      TINYINT      NOT NULL DEFAULT 0 COMMENT '是否紧急（计分报警）：0否 1是',
                                    `has_read_consent` TINYINT    NOT NULL DEFAULT 0 COMMENT '是否已阅读知情同意书：0否 1是',
                                    `consent_time`   DATETIME     DEFAULT NULL COMMENT '同意时间',
                                    `create_time`    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                    `update_time`    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                                    `deleted`        TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除',
                                    PRIMARY KEY (`id`),
                                    KEY `idx_student_id` (`student_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='首访登记表';

-- 2.4 初访预约记录表
DROP TABLE IF EXISTS `first_visit_appointment`;
CREATE TABLE `first_visit_appointment` (
                                           `id`              BIGINT      NOT NULL AUTO_INCREMENT COMMENT '主键',
                                           `student_id`      BIGINT      NOT NULL COMMENT '学生用户ID',
                                           `form_id`         BIGINT      NOT NULL COMMENT '首访登记表ID',
                                           `visitor_id`      BIGINT      DEFAULT NULL COMMENT '初访员ID（管理员审核时分配）',
                                           `duty_schedule_id` BIGINT     DEFAULT NULL COMMENT '值班安排ID',
                                           `appointment_date` DATE       NOT NULL COMMENT '预约日期',
                                           `time_slot_id`    BIGINT      NOT NULL COMMENT '时间段ID',
                                           `location`        VARCHAR(128) DEFAULT NULL COMMENT '咨询地点',
                                           `status`          TINYINT     NOT NULL DEFAULT 1 COMMENT '状态：1待审核 2已通过 3已拒绝 4已撤销',
                                           `is_priority`     TINYINT     NOT NULL DEFAULT 0 COMMENT '是否优先排队：0否 1是',
                                           `reviewer_id`     BIGINT      DEFAULT NULL COMMENT '审核人ID',
                                           `review_time`     DATETIME    DEFAULT NULL COMMENT '审核时间',
                                           `review_remark`   VARCHAR(256) DEFAULT NULL COMMENT '审核备注',
                                           `create_time`     DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间（即排队时间）',
                                           `update_time`     DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                                           `deleted`         TINYINT     NOT NULL DEFAULT 0 COMMENT '逻辑删除',
                                           PRIMARY KEY (`id`),
                                           KEY `idx_student_id` (`student_id`),
                                           KEY `idx_status` (`status`),
                                           KEY `idx_appointment_date` (`appointment_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='初访预约记录表';

-- ---------- 初始数据：时间段 ----------
INSERT INTO `time_config` (`slot_name`, `start_time`, `end_time`, `interval_minutes`) VALUES
                                                                                          ('08:00-08:50', '08:00:00', '08:50:00', 10),
                                                                                          ('09:00-09:50', '09:00:00', '09:50:00', 10),
                                                                                          ('10:00-10:50', '10:00:00', '10:50:00', 10),
                                                                                          ('14:00-14:50', '14:00:00', '14:50:00', 10),
                                                                                          ('15:00-15:50', '15:00:00', '15:50:00', 10),
                                                                                          ('16:00-16:50', '16:00:00', '16:50:00', 10);


-- =====================================================
-- 3. psy_consultation（咨询管理服务）
-- =====================================================
USE psy_consultation;

-- 3.1 初访评估结果表
DROP TABLE IF EXISTS `first_visit_result`;
CREATE TABLE `first_visit_result` (
                                      `id`             BIGINT    NOT NULL AUTO_INCREMENT COMMENT '主键',
                                      `appointment_id` BIGINT    NOT NULL COMMENT '初访预约ID',
                                      `student_id`     BIGINT    NOT NULL COMMENT '学生ID',
                                      `visitor_id`     BIGINT    NOT NULL COMMENT '初访员ID',
                                      `crisis_level`   TINYINT   NOT NULL COMMENT '危机等级：1低 2中 3高 4紧急',
                                      `problem_type`   TINYINT   NOT NULL COMMENT '问题类型：1学业 2情绪 3人际 4恋爱 5职业 6成长 7家庭 8其他',
                                      `visit_time`     DATETIME  NOT NULL COMMENT '初访时间',
                                      `conclusion`     TINYINT   NOT NULL COMMENT '初访结论：1无需咨询 2安排咨询 3转介送诊',
                                      `remark`         TEXT      DEFAULT NULL COMMENT '备注',
                                      `create_time`    DATETIME  NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                      `update_time`    DATETIME  NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                                      `deleted`        TINYINT   NOT NULL DEFAULT 0 COMMENT '逻辑删除',
                                      PRIMARY KEY (`id`),
                                      KEY `idx_appointment_id` (`appointment_id`),
                                      KEY `idx_student_id` (`student_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='初访评估结果表';

-- 3.2 咨询安排记录表
DROP TABLE IF EXISTS `consultation_appointment`;
CREATE TABLE `consultation_appointment` (
                                            `id`                    BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
                                            `student_id`            BIGINT       NOT NULL COMMENT '学生ID',
                                            `first_visit_result_id` BIGINT       NOT NULL COMMENT '初访结果ID',
                                            `counselor_id`          BIGINT       NOT NULL COMMENT '咨询师ID',
                                            `start_date`            DATE         NOT NULL COMMENT '咨询开始日期',
                                            `time_slot_id`          BIGINT       NOT NULL COMMENT '时间段ID',
                                            `day_of_week`           TINYINT      DEFAULT NULL COMMENT '每周几（1-7）',
                                            `location`              VARCHAR(128) NOT NULL COMMENT '咨询地点',
                                            `occupied_weeks`        INT          NOT NULL DEFAULT 8 COMMENT '占用总周数',
                                            `remaining_weeks`       INT          NOT NULL DEFAULT 8 COMMENT '剩余周数',
                                            `status`                TINYINT      NOT NULL DEFAULT 1 COMMENT '状态：1进行中 2已结案 3已脱落',
                                            `create_time`           DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                            `update_time`           DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                                            `deleted`               TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除',
                                            PRIMARY KEY (`id`),
                                            KEY `idx_student_id` (`student_id`),
                                            KEY `idx_counselor_id` (`counselor_id`),
                                            KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='咨询安排记录表';

-- 3.3 咨询记录表
DROP TABLE IF EXISTS `consultation_record`;
CREATE TABLE `consultation_record` (
                                       `id`              BIGINT   NOT NULL AUTO_INCREMENT COMMENT '主键',
                                       `appointment_id`  BIGINT   NOT NULL COMMENT '咨询安排ID',
                                       `session_number`  INT      NOT NULL COMMENT '第几次咨询（1,2,3...8,9+）',
                                       `consult_date`    DATE     NOT NULL COMMENT '咨询日期',
                                       `status`          TINYINT  NOT NULL COMMENT '状态：1完成咨询 2旷约 3请假 4脱落 5结案',
                                       `content`         TEXT     DEFAULT NULL COMMENT '咨询内容记录',
                                       `counselor_note`  TEXT     DEFAULT NULL COMMENT '咨询师备注',
                                       `create_time`     DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                       `update_time`     DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                                       `deleted`         TINYINT  NOT NULL DEFAULT 0 COMMENT '逻辑删除',
                                       PRIMARY KEY (`id`),
                                       KEY `idx_appointment_id` (`appointment_id`),
                                       KEY `idx_consult_date` (`consult_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='咨询记录表';

-- 3.4 追加咨询时段申请表
DROP TABLE IF EXISTS `consultation_extension`;
CREATE TABLE `consultation_extension` (
                                          `id`              BIGINT      NOT NULL AUTO_INCREMENT COMMENT '主键',
                                          `appointment_id`  BIGINT      NOT NULL COMMENT '咨询安排ID',
                                          `counselor_id`    BIGINT      NOT NULL COMMENT '申请人ID（咨询师）',
                                          `extend_weeks`    INT         NOT NULL COMMENT '申请追加周数',
                                          `reason`          TEXT        NOT NULL COMMENT '申请理由',
                                          `status`          TINYINT     NOT NULL DEFAULT 1 COMMENT '状态：1待审批 2已通过 3已拒绝',
                                          `approver_id`     BIGINT      DEFAULT NULL COMMENT '审批人ID（管理员）',
                                          `approve_time`    DATETIME    DEFAULT NULL COMMENT '审批时间',
                                          `approve_remark`  VARCHAR(256) DEFAULT NULL COMMENT '审批备注',
                                          `create_time`     DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                          `update_time`     DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                                          `deleted`         TINYINT     NOT NULL DEFAULT 0 COMMENT '逻辑删除',
                                          PRIMARY KEY (`id`),
                                          KEY `idx_appointment_id` (`appointment_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='追加咨询时段申请表';

-- 3.5 结案报告表
DROP TABLE IF EXISTS `closing_report`;
CREATE TABLE `closing_report` (
                                  `id`               BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
                                  `appointment_id`   BIGINT       NOT NULL COMMENT '咨询安排ID',
                                  `counselor_id`     BIGINT       NOT NULL COMMENT '咨询师ID',
                                  `student_no`       VARCHAR(32)  NOT NULL COMMENT '来访者学号',
                                  `student_name`     VARCHAR(32)  NOT NULL COMMENT '来访者姓名',
                                  `gender`           VARCHAR(4)   NOT NULL COMMENT '来访者性别',
                                  `department`       VARCHAR(64)  NOT NULL COMMENT '来访者院系',
                                  `phone`            VARCHAR(16)  NOT NULL COMMENT '来访者联系电话',
                                  `problem_type`     TINYINT      NOT NULL COMMENT '问题类型',
                                  `total_sessions`   INT          NOT NULL COMMENT '咨询总次数',
                                  `self_evaluation`  TEXT         NOT NULL COMMENT '咨询效果自评',
                                  `file_path`        VARCHAR(256) DEFAULT NULL COMMENT 'Word 文件路径',
                                  `create_time`      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                                  `update_time`      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                                  `deleted`          TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除',
                                  PRIMARY KEY (`id`),
                                  KEY `idx_appointment_id` (`appointment_id`),
                                  KEY `idx_student_no` (`student_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='结案报告表';


-- =====================================================
-- 4. psy_notification（通知服务）
-- =====================================================
USE psy_notification;

-- 4.1 短信发送日志表
DROP TABLE IF EXISTS `sms_log`;
CREATE TABLE `sms_log` (
                           `id`             BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
                           `phone`          VARCHAR(16)  NOT NULL COMMENT '接收手机号',
                           `content`        TEXT         NOT NULL COMMENT '短信内容',
                           `template_code`  VARCHAR(32)  DEFAULT NULL COMMENT '短信模板编码',
                           `send_status`    TINYINT      NOT NULL DEFAULT 0 COMMENT '发送状态：0待发送 1已发送 2发送失败',
                           `send_time`      DATETIME     DEFAULT NULL COMMENT '实际发送时间',
                           `fail_reason`    VARCHAR(256) DEFAULT NULL COMMENT '失败原因',
                           `create_time`    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                           `update_time`    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                           `deleted`        TINYINT      NOT NULL DEFAULT 0 COMMENT '逻辑删除',
                           PRIMARY KEY (`id`),
                           KEY `idx_phone` (`phone`),
                           KEY `idx_send_status` (`send_status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='短信发送日志表';
