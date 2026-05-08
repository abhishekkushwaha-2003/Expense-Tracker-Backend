package com.spendsmart.admin_service.service;

import com.spendsmart.admin_service.dto.AdminAuditLog;
import com.spendsmart.admin_service.dto.AdminBroadcastRequest;
import com.spendsmart.admin_service.dto.AdminLoginRequest;
import com.spendsmart.admin_service.dto.AdminLoginResponse;
import java.util.List;
import java.util.Map;

public interface AdminService {

    AdminLoginResponse login(AdminLoginRequest request);

    Map<String, Object> getOverview(String token);

    List<Map<String, Object>> getUsers(String token);

    Map<String, Object> updateUserStatus(String token, Long userId, boolean active);

    void deleteUser(String token, Long userId);

    List<Map<String, Object>> getTransactions(String token);

    Map<String, Object> sendBroadcast(String token, AdminBroadcastRequest request);

    List<AdminAuditLog> getAuditLogs(String token);
}
