-- ============================================
-- 心语碎片（Healing Fragment）功能
-- 学生每日心情记录 + AI 生成的治愈内容
-- ============================================

CREATE TABLE IF NOT EXISTS `healing_fragment` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    `student_id` BIGINT NOT NULL COMMENT '学生用户ID',
    `mood_level` INT DEFAULT NULL COMMENT '心情等级：1-很差 2-不太好 3-一般 4-不错 5-很好',
    `note` VARCHAR(500) DEFAULT NULL COMMENT '学生心情笔记',
    `fragment_content` TEXT COMMENT 'AI 生成的治愈碎片内容（温暖鼓励话语）',
    `is_read` TINYINT DEFAULT 0 COMMENT '是否已查看：0-未查看 1-已查看',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除',
    PRIMARY KEY (`id`),
    INDEX `idx_student_id` (`student_id`),
    INDEX `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='心语碎片表';
