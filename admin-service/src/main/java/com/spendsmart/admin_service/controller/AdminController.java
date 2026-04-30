package com.spendsmart.admin_service.controller;

import com.spendsmart.admin_service.dto.AdminAuditLog;
import com.spendsmart.admin_service.dto.AdminBroadcastRequest;
import com.spendsmart.admin_service.dto.AdminLoginRequest;
import com.spendsmart.admin_service.dto.AdminLoginResponse;
import com.spendsmart.admin_service.dto.AdminUserStatusRequest;
import com.spendsmart.admin_service.service.AdminService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin")
public class AdminController {

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    @PostMapping("/login")
    public AdminLoginResponse login(@Valid @RequestBody AdminLoginRequest request) {
        return adminService.login(request);
    }

    @GetMapping("/overview")
    public Map<String, Object> getOverview(@RequestHeader("Authorization") String authorization) {
        return adminService.getOverview(authorization);
    }

    @GetMapping("/users")
    public List<Map<String, Object>> getUsers(@RequestHeader("Authorization") String authorization) {
        return adminService.getUsers(authorization);
    }

    @PutMapping("/users/{userId}/status")
    public Map<String, Object> updateUserStatus(
            @RequestHeader("Authorization") String authorization,
            @PathVariable Long userId,
            @Valid @RequestBody AdminUserStatusRequest request
    ) {
        return adminService.updateUserStatus(authorization, userId, request.isActive());
    }

    @DeleteMapping("/users/{userId}")
    public void deleteUser(@RequestHeader("Authorization") String authorization, @PathVariable Long userId) {
        adminService.deleteUser(authorization, userId);
    }

    @GetMapping("/transactions")
    public List<Map<String, Object>> getTransactions(@RequestHeader("Authorization") String authorization) {
        return adminService.getTransactions(authorization);
    }

    @PostMapping("/broadcast")
    public Map<String, Object> sendBroadcast(
            @RequestHeader("Authorization") String authorization,
            @Valid @RequestBody AdminBroadcastRequest request
    ) {
        return adminService.sendBroadcast(authorization, request);
    }

    @GetMapping("/audit-logs")
    public List<AdminAuditLog> getAuditLogs(@RequestHeader("Authorization") String authorization) {
        return adminService.getAuditLogs(authorization);
    }
}
