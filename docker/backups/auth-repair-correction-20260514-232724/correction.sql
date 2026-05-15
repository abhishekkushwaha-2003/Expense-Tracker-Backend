USE spendsmart_auth_db;

DROP TABLE IF EXISTS user_id_repair_map_corrected;
CREATE TABLE user_id_repair_map_corrected (
    old_docker_user_id BIGINT PRIMARY KEY,
    repaired_user_id BIGINT NOT NULL,
    email VARCHAR(255),
    created_at DATETIME(6)
);

INSERT INTO user_id_repair_map_corrected (old_docker_user_id, repaired_user_id, email, created_at)
SELECT c.user_id,
       CASE
           WHEN o.user_id IS NOT NULL THEN o.user_id
           WHEN c.user_id = 1 THEN 44
           WHEN c.user_id = 5 THEN 47
           WHEN c.user_id = 6 THEN 48
           ELSE c.user_id
       END AS repaired_user_id,
       c.email,
       c.created_at
FROM users_docker_before_repair c
LEFT JOIN old_users_restore o ON LOWER(o.email) = LOWER(c.email)
ORDER BY c.user_id;

UPDATE old_users_restore o
JOIN users_docker_before_repair c ON LOWER(c.email) = LOWER(o.email)
SET
    o.full_name = COALESCE(NULLIF(c.full_name, ''), o.full_name),
    o.password = COALESCE(NULLIF(c.password, ''), o.password),
    o.currency = COALESCE(NULLIF(c.currency, ''), o.currency),
    o.timezone = COALESCE(NULLIF(c.timezone, ''), o.timezone),
    o.status = COALESCE(NULLIF(c.status, ''), o.status),
    o.monthly_budget = COALESCE(c.monthly_budget, o.monthly_budget);

INSERT INTO old_users_restore (user_id, created_at, currency, email, full_name, monthly_budget, password, status, timezone)
SELECT m.repaired_user_id, c.created_at, c.currency, c.email, c.full_name, c.monthly_budget, c.password, c.status, c.timezone
FROM users_docker_before_repair c
JOIN user_id_repair_map_corrected m ON m.old_docker_user_id = c.user_id
LEFT JOIN old_users_restore o ON LOWER(o.email) = LOWER(c.email)
WHERE o.user_id IS NULL;

DROP TEMPORARY TABLE IF EXISTS wrong_repair_ids;
CREATE TEMPORARY TABLE wrong_repair_ids (wrong_id BIGINT PRIMARY KEY, correct_id BIGINT NOT NULL);
INSERT INTO wrong_repair_ids VALUES (45, 6), (46, 34);
UPDATE spendsmart_budget_db.budgets t JOIN wrong_repair_ids m ON t.user_id = m.wrong_id SET t.user_id = m.correct_id;
UPDATE spendsmart_category_db.categories t JOIN wrong_repair_ids m ON t.user_id = m.wrong_id SET t.user_id = m.correct_id;
UPDATE spendsmart_expense_db.expenses t JOIN wrong_repair_ids m ON t.user_id = m.wrong_id SET t.user_id = m.correct_id;
UPDATE spendsmart_income_db.income t JOIN wrong_repair_ids m ON t.user_id = m.wrong_id SET t.user_id = m.correct_id;
UPDATE spendsmart_payment_db.payments t JOIN wrong_repair_ids m ON t.user_id = m.wrong_id SET t.user_id = m.correct_id;
UPDATE spendsmart_recurring_db.recurring t JOIN wrong_repair_ids m ON t.user_id = m.wrong_id SET t.user_id = m.correct_id;
UPDATE spendsmart_notification_db.notification t JOIN wrong_repair_ids m ON t.user_id = m.wrong_id SET t.user_id = m.correct_id, t.related_id = CASE WHEN t.related_type = 'USER' THEN m.correct_id ELSE t.related_id END;

UPDATE spendsmart_budget_db.budgets t JOIN user_id_repair_map_corrected m ON t.user_id = m.old_docker_user_id AND m.repaired_user_id <> m.old_docker_user_id AND t.created_at >= m.created_at SET t.user_id = m.repaired_user_id;
UPDATE spendsmart_category_db.categories t JOIN user_id_repair_map_corrected m ON t.user_id = m.old_docker_user_id AND m.repaired_user_id <> m.old_docker_user_id AND t.created_at >= m.created_at SET t.user_id = m.repaired_user_id;
UPDATE spendsmart_expense_db.expenses t JOIN user_id_repair_map_corrected m ON t.user_id = m.old_docker_user_id AND m.repaired_user_id <> m.old_docker_user_id AND t.created_at >= m.created_at SET t.user_id = m.repaired_user_id;
UPDATE spendsmart_income_db.income t JOIN user_id_repair_map_corrected m ON t.user_id = m.old_docker_user_id AND m.repaired_user_id <> m.old_docker_user_id AND t.created_at >= m.created_at SET t.user_id = m.repaired_user_id;
UPDATE spendsmart_payment_db.payments t JOIN user_id_repair_map_corrected m ON t.user_id = m.old_docker_user_id AND m.repaired_user_id <> m.old_docker_user_id AND t.created_at >= m.created_at SET t.user_id = m.repaired_user_id;
UPDATE spendsmart_recurring_db.recurring t JOIN user_id_repair_map_corrected m ON t.user_id = m.old_docker_user_id AND m.repaired_user_id <> m.old_docker_user_id AND t.start_date >= DATE(m.created_at) SET t.user_id = m.repaired_user_id;
UPDATE spendsmart_notification_db.notification t JOIN user_id_repair_map_corrected m ON t.user_id = m.old_docker_user_id AND m.repaired_user_id <> m.old_docker_user_id AND t.created_at >= m.created_at SET t.user_id = m.repaired_user_id, t.related_id = CASE WHEN t.related_type = 'USER' THEN m.repaired_user_id ELSE t.related_id END;

DELETE FROM users;
INSERT INTO users (monthly_budget, created_at, user_id, currency, email, full_name, password, status, timezone)
SELECT monthly_budget, created_at, user_id, currency, email, full_name, password, status, timezone
FROM old_users_restore
ORDER BY user_id;
SET @next_auto := (SELECT COALESCE(MAX(user_id), 0) + 1 FROM users);
SET @alter_sql := CONCAT('ALTER TABLE users AUTO_INCREMENT = ', @next_auto);
PREPARE stmt FROM @alter_sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

UPDATE spendsmart_notification_db.notification n
JOIN spendsmart_auth_db.users u ON LOWER(u.email) = LOWER(n.recipient_email)
SET n.user_id = u.user_id,
    n.related_id = CASE WHEN n.related_type = 'USER' THEN u.user_id ELSE n.related_id END
WHERE n.recipient_email IS NOT NULL;

UPDATE spendsmart_payment_db.payments p
JOIN spendsmart_auth_db.users u ON LOWER(u.email) = LOWER(p.payer_email)
SET p.user_id = u.user_id
WHERE p.payer_email IS NOT NULL;
