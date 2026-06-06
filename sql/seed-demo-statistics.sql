-- 统计分析页演示数据（psy_statistics.closing_report）
-- 执行方式：cmd /c "mysql -u root -p123456 --default-character-set=utf8mb4 < seed-demo-statistics.sql"
USE psy_statistics;
SET NAMES utf8mb4;

INSERT INTO closing_report (
    appointment_id, counselor_id, student_no, student_name, gender, student_grade,
    department, student_major, phone, student_email, problem_type, consultation_method,
    first_consultation_date, closing_date, total_sessions, total_hours,
    closing_reason, case_summary, self_evaluation, counseling_outcome,
    risk_level, status, reviewer_id, reviewer_name, review_date
)
SELECT 1, 4, 'stu001', '学生小明', '男', '2023级', '计算机学院', '软件工程',
       '13800000005', 'stu001@school.edu.cn', 2, '面对面',
       '2026-03-01 10:00:00', '2026-05-20 16:00:00', 6, 9.00,
       '目标达成', '来访者因学业压力与情绪困扰求助，经6次咨询后状态明显改善。',
       '情绪更稳定，能更好应对考试压力。', '咨询目标基本达成，建议定期自我关注。',
       '低', '已审核', 1, '系统管理员', '2026-05-21 09:00:00'
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM closing_report WHERE student_no = 'stu001' AND status = '已审核');

INSERT INTO closing_report (
    appointment_id, counselor_id, student_no, student_name, gender, student_grade,
    department, phone, problem_type, consultation_method,
    first_consultation_date, closing_date, total_sessions, total_hours,
    closing_reason, self_evaluation, risk_level, status
)
SELECT 2, 4, 'stu002', '学生小红', '女', '2022级', '外国语学院',
       '13800000006', 3, '面对面',
       '2026-04-10 14:00:00', '2026-06-01 15:00:00', 4, 6.00,
       '来访者主动结束', '人际关系有所改善，仍需继续练习沟通技巧。', '低', '草稿'
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM closing_report WHERE student_no = 'stu002');

INSERT INTO closing_report (
    appointment_id, counselor_id, student_no, student_name, gender, student_grade,
    department, phone, problem_type, consultation_method,
    first_consultation_date, closing_date, total_sessions, total_hours,
    closing_reason, self_evaluation, counseling_outcome, risk_level, status,
    reviewer_id, reviewer_name, review_date
)
SELECT 3, 4, 'stu003', '学生小刚', '男', '2024级', '经济学院',
       '13800000007', 1, '线上视频',
       '2026-02-15 09:00:00', '2026-04-30 11:00:00', 8, 12.00,
       '目标达成', '学习效率提升，焦虑减轻。', '来访者学业状态恢复良好。',
       '低', '已审核', 1, '系统管理员', '2026-05-01 10:00:00'
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM closing_report WHERE student_no = 'stu003');
