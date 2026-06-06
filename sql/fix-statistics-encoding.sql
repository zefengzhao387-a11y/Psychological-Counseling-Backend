USE psy_statistics;
SET NAMES utf8mb4;

UPDATE closing_report SET status = '已审核', closing_reason = '目标达成', risk_level = '低' WHERE id = 1;
UPDATE closing_report SET status = '草稿', closing_reason = '来访者主动结束', risk_level = '低' WHERE id = 2;
UPDATE closing_report SET status = '已审核', closing_reason = '目标达成', risk_level = '低' WHERE id = 3;
