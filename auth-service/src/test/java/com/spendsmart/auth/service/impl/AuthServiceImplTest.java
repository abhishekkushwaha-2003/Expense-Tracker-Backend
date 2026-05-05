package com.spendsmart.auth.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.spendsmart.auth.dto.UserPreferencesRequest;
import com.spendsmart.auth.entity.User;
import com.spendsmart.auth.repository.UserRepository;
import com.spendsmart.auth.security.JwtUtil;
import com.spendsmart.auth.service.OtpService;
import java.net.URI;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.client.support.HttpRequestWrapper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.client.RequestCallback;
import org.springframework.web.client.ResponseExtractor;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private OtpService otpService;

    @Mock
    private PasswordEncoder passwordEncoder;

    private AuthServiceImpl service;
    private JwtUtil jwtUtil;
    private RecordingRestTemplate restTemplate;

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil();
        restTemplate = new RecordingRestTemplate();
        service = new AuthServiceImpl(
                userRepository,
                jwtUtil,
                restTemplate,
                otpService,
                passwordEncoder,
                null,
                false
        );
    }

    @Test
    void registerSetsDefaultsAndPostsWelcomeNotification() {
        User input = User.builder()
                .email("new@example.com")
                .password("plain")
                .otp("123456")
                .fullName("New User")
                .build();

        User saved = User.builder()
                .userId(99L)
                .email("new@example.com")
                .password("encoded")
                .fullName("New User")
                .currency("INR")
                .timezone("Asia/Kolkata")
                .status("active")
                .build();

        when(userRepository.existsByEmail("new@example.com")).thenReturn(false);
        when(passwordEncoder.encode("plain")).thenReturn("encoded");
        when(userRepository.save(any(User.class))).thenReturn(saved);

        User result = service.register(input);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        verify(otpService).verifyRegistrationOtp("new@example.com", "123456");

        assertEquals(saved, result);
        assertEquals("encoded", userCaptor.getValue().getPassword());
        assertEquals("INR", userCaptor.getValue().getCurrency());
        assertEquals("Asia/Kolkata", userCaptor.getValue().getTimezone());
        assertEquals("active", userCaptor.getValue().getStatus());
        assertEquals("http://NOTIFICATION-SERVICE/notifications/send", restTemplate.lastUrl);
        assertNotNull(restTemplate.lastPayload);
    }

    @Test
    void loginReturnsGeneratedTokenForActiveUser() {
        User user = User.builder()
                .userId(5L)
                .email("user@example.com")
                .password("encoded")
                .status("active")
                .build();

        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("secret", "encoded")).thenReturn(true);

        String token = service.login("user@example.com", "secret");

        assertTrue(jwtUtil.validateToken(token));
        assertEquals("user@example.com", jwtUtil.extractEmail(token));
    }

    @Test
    void updatePreferencesRejectsNegativeMonthlyBudget() {
        User user = User.builder()
                .userId(3L)
                .email("user@example.com")
                .status("active")
                .build();
        UserPreferencesRequest request = new UserPreferencesRequest();
        request.setMonthlyBudget(-50.0);

        when(userRepository.findByUserId(3L)).thenReturn(Optional.of(user));

        assertThrows(ResponseStatusException.class, () -> service.updatePreferences(3L, request));
    }

    @Test
    void updateUserStatusPersistsMappedState() {
        User user = User.builder().userId(4L).status("active").build();
        when(userRepository.findByUserId(4L)).thenReturn(Optional.of(user));
        when(userRepository.save(user)).thenReturn(user);

        User updated = service.updateUserStatus(4L, false);

        assertEquals("deactive", updated.getStatus());
    }

    private static final class RecordingRestTemplate extends RestTemplate {
        private String lastUrl;
        private Object lastPayload;

        @Override
        public <T> T postForObject(String url, Object request, Class<T> responseType, Object... uriVariables) {
            this.lastUrl = url;
            this.lastPayload = request;
            return null;
        }

        @Override
        public <T> T execute(String url, HttpMethod method, RequestCallback requestCallback, ResponseExtractor<T> responseExtractor, Object... uriVariables) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void setRequestFactory(ClientHttpRequestFactory requestFactory) {
            super.setRequestFactory(requestFactory);
        }
    }
}



