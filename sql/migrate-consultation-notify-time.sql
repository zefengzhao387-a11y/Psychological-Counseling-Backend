USE psy_consultation;

ALTER TABLE consultation_appointment
    ADD COLUMN notify_time DATETIME DEFAULT NULL COMMENT '短信通知时间' AFTER status;
