-- 修复已评估但预约仍为「已通过」的历史数据
USE psy_appointment;
UPDATE first_visit_appointment a
SET a.status = 5
WHERE a.status = 2
  AND EXISTS (
    SELECT 1 FROM psy_consultation.first_visit_result r
    WHERE r.appointment_id = a.id AND r.deleted = 0
  );

USE psy_consultation;
-- 同一预约只保留最新一条评估记录
DELETE r1 FROM first_visit_result r1
INNER JOIN first_visit_result r2
  ON r1.appointment_id = r2.appointment_id AND r1.id < r2.id;

-- 防止重复评估
ALTER TABLE first_visit_result
  ADD UNIQUE KEY uk_appointment_id (appointment_id);
