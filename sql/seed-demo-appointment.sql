USE psy_appointment;

INSERT INTO first_visit_form (student_id, student_name, student_no, phone, gender, department, questionnaire, total_score, is_urgent, has_read_consent, consent_time)
SELECT 5, '学生小明', 'stu001', '13800000005', '男', '计算机学院', '{"scores":[3,4,5]}', 12, 0, 1, NOW()
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM first_visit_form WHERE student_no = 'stu001');

INSERT INTO duty_schedule (counselor_id, counselor_type, duty_date, time_slot_id, max_appointments, booked_count)
SELECT 2, 1, '2026-06-02', 1, 5, 0 FROM DUAL
WHERE NOT EXISTS (
  SELECT 1 FROM duty_schedule WHERE duty_date = '2026-06-02' AND time_slot_id = 1 AND counselor_id = 2
);
