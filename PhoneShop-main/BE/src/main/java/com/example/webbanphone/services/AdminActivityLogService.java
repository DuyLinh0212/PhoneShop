package com.example.webbanphone.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AdminActivityLogService {

    private static final Logger LOGGER = LoggerFactory.getLogger(AdminActivityLogService.class);
    private static final int DETAIL_LIMIT = 500;

    private final JdbcTemplate jdbcTemplate;

    public AdminActivityLogService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Async("backendTaskExecutor")
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void writeAsync(String action, String target, String detail) {
        try {
            jdbcTemplate.update(
                    "INSERT INTO activity_logs (actor, action, target, detail) VALUES (?, ?, ?, ?)",
                    "admin-api",
                    action,
                    target,
                    truncate(detail)
            );
        } catch (RuntimeException exception) {
            LOGGER.warn("Không thể ghi nhật ký quản trị bất đồng bộ", exception);
        }
    }

    private String truncate(String value) {
        if (value == null || value.length() <= DETAIL_LIMIT) {
            return value;
        }
        return value.substring(0, DETAIL_LIMIT);
    }
}
