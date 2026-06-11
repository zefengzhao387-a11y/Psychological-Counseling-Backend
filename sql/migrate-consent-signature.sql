USE psy_appointment;
ALTER TABLE first_visit_form
  ADD COLUMN consent_signature VARCHAR(64) DEFAULT NULL COMMENT '电子签名（姓名）' AFTER consent_time;
