package com.spendsmart.auth;

import com.spendsmart.auth.config.RabbitMqConfig;
import com.spendsmart.auth.config.RestConfig;
import com.spendsmart.auth.controller.AuthController;
import com.spendsmart.auth.dto.*;
import com.spendsmart.auth.entity.User;
import com.spendsmart.auth.exception.ApiExceptionHandler;
import com.spendsmart.auth.messaging.NotificationPublisher;
import com.spendsmart.auth.security.JwtFilter;
import com.spendsmart.auth.security.JwtUtil;
import com.spendsmart.auth.service.AuthService;
import jakarta.servlet.ServletException;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AuthLayerCoverageTest {

    @Test
    void controllerDelegatesToService() {
        AuthService service = mock(AuthService.class);
        AuthController controller = new AuthController();
        ReflectionTestUtils.setField(controller, "authService", service);
        User user = User.builder().userId(1L).email("user@test.com").password("p").build();
        AuthRegisterRequest register = new AuthRegisterRequest();
        register.setEmail("user@test.com");
        register.setPassword("p");
        register.setOtp("123456");
        AuthLoginRequest login = new AuthLoginRequest();
        login.setEmail("user@test.com");
        login.setPassword("p");
        SendOtpRequest sendOtp = new SendOtpRequest();
        sendOtp.setEmail("user@test.com");
        VerifyOtpRequest verifyOtp = new VerifyOtpRequest();
        verifyOtp.setEmail("user@test.com");
        verifyOtp.setOtp("123456");
        ResetPasswordRequest reset = new ResetPasswordRequest();
        reset.setEmail("user@test.com");
        reset.setOtp("123456");
        reset.setNewPassword("new");
        UserPreferencesRequest prefs = new UserPreferencesRequest();
        when(service.register(any(User.class))).thenReturn(user);
        when(service.login("user@test.com", "p")).thenReturn("token");
        when(service.getUserById(1L)).thenReturn(user);
        when(service.getAllUsers()).thenReturn(List.of(user));
        when(service.updateUserStatus(1L, true)).thenReturn(user);
        when(service.updatePreferences(1L, prefs)).thenReturn(user);

        assertSame(user, controller.register(register));
        assertEquals("OTP sent successfully", controller.sendRegistrationOtp(sendOtp));
        assertEquals("OTP verified successfully", controller.verifyRegistrationOtp(verifyOtp));
        assertEquals("Password reset OTP sent successfully", controller.sendPasswordResetOtp(sendOtp));
        assertEquals("OTP verified successfully", controller.verifyPasswordResetOtp(verifyOtp));
        assertEquals("Password reset successfully", controller.resetPassword(reset));
        assertEquals("token", controller.login(login));
        assertSame(user, controller.getUser(1L));
        assertSame(user, controller.getInternalUser(1L));
        assertEquals(List.of(user), controller.getAllInternalUsers());
        assertSame(user, controller.updateInternalUserStatus(1L, true));
        controller.deleteInternalUser(1L);
        assertSame(user, controller.updatePreferences(1L, prefs));
        verify(service).deleteUser(1L);
    }

    @Test
    void jwtFilterAllowsPublicPathsAndRejectsInvalidRequests() throws ServletException, IOException {
        JwtUtil jwtUtil = new JwtUtil();
        JwtFilter filter = new JwtFilter();
        ReflectionTestUtils.setField(filter, "jwtUtil", jwtUtil);

        MockHttpServletRequest publicRequest = new MockHttpServletRequest("POST", "/auth/login");
        publicRequest.setServletPath("/auth/login");
        MockHttpServletResponse publicResponse = new MockHttpServletResponse();
        MockFilterChain publicChain = new MockFilterChain();
        filter.doFilter(publicRequest, publicResponse, publicChain);
        assertEquals(200, publicResponse.getStatus());

        MockHttpServletRequest missingHeader = new MockHttpServletRequest("GET", "/auth/users/1");
        MockHttpServletResponse unauthorized = new MockHttpServletResponse();
        filter.doFilter(missingHeader, unauthorized, new MockFilterChain());
        assertEquals(401, unauthorized.getStatus());

        MockHttpServletRequest valid = new MockHttpServletRequest("GET", "/auth/users/1");
        valid.addHeader("Authorization", "Bearer " + jwtUtil.generateToken("user@test.com"));
        MockHttpServletResponse validResponse = new MockHttpServletResponse();
        filter.doFilter(valid, validResponse, new MockFilterChain());
        assertEquals(200, validResponse.getStatus());
    }

    @Test
    void dtoEntityConfigMessagingAndExceptionClassesWork() {
        User user = User.builder().userId(1L).fullName("User").email("u").password("p").currency("INR").timezone("UTC").monthlyBudget(100.0).otp("123456").build();
        user.onCreate();
        assertNotNull(user.getCreatedAt());
        assertEquals("active", new User().getStatus());
        assertEquals("User", user.getFullName());
        assertEquals("INR", user.getCurrency());
        assertEquals("UTC", user.getTimezone());
        assertEquals(100.0, user.getMonthlyBudget());
        assertEquals("123456", user.getOtp());
        assertTrue(new RestConfig().restTemplate() instanceof RestTemplate);
        assertTrue(new RabbitMqConfig().jsonMessageConverter() instanceof Jackson2JsonMessageConverter);
        RecordingRabbitTemplate rabbitTemplate = new RecordingRabbitTemplate();
        NotificationPublisher publisher = new NotificationPublisher(rabbitTemplate, "exchange", "send");
        Map<String, Object> payload = Map.of("recipientId", 1L);
        publisher.publishNotification(payload);
        assertEquals("exchange", rabbitTemplate.exchange);
        assertEquals("send", rabbitTemplate.routingKey);
        assertSame(payload, rabbitTemplate.payload);
        var response = new ApiExceptionHandler().handleResponseStatusException(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Bad"));
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Bad", response.getBody().get("message"));

        AuthLoginRequest login = new AuthLoginRequest();
        login.setEmail("e"); login.setPassword("p");
        assertEquals("e", login.getEmail());
        AuthRegisterRequest register = new AuthRegisterRequest();
        register.setEmail("e"); register.setPassword("p"); register.setOtp("o"); register.setFullName("f");
        assertEquals("e", register.toUser().getEmail());
        UserPreferencesRequest prefs = new UserPreferencesRequest();
        prefs.setCurrency("INR"); prefs.setTimezone("UTC"); prefs.setFullName("Full"); prefs.setMonthlyBudget(10.0);
        assertEquals("INR", prefs.getCurrency());
    }

    private static class RecordingRabbitTemplate extends RabbitTemplate {
        private String exchange;
        private String routingKey;
        private Object payload;

        @Override
        public void convertAndSend(String exchange, String routingKey, Object object) {
            this.exchange = exchange;
            this.routingKey = routingKey;
            this.payload = object;
        }
    }
}
