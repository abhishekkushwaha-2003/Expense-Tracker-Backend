package com.spendsmart.admin_service;

import com.spendsmart.admin_service.config.RabbitMqConfig;
import com.spendsmart.admin_service.config.RestConfig;
import com.spendsmart.admin_service.controller.AdminController;
import com.spendsmart.admin_service.dto.*;
import com.spendsmart.admin_service.exception.ApiExceptionHandler;
import com.spendsmart.admin_service.messaging.NotificationPublisher;
import com.spendsmart.admin_service.service.AdminService;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AdminLayerCoverageTest {

    @Test
    void controllerDelegatesToService() {
        AdminService service = mock(AdminService.class);
        AdminController controller = new AdminController(service);
        AdminLoginRequest loginRequest = new AdminLoginRequest();
        AdminLoginResponse loginResponse = AdminLoginResponse.builder().token("t").email("admin@test.com").fullName("Admin").role("admin").build();
        AdminUserStatusRequest statusRequest = new AdminUserStatusRequest();
        statusRequest.setActive(false);
        AdminBroadcastRequest broadcastRequest = new AdminBroadcastRequest();
        AdminAuditLog auditLog = AdminAuditLog.builder().timestamp(LocalDateTime.now()).action("VIEW").build();
        when(service.login(loginRequest)).thenReturn(loginResponse);
        when(service.getOverview("Bearer t")).thenReturn(Map.of("totalUsers", 1));
        when(service.getUsers("Bearer t")).thenReturn(List.of(Map.of("userId", 1)));
        when(service.updateUserStatus("Bearer t", 1L, false)).thenReturn(Map.of("status", "deactive"));
        when(service.getTransactions("Bearer t")).thenReturn(List.of(Map.of("kind", "expense")));
        when(service.sendBroadcast("Bearer t", broadcastRequest)).thenReturn(Map.of("sentCount", 1));
        when(service.getAuditLogs("Bearer t")).thenReturn(List.of(auditLog));

        assertSame(loginResponse, controller.login(loginRequest));
        assertEquals(1, controller.getOverview("Bearer t").get("totalUsers"));
        assertEquals(1, controller.getUsers("Bearer t").size());
        assertEquals("deactive", controller.updateUserStatus("Bearer t", 1L, statusRequest).get("status"));
        controller.deleteUser("Bearer t", 1L);
        assertEquals(1, controller.getTransactions("Bearer t").size());
        assertEquals(1, controller.sendBroadcast("Bearer t", broadcastRequest).get("sentCount"));
        assertEquals(List.of(auditLog), controller.getAuditLogs("Bearer t"));
        verify(service).deleteUser("Bearer t", 1L);
    }

    @Test
    void dtoConfigMessagingAndExceptionClassesWork() {
        assertTrue(new RestConfig().restTemplate() instanceof RestTemplate);
        assertTrue(new RabbitMqConfig().jsonMessageConverter() instanceof Jackson2JsonMessageConverter);
        RabbitTemplate rabbitTemplate = mock(RabbitTemplate.class);
        NotificationPublisher publisher = new NotificationPublisher(rabbitTemplate, "exchange", "send");
        Map<String, Object> payload = Map.of("title", "Hello");
        publisher.publishNotification(payload);
        verify(rabbitTemplate).convertAndSend("exchange", "send", payload);

        AdminLoginRequest login = new AdminLoginRequest();
        login.setEmail("admin@test.com");
        login.setPassword("secret");
        assertEquals("admin@test.com", login.getEmail());
        assertEquals("secret", login.getPassword());
        AdminBroadcastRequest broadcast = new AdminBroadcastRequest();
        broadcast.setTitle("T");
        broadcast.setMessage("M");
        broadcast.setEmailEnabled(true);
        assertEquals("T", broadcast.getTitle());
        assertTrue(broadcast.isEmailEnabled());
        AdminAuditLog log = AdminAuditLog.builder().actorEmail("a").targetId("1").details("d").build();
        assertEquals("a", log.getActorEmail());
        assertEquals("1", log.getTargetId());
        assertEquals("d", log.getDetails());

        var response = new ApiExceptionHandler().handleResponseStatusException(new ResponseStatusException(HttpStatus.FORBIDDEN, "Denied"));
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertEquals("Denied", response.getBody().get("message"));
    }
}