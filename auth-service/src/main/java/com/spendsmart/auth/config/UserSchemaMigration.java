package com.spendsmart.auth.config;

import jakarta.annotation.PostConstruct;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class UserSchemaMigration {

    private final JdbcTemplate jdbcTemplate;

    public UserSchemaMigration(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @PostConstruct
    public void migrateUsersTable() {
        if (!tableExists("users")) {
            return;
        }

        boolean passwordExists = columnExists("users", "password");
        boolean passwordHashExists = columnExists("users", "password_hash");

        if (passwordHashExists && passwordExists) {
            jdbcTemplate.execute("ALTER TABLE users DROP COLUMN password");
            jdbcTemplate.execute("ALTER TABLE users CHANGE COLUMN password_hash password VARCHAR(255) NOT NULL");
        } else if (passwordHashExists) {
            jdbcTemplate.execute("ALTER TABLE users CHANGE COLUMN password_hash password VARCHAR(255) NOT NULL");
        } else if (!passwordExists) {
            jdbcTemplate.execute("ALTER TABLE users ADD COLUMN password VARCHAR(255) NOT NULL DEFAULT ''");
        }

        if (!columnExists("users", "status")) {
            jdbcTemplate.execute("ALTER TABLE users ADD COLUMN status VARCHAR(16) NULL");
        }

        if (columnExists("users", "is_active")) {
            jdbcTemplate.execute(
                    "UPDATE users SET status = CASE " +
                            "WHEN is_active IN (b'1', 1, true) THEN 'active' " +
                            "ELSE 'deactive' END " +
                            "WHERE status IS NULL OR status = ''"
            );
            jdbcTemplate.execute("ALTER TABLE users DROP COLUMN is_active");
        } else {
            jdbcTemplate.execute("UPDATE users SET status = 'active' WHERE status IS NULL OR status = ''");
        }

        dropColumnIfExists("users", "avatar_url");
        dropColumnIfExists("users", "provider");
        dropColumnIfExists("users", "bio");
        dropColumnIfExists("users", "premium_expires_at");
        dropColumnIfExists("users", "role");
        dropColumnIfExists("users", "subscription_type");
    }

    private boolean columnExists(String tableName, String columnName) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM information_schema.columns " +
                        "WHERE table_schema = DATABASE() AND table_name = ? AND column_name = ?",
                Integer.class,
                tableName,
                columnName
        );
        return count != null && count > 0;
    }

    private boolean tableExists(String tableName) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM information_schema.tables " +
                        "WHERE table_schema = DATABASE() AND table_name = ?",
                Integer.class,
                tableName
        );
        return count != null && count > 0;
    }

    private void dropColumnIfExists(String tableName, String columnName) {
        if (columnExists(tableName, columnName)) {
            jdbcTemplate.execute("ALTER TABLE " + tableName + " DROP COLUMN " + columnName);
        }
    }
}
