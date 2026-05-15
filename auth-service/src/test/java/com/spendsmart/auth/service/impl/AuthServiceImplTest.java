package com.spendsmart.auth.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.spendsmart.auth.dto.UserPreferencesRequest;
import com.spendsmart.auth.dto.AuthLoginResponse;
import com.spendsmart.auth.entity.User;
import com.spendsmart.auth.messaging.NotificationPublisher;
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
    void registerValidatesRequiredFieldsAndDuplicateEmail() {
        User missingEmail = User.builder().password("plain").build();
        assertThrows(ResponseStatusException.class, () -> service.register(missingEmail));

        User missingPassword = User.builder().email("new@example.com").build();
        assertThrows(ResponseStatusException.class, () -> service.register(missingPassword));

        User duplicate = User.builder().email("new@example.com").password("plain").build();
        when(userRepository.existsByEmail("new@example.com")).thenReturn(true);
        assertThrows(ResponseStatusException.class, () -> service.register(duplicate));
    }

    @Test
    void registerPublishesWelcomeNotificationAsyncAndIgnoresNotificationFailures() {
        RecordingNotificationPublisher publisher = new RecordingNotificationPublisher();
        AuthServiceImpl asyncService = new AuthServiceImpl(
                userRepository,
                jwtUtil,
                restTemplate,
                otpService,
                passwordEncoder,
                publisher,
                true
        );
        User input = User.builder().email("new@example.com").password("plain").otp("123456").build();
        User saved = User.builder().userId(99L).email("new@example.com").password("encoded").build();
        when(userRepository.existsByEmail("new@example.com")).thenReturn(false);
        when(passwordEncoder.encode("plain")).thenReturn("encoded");
        when(userRepository.save(any(User.class))).thenReturn(saved);

        User result = asyncService.register(input);

        assertEquals(saved, result);
        assertNotNull(publisher.payload);
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

        AuthLoginResponse response = service.login(" USER@example.com ", "secret");

        assertEquals(5L, response.getUserId());
        assertEquals("user@example.com", response.getEmail());
        assertTrue(jwtUtil.validateToken(response.getToken()));
        assertEquals("user@example.com", jwtUtil.extractEmail(response.getToken()));
    }

    @Test
    void loginRejectsMissingInactiveUnknownAndWrongPasswordCases() {
        assertThrows(ResponseStatusException.class, () -> service.login("", "secret"));
        assertThrows(ResponseStatusException.class, () -> service.login("user@example.com", ""));

        when(userRepository.findByEmail("missing@example.com")).thenReturn(Optional.empty());
        assertThrows(ResponseStatusException.class, () -> service.login("missing@example.com", "secret"));

        User inactive = User.builder().email("inactive@example.com").password("encoded").status("deactive").build();
        when(userRepository.findByEmail("inactive@example.com")).thenReturn(Optional.of(inactive));
        assertThrows(ResponseStatusException.class, () -> service.login("inactive@example.com", "secret"));

        User active = User.builder().email("user@example.com").password("encoded").status("active").build();
        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(active));
        when(passwordEncoder.matches("bad", "encoded")).thenReturn(false);
        assertThrows(ResponseStatusException.class, () -> service.login("user@example.com", "bad"));
    }

    @Test
    void resetPasswordConsumesOtpAndSavesEncodedPassword() {
        User user = User.builder()
                .userId(7L)
                .email("user@example.com")
                .password("old-encoded")
                .status("active")
                .build();

        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.encode("NewPass@123")).thenReturn("new-encoded");
        when(userRepository.save(user)).thenReturn(user);

        service.resetPassword(" USER@example.com ", "123456", "NewPass@123");

        verify(otpService).consumePasswordResetOtp("user@example.com", "123456");
        verify(userRepository).save(user);
        assertEquals("new-encoded", user.getPassword());
    }

    @Test
    void resetPasswordValidatesPasswordAndUnknownUser() {
        assertThrows(ResponseStatusException.class, () -> service.resetPassword("user@example.com", "123456", " "));
        when(userRepository.findByEmail("missing@example.com")).thenReturn(Optional.empty());
        assertThrows(ResponseStatusException.class, () -> service.resetPassword("missing@example.com", "123456", "NewPass@123"));
    }

    @Test
    void otpFacadeMethodsDelegateToOtpService() {
        service.sendRegistrationOtp("user@example.com");
        service.sendPasswordResetOtp("user@example.com");
        service.verifyRegistrationOtp("user@example.com", "123456");
        service.verifyPasswordResetOtp("user@example.com", "123456");

        verify(otpService).sendRegistrationOtp("user@example.com");
        verify(otpService).sendPasswordResetOtp("user@example.com");
        verify(otpService).checkRegistrationOtp("user@example.com", "123456");
        verify(otpService).verifyPasswordResetOtp("user@example.com", "123456");
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
    void updatePreferencesUpdatesProvidedFieldsAndIgnoresBlankValues() {
        User user = User.builder()
                .userId(3L)
                .email("user@example.com")
                .fullName("Old")
                .currency("INR")
                .timezone("Asia/Kolkata")
                .status("active")
                .build();
        UserPreferencesRequest request = new UserPreferencesRequest();
        request.setFullName(" New Name ");
        request.setCurrency(" usd ");
        request.setTimezone(" UTC ");
        request.setMonthlyBudget(2500.0);
        when(userRepository.findByUserId(3L)).thenReturn(Optional.of(user));
        when(userRepository.save(user)).thenReturn(user);

        User updated = service.updatePreferences(3L, request);

        assertEquals("New Name", updated.getFullName());
        assertEquals("USD", updated.getCurrency());
        assertEquals("UTC", updated.getTimezone());
        assertEquals(2500.0, updated.getMonthlyBudget());
    }

    @Test
    void updateUserStatusPersistsMappedState() {
        User user = User.builder().userId(4L).status("active").build();
        when(userRepository.findByUserId(4L)).thenReturn(Optional.of(user));
        when(userRepository.save(user)).thenReturn(user);

        User updated = service.updateUserStatus(4L, false);

        assertEquals("deactive", updated.getStatus());
    }

    @Test
    void getAllGetByIdDeleteAndActivateUserUseRepository() {
        User user = User.builder().userId(4L).status("deactive").build();
        when(userRepository.findAll()).thenReturn(java.util.List.of(user));
        when(userRepository.findByUserId(4L)).thenReturn(Optional.of(user));
        when(userRepository.findByUserId(99L)).thenReturn(Optional.empty());
        when(userRepository.save(user)).thenReturn(user);

        assertEquals(java.util.List.of(user), service.getAllUsers());
        assertEquals(user, service.getUserById(4L));
        assertEquals("active", service.updateUserStatus(4L, true).getStatus());
        service.deleteUser(4L);

        verify(userRepository).delete(user);
        assertThrows(ResponseStatusException.class, () -> service.getUserById(99L));
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

    private static final class RecordingNotificationPublisher extends NotificationPublisher {
        private Map<String, Object> payload;

        private RecordingNotificationPublisher() {
            super(null, "exchange", "routing");
        }

        @Override
        public void publishNotification(Map<String, Object> payload) {
            this.payload = payload;
        }
    }
}



