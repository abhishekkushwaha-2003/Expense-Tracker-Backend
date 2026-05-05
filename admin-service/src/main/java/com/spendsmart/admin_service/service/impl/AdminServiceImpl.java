package com.spendsmart.admin_service.service.impl;

import com.spendsmart.admin_service.dto.AdminAuditLog;
import com.spendsmart.admin_service.dto.AdminBroadcastRequest;
import com.spendsmart.admin_service.dto.AdminLoginRequest;
import com.spendsmart.admin_service.dto.AdminLoginResponse;
import com.spendsmart.admin_service.messaging.NotificationPublisher;
import com.spendsmart.admin_service.service.AdminService;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AdminServiceImpl implements AdminService {

    private final RestTemplate restTemplate;
    private final NotificationPublisher notificationPublisher;
    private final boolean asyncNotificationEnabled;
    private final Map<String, String> activeSessions = new ConcurrentHashMap<>();
    private final List<AdminAuditLog> auditLogs = new CopyOnWriteArrayList<>();

    @Value("${admin.email}")
    private String adminEmail;

    @Value("${admin.password}")
    private String adminPassword;

    @Value("${admin.full-name}")
    private String adminFullName;

    @Value("${services.auth-base-url}")
    private String authBaseUrl;

    @Value("${services.expense-base-url}")
    private String expenseBaseUrl;

    @Value("${services.income-base-url}")
    private String incomeBaseUrl;

    @Value("${services.category-base-url}")
    private String categoryBaseUrl;

    @Value("${services.notification-base-url}")
    private String notificationBaseUrl;

    public AdminServiceImpl(
            RestTemplate restTemplate,
            @org.springframework.beans.factory.annotation.Autowired(required = false) NotificationPublisher notificationPublisher,
            @Value("${app.messaging.notification.async-enabled:false}") boolean asyncNotificationEnabled
    ) {
        this.restTemplate = restTemplate;
        this.notificationPublisher = notificationPublisher;
        this.asyncNotificationEnabled = asyncNotificationEnabled;
    }

    @Override
    public AdminLoginResponse login(AdminLoginRequest request) {
        if (!adminEmail.equalsIgnoreCase(request.getEmail())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid admin credentials");
        }

        Map<String, Object> adminUser = findAdminUser();

        if (!adminUser.isEmpty() && !isUserActive(adminUser)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Admin account is inactive");
        }

        String storedPassword = adminUser.isEmpty() ? null : Objects.toString(adminUser.get("password"), null);
        boolean matchesConfiguredPassword = adminPassword.equals(request.getPassword());
        boolean matchesStoredPassword = storedPassword != null && storedPassword.equals(request.getPassword());
        if (!matchesConfiguredPassword && !matchesStoredPassword) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid admin credentials");
        }

        String token = UUID.randomUUID().toString();
        activeSessions.put(token, adminEmail);
        addAudit(adminEmail, "LOGIN", "ADMIN_SESSION", token, "Admin signed in");

        return AdminLoginResponse.builder()
                .token(token)
                .email(adminEmail)
                .fullName(adminFullName)
                .role("admin")
                .build();
    }

    @Override
    public Map<String, Object> getOverview(String token) {
        String actor = requireAdmin(token);
        List<Map<String, Object>> users = getRawUsers();
        List<Map<String, Object>> transactions = buildTransactions(users);

        double totalExpense = transactions.stream()
                .filter(item -> "expense".equals(item.get("kind")))
                .mapToDouble(item -> toDouble(item.get("amount")))
                .sum();
        double totalIncome = transactions.stream()
                .filter(item -> "income".equals(item.get("kind")))
                .mapToDouble(item -> toDouble(item.get("amount")))
                .sum();

        Map<String, Long> categoryUsage = new LinkedHashMap<>();
        for (Map<String, Object> user : users) {
            Long userId = toLong(user.get("userId"));
            List<Map<String, Object>> categories = getList(categoryBaseUrl + "/user/" + userId);
            for (Map<String, Object> category : categories) {
                String name = String.valueOf(category.getOrDefault("name", "Uncategorised"));
                categoryUsage.put(name, categoryUsage.getOrDefault(name, 0L) + 1);
            }
        }

        List<Map<String, Object>> topUsers = getUsers(token).stream()
                .sorted(Comparator.comparingDouble(item -> -toDouble(item.get("expenseTotal"))))
                .limit(5)
                .toList();

        double averageMonthlySpendPerUser = users.isEmpty() ? 0.0 : totalExpense / users.size();

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("totalUsers", users.size());
        response.put("activeUsers", users.stream().filter(this::isUserActive).count());
        response.put("suspendedUsers", users.stream().filter(user -> !isUserActive(user)).count());
        response.put("totalTransactions", transactions.size());
        response.put("totalExpense", totalExpense);
        response.put("totalIncome", totalIncome);
        response.put("averageMonthlySpendPerUser", averageMonthlySpendPerUser);
        response.put("mostUsedCategories", categoryUsage.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(5)
                .map(entry -> Map.of("name", entry.getKey(), "count", entry.getValue()))
                .toList());
        response.put("topSpendingUsers", topUsers);

        addAudit(actor, "VIEW", "ADMIN_OVERVIEW", "overview", "Viewed admin overview");
        return response;
    }

    @Override
    public List<Map<String, Object>> getUsers(String token) {
        String actor = requireAdmin(token);
        List<Map<String, Object>> users = getRawUsers();
        List<Map<String, Object>> response = new ArrayList<>();

        for (Map<String, Object> user : users) {
            Long userId = toLong(user.get("userId"));
            List<Map<String, Object>> expenses = getList(expenseBaseUrl + "/user/" + userId);
            List<Map<String, Object>> incomes = getList(incomeBaseUrl + "/user/" + userId);

            double expenseTotal = expenses.stream().mapToDouble(item -> toDouble(item.get("amount"))).sum();
            double incomeTotal = incomes.stream().mapToDouble(item -> toDouble(item.get("amount"))).sum();

            Map<String, Object> item = new LinkedHashMap<>(user);
            item.put("expenseCount", expenses.size());
            item.put("incomeCount", incomes.size());
            item.put("expenseTotal", expenseTotal);
            item.put("incomeTotal", incomeTotal);
            item.put("netBalance", incomeTotal - expenseTotal);
            response.add(item);
        }

        addAudit(actor, "VIEW", "USER_LIST", "all", "Viewed all users");
        return response;
    }

    @Override
    public Map<String, Object> updateUserStatus(String token, Long userId, boolean active) {
        String actor = requireAdmin(token);
        String url = authBaseUrl + "/internal/users/" + userId + "/status?active=" + active;
        @SuppressWarnings("unchecked")
        Map<String, Object> updatedUser = restTemplate.exchange(url, org.springframework.http.HttpMethod.PUT, null, Map.class).getBody();
        addAudit(actor, active ? "REACTIVATE" : "SUSPEND", "USER", String.valueOf(userId), "Updated user status");
        return updatedUser == null ? Map.of() : updatedUser;
    }

    @Override
    public void deleteUser(String token, Long userId) {
        String actor = requireAdmin(token);
        restTemplate.delete(authBaseUrl + "/internal/users/" + userId);
        addAudit(actor, "DELETE", "USER", String.valueOf(userId), "Deleted user account");
    }

    @Override
    public List<Map<String, Object>> getTransactions(String token) {
        String actor = requireAdmin(token);
        List<Map<String, Object>> transactions = buildTransactions(getRawUsers());
        addAudit(actor, "VIEW", "TRANSACTIONS", "all", "Viewed platform transactions");
        return transactions;
    }

    @Override
    public Map<String, Object> sendBroadcast(String token, AdminBroadcastRequest request) {
        String actor = requireAdmin(token);
        List<Map<String, Object>> users = getRawUsers().stream()
                .filter(this::isUserActive)
                .toList();

        List<Map<String, Object>> notifications = new ArrayList<>();
        for (Map<String, Object> user : users) {
            Map<String, Object> notification = new LinkedHashMap<>();
            notification.put("recipientId", toLong(user.get("userId")));
            notification.put("recipientEmail", user.get("email"));
            notification.put("type", "SYSTEM");
            notification.put("severity", "INFO");
            notification.put("title", request.getTitle());
            notification.put("message", request.getMessage());
            notification.put("relatedId", null);
            notification.put("relatedType", "ADMIN_BROADCAST");
            notification.put("emailEnabled", request.isEmailEnabled());
            notifications.add(notification);
        }

        if (asyncNotificationEnabled && notificationPublisher != null) {
            notifications.forEach(notificationPublisher::publishNotification);
        } else {
            Map<String, Object> payload = Map.of("notifications", notifications);
            restTemplate.postForObject(notificationBaseUrl + "/bulk", payload, Object.class);
        }

        addAudit(actor, "BROADCAST", "NOTIFICATION", String.valueOf(notifications.size()), "Sent broadcast to active users");
        return Map.of("sentCount", notifications.size(), "status", "queued");
    }

    @Override
    public List<AdminAuditLog> getAuditLogs(String token) {
        String actor = requireAdmin(token);
        addAudit(actor, "VIEW", "AUDIT_LOG", "all", "Viewed audit logs");
        return auditLogs.stream()
                .sorted(Comparator.comparing(AdminAuditLog::getTimestamp).reversed())
                .toList();
    }

    private String requireAdmin(String authorizationHeader) {
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Admin token is required");
        }

        String token = authorizationHeader.substring(7);
        String actor = activeSessions.get(token);
        if (actor == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid admin session");
        }
        return actor;
    }

    private List<Map<String, Object>> buildTransactions(List<Map<String, Object>> users) {
        List<Map<String, Object>> transactions = new ArrayList<>();

        for (Map<String, Object> user : users) {
            Long userId = toLong(user.get("userId"));
            String email = String.valueOf(user.getOrDefault("email", ""));
            String fullName = String.valueOf(user.getOrDefault("fullName", email));

            for (Map<String, Object> expense : getList(expenseBaseUrl + "/user/" + userId)) {
                Map<String, Object> item = new LinkedHashMap<>();
                item.put("id", expense.get("expenseId"));
                item.put("kind", "expense");
                item.put("userId", userId);
                item.put("userName", fullName);
                item.put("email", email);
                item.put("title", expense.get("title"));
                item.put("amount", toDouble(expense.get("amount")));
                item.put("currency", expense.getOrDefault("currency", "INR"));
                item.put("date", expense.get("date"));
                item.put("notes", expense.get("notes"));
                transactions.add(item);
            }

            for (Map<String, Object> income : getList(incomeBaseUrl + "/user/" + userId)) {
                Map<String, Object> item = new LinkedHashMap<>();
                item.put("id", income.get("incomeId"));
                item.put("kind", "income");
                item.put("userId", userId);
                item.put("userName", fullName);
                item.put("email", email);
                item.put("title", income.get("source"));
                item.put("amount", toDouble(income.get("amount")));
                item.put("currency", income.getOrDefault("currency", "INR"));
                item.put("date", income.get("date"));
                item.put("notes", income.get("notes"));
                transactions.add(item);
            }
        }

        transactions.sort(Comparator.comparing(this::extractDate).reversed());
        return transactions;
    }

    private List<Map<String, Object>> getRawUsers() {
        return getList(authBaseUrl + "/internal/users");
    }

    private Map<String, Object> findAdminUser() {
        try {
            return getRawUsers().stream()
                    .filter(user -> adminEmail.equalsIgnoreCase(String.valueOf(user.get("email"))))
                    .findFirst()
                    .orElse(Map.of());
        } catch (Exception ignored) {
            return Map.of();
        }
    }

    private boolean isUserActive(Map<String, Object> user) {
        return "active".equalsIgnoreCase(String.valueOf(user.getOrDefault("status", "")));
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> getList(String url) {
        ResponseEntity<List> response = restTemplate.getForEntity(url, List.class);
        return response.getBody() == null ? List.of() : response.getBody();
    }

    private void addAudit(String actorEmail, String action, String targetType, String targetId, String details) {
        auditLogs.add(AdminAuditLog.builder()
                .timestamp(LocalDateTime.now())
                .actorEmail(actorEmail)
                .action(action)
                .targetType(targetType)
                .targetId(targetId)
                .details(details)
                .build());
    }

    private double toDouble(Object value) {
        if (value == null) {
            return 0.0;
        }
        return Double.parseDouble(String.valueOf(value));
    }

    private Long toLong(Object value) {
        if (value == null) {
            return null;
        }
        return Long.parseLong(String.valueOf(value));
    }

    private LocalDateTime extractDate(Map<String, Object> item) {
        Object raw = item.get("date");
        if (raw == null) {
            return LocalDateTime.MIN;
        }
        try {
            return LocalDateTime.parse(String.valueOf(raw));
        } catch (DateTimeParseException ignored) {
            try {
                return YearMonth.parse(String.valueOf(raw)).atDay(1).atStartOfDay();
            } catch (DateTimeParseException ignoredAgain) {
                return LocalDateTime.MIN;
            }
        }
    }
}
