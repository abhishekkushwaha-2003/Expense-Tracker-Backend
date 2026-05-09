package com.spendsmart.admin_service.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.spendsmart.admin_service.dto.AdminBroadcastRequest;
import com.spendsmart.admin_service.dto.AdminLoginRequest;
import com.spendsmart.admin_service.messaging.NotificationPublisher;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpMethod;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

@ExtendWith(MockitoExtension.class)
class AdminServiceImplTest {

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private NotificationPublisher notificationPublisher;

    private AdminServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new AdminServiceImpl(restTemplate, notificationPublisher, true);
        ReflectionTestUtils.setField(service, "adminEmail", "admin@spendsmart.com");
        ReflectionTestUtils.setField(service, "adminPassword", "secret");
        ReflectionTestUtils.setField(service, "adminFullName", "Spend Smart Admin");
        ReflectionTestUtils.setField(service, "authBaseUrl", "http://auth-service/auth");
        ReflectionTestUtils.setField(service, "expenseBaseUrl", "http://expense-service/expenses");
        ReflectionTestUtils.setField(service, "incomeBaseUrl", "http://income-service/incomes");
        ReflectionTestUtils.setField(service, "categoryBaseUrl", "http://category-service/categories");
        ReflectionTestUtils.setField(service, "notificationBaseUrl", "http://notification-service/notifications");
    }

    @Test
    void loginReturnsTokenForConfiguredAdmin() {
        AdminLoginRequest request = new AdminLoginRequest();
        request.setEmail("admin@spendsmart.com");
        request.setPassword("secret");

        when(restTemplate.getForEntity(eq("http://auth-service/auth/internal/users"), eq(List.class)))
                .thenReturn(ResponseEntity.ok(List.of()));

        var response = service.login(request);

        assertEquals("admin@spendsmart.com", response.getEmail());
        assertEquals("Spend Smart Admin", response.getFullName());
        assertEquals("admin", response.getRole());
        assertFalse(response.getToken().isBlank());
    }

    @Test
    void getUsersAggregatesExpenseAndIncomeTotals() {
        String token = loginAndGetBearerToken();
        Map<String, Object> user = Map.of(
                "userId", 7,
                "email", "user@example.com",
                "fullName", "User One",
                "status", "active"
        );

        when(restTemplate.getForEntity(eq("http://auth-service/auth/internal/users"), eq(List.class)))
                .thenReturn(ResponseEntity.ok(List.of(user)));
        when(restTemplate.getForEntity(eq("http://expense-service/expenses/user/7"), eq(List.class)))
                .thenReturn(ResponseEntity.ok(List.of(Map.of("amount", 45.5), Map.of("amount", 4.5))));
        when(restTemplate.getForEntity(eq("http://income-service/incomes/user/7"), eq(List.class)))
                .thenReturn(ResponseEntity.ok(List.of(Map.of("amount", 100.0))));

        List<Map<String, Object>> result = service.getUsers(token);

        assertEquals(1, result.size());
        assertEquals(2, result.get(0).get("expenseCount"));
        assertEquals(1, result.get(0).get("incomeCount"));
        assertEquals(50.0, (Double) result.get(0).get("expenseTotal"));
        assertEquals(100.0, (Double) result.get(0).get("incomeTotal"));
        assertEquals(50.0, (Double) result.get(0).get("netBalance"));
    }

    @Test
    void sendBroadcastPublishesAsyncNotificationsForActiveUsers() {
        String token = loginAndGetBearerToken();
        Map<String, Object> activeUser = Map.of(
                "userId", 1,
                "email", "active@example.com",
                "status", "active"
        );
        Map<String, Object> inactiveUser = Map.of(
                "userId", 2,
                "email", "inactive@example.com",
                "status", "deactive"
        );

        when(restTemplate.getForEntity(eq("http://auth-service/auth/internal/users"), eq(List.class)))
                .thenReturn(ResponseEntity.ok(List.of(activeUser, inactiveUser)));

        AdminBroadcastRequest request = new AdminBroadcastRequest();
        request.setTitle("Maintenance");
        request.setMessage("Scheduled update");
        request.setEmailEnabled(true);

        Map<String, Object> response = service.sendBroadcast(token, request);
        ArgumentCaptor<Map<String, Object>> payloadCaptor = ArgumentCaptor.forClass(Map.class);

        verify(notificationPublisher).publishNotification(payloadCaptor.capture());
        verify(restTemplate, never()).postForObject(any(), any(), eq(Object.class));

        assertEquals(1, response.get("sentCount"));
        assertEquals("active@example.com", payloadCaptor.getValue().get("recipientEmail"));
        assertEquals("Maintenance", payloadCaptor.getValue().get("title"));
    }

    @Test
    void getUsersRejectsInvalidToken() {
        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> service.getUsers("Bearer invalid")
        );

        assertTrue(exception.getReason().contains("Invalid admin session"));
    }

    @Test
    void loginRejectsWrongEmailWrongPasswordAndInactiveAdmin() {
        AdminLoginRequest wrongEmail = new AdminLoginRequest();
        wrongEmail.setEmail("other@spendsmart.com");
        wrongEmail.setPassword("secret");
        assertThrows(ResponseStatusException.class, () -> service.login(wrongEmail));

        AdminLoginRequest wrongPassword = new AdminLoginRequest();
        wrongPassword.setEmail("admin@spendsmart.com");
        wrongPassword.setPassword("bad");
        when(restTemplate.getForEntity(eq("http://auth-service/auth/internal/users"), eq(List.class)))
                .thenReturn(ResponseEntity.ok(List.of()));
        assertThrows(ResponseStatusException.class, () -> service.login(wrongPassword));

        AdminLoginRequest inactive = new AdminLoginRequest();
        inactive.setEmail("admin@spendsmart.com");
        inactive.setPassword("secret");
        when(restTemplate.getForEntity(eq("http://auth-service/auth/internal/users"), eq(List.class)))
                .thenReturn(ResponseEntity.ok(List.of(Map.of(
                        "userId", 99,
                        "email", "admin@spendsmart.com",
                        "password", "secret",
                        "status", "deactive"
                ))));
        assertThrows(ResponseStatusException.class, () -> service.login(inactive));
    }

    @Test
    void getOverviewAggregatesPlatformMetrics() {
        String token = loginAndGetBearerToken();
        Map<String, Object> user = Map.of("userId", 7, "email", "user@example.com", "fullName", "User One", "status", "active");
        when(restTemplate.getForEntity(eq("http://auth-service/auth/internal/users"), eq(List.class)))
                .thenReturn(ResponseEntity.ok(List.of(user)));
        when(restTemplate.getForEntity(eq("http://expense-service/expenses/user/7"), eq(List.class)))
                .thenReturn(ResponseEntity.ok(List.of(Map.of("expenseId", 1, "title", "Food", "amount", 60.0, "date", "2026-05-01T10:00:00"))));
        when(restTemplate.getForEntity(eq("http://income-service/incomes/user/7"), eq(List.class)))
                .thenReturn(ResponseEntity.ok(List.of(Map.of("incomeId", 2, "source", "Salary", "amount", 200.0, "date", "2026-05"))));
        when(restTemplate.getForEntity(eq("http://category-service/categories/user/7"), eq(List.class)))
                .thenReturn(ResponseEntity.ok(List.of(Map.of("name", "Food"))));

        Map<String, Object> overview = service.getOverview(token);

        assertEquals(1, overview.get("totalUsers"));
        assertEquals(1L, overview.get("activeUsers"));
        assertEquals(2, overview.get("totalTransactions"));
        assertEquals(60.0, (Double) overview.get("totalExpense"));
        assertEquals(200.0, (Double) overview.get("totalIncome"));
    }

    @Test
    void updateUserStatusAndDeleteUserCallAuthService() {
        String token = loginAndGetBearerToken();
        when(restTemplate.exchange(eq("http://auth-service/auth/internal/users/7/status?active=false"), eq(HttpMethod.PUT), eq(null), eq(Map.class)))
                .thenReturn(ResponseEntity.ok(Map.of("userId", 7, "status", "deactive")));

        Map<String, Object> updated = service.updateUserStatus(token, 7L, false);
        service.deleteUser(token, 7L);

        assertEquals("deactive", updated.get("status"));
        verify(restTemplate).delete("http://auth-service/auth/internal/users/7");
    }

    @Test
    void getTransactionsBuildsSortedExpenseAndIncomeRows() {
        String token = loginAndGetBearerToken();
        Map<String, Object> user = Map.of("userId", 7, "email", "user@example.com", "fullName", "User One", "status", "active");
        when(restTemplate.getForEntity(eq("http://auth-service/auth/internal/users"), eq(List.class)))
                .thenReturn(ResponseEntity.ok(List.of(user)));
        when(restTemplate.getForEntity(eq("http://expense-service/expenses/user/7"), eq(List.class)))
                .thenReturn(ResponseEntity.ok(List.of(Map.of("expenseId", 1, "title", "Food", "amount", "60.0", "date", "bad-date"))));
        when(restTemplate.getForEntity(eq("http://income-service/incomes/user/7"), eq(List.class)))
                .thenReturn(ResponseEntity.ok(List.of(Map.of("incomeId", 2, "source", "Salary", "amount", 200.0, "date", "2026-05"))));

        List<Map<String, Object>> transactions = service.getTransactions(token);

        assertEquals(2, transactions.size());
        assertEquals("income", transactions.get(0).get("kind"));
        assertEquals("expense", transactions.get(1).get("kind"));
    }

    @Test
    void sendBroadcastFallsBackToBulkEndpointWhenAsyncDisabled() {
        service = new AdminServiceImpl(restTemplate, notificationPublisher, false);
        setUpFields(service);
        String token = loginAndGetBearerToken();
        when(restTemplate.getForEntity(eq("http://auth-service/auth/internal/users"), eq(List.class)))
                .thenReturn(ResponseEntity.ok(List.of(Map.of("userId", 1, "email", "active@example.com", "status", "active"))));
        AdminBroadcastRequest request = new AdminBroadcastRequest();
        request.setTitle("Maintenance");
        request.setMessage("Scheduled update");

        Map<String, Object> response = service.sendBroadcast(token, request);

        assertEquals(1, response.get("sentCount"));
        verify(restTemplate).postForObject(eq("http://notification-service/notifications/bulk"), any(), eq(Object.class));
    }

    @Test
    void getAuditLogsReturnsNewestFirstAndGetListHandlesRemoteFailures() {
        String token = loginAndGetBearerToken();
        when(restTemplate.getForEntity(eq("http://auth-service/auth/internal/users"), eq(List.class)))
                .thenThrow(new org.springframework.web.client.RestClientException("down"));

        assertTrue(service.getUsers(token).isEmpty());
        assertFalse(service.getAuditLogs(token).isEmpty());
    }

    private String loginAndGetBearerToken() {
        AdminLoginRequest request = new AdminLoginRequest();
        request.setEmail("admin@spendsmart.com");
        request.setPassword("secret");

        when(restTemplate.getForEntity(eq("http://auth-service/auth/internal/users"), eq(List.class)))
                .thenReturn(ResponseEntity.ok(List.of()));

        return "Bearer " + service.login(request).getToken();
    }

    private void setUpFields(AdminServiceImpl target) {
        ReflectionTestUtils.setField(target, "adminEmail", "admin@spendsmart.com");
        ReflectionTestUtils.setField(target, "adminPassword", "secret");
        ReflectionTestUtils.setField(target, "adminFullName", "Spend Smart Admin");
        ReflectionTestUtils.setField(target, "authBaseUrl", "http://auth-service/auth");
        ReflectionTestUtils.setField(target, "expenseBaseUrl", "http://expense-service/expenses");
        ReflectionTestUtils.setField(target, "incomeBaseUrl", "http://income-service/incomes");
        ReflectionTestUtils.setField(target, "categoryBaseUrl", "http://category-service/categories");
        ReflectionTestUtils.setField(target, "notificationBaseUrl", "http://notification-service/notifications");
    }
}
