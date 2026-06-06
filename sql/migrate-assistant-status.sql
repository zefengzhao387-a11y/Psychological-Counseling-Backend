-- 已有数据库增量：初访结果表增加心理助理处理状态
USE psy_consultation;

ALTER TABLE `first_visit_result`
    ADD COLUMN `assistant_status` TINYINT NOT NULL DEFAULT 0
        COMMENT '心理助理处理：0待处理 1已安排 2已标记处理'
        AFTER `conclusion`;
