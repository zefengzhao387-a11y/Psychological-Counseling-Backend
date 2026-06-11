-- 为学生初访预约测试补排班（初访员 fv001，user_id=2）
USE psy_appointment;
SET NAMES utf8mb4;

INSERT INTO duty_schedule (counselor_id, counselor_type, duty_date, time_slot_id, max_appointments, booked_count)
SELECT 2, 1, d.dt, s.slot_id, 5, 0
FROM (
    SELECT '2026-06-09' AS dt UNION ALL SELECT '2026-06-10' UNION ALL SELECT '2026-06-11'
    UNION ALL SELECT '2026-06-12' UNION ALL SELECT '2026-06-13' UNION ALL SELECT '2026-06-16'
) d
CROSS JOIN (SELECT 1 AS slot_id UNION ALL SELECT 2 UNION ALL SELECT 3) s
WHERE NOT EXISTS (
    SELECT 1 FROM duty_schedule ds
    WHERE ds.counselor_id = 2 AND ds.duty_date = d.dt AND ds.time_slot_id = s.slot_id AND ds.deleted = 0
);
