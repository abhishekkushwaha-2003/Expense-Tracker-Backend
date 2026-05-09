package com.spendsmart.auth.service.impl;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.spendsmart.auth.repository.UserRepository;
import java.lang.reflect.Proxy;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.MailSendException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.web.server.ResponseStatusException;

@ExtendWith(MockitoExtension.class)
class OtpServiceImplTest {

    @Mock
    private JavaMailSender mailSender;

    @Mock
    private UserRepository userRepository;

    @Test
    void sendAndVerifyRegistrationOtpConsumesStoredCode() {
        OtpServiceImpl service = new OtpServiceImpl(mailSender, userRepository, null, 10, "noreply@spendsmart.com", false);
        when(userRepository.existsByEmail("user@example.com")).thenReturn(false);

        service.sendRegistrationOtp("user@example.com");

        ArgumentCaptor<SimpleMailMessage> messageCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(messageCaptor.capture());

        String text = messageCaptor.getValue().getText();
        String otp = text.replaceAll("(?s).*OTP is (\\d{6}).*", "$1");

        service.checkRegistrationOtp("user@example.com", otp);
        service.verifyRegistrationOtp("user@example.com", otp);

        assertThrows(ResponseStatusException.class, () -> service.checkRegistrationOtp("user@example.com", otp));
    }

    @Test
    void sendRegistrationOtpRejectsExistingEmail() {
        OtpServiceImpl service = new OtpServiceImpl(mailSender, userRepository, null, 10, "noreply@spendsmart.com", false);
        when(userRepository.existsByEmail("existing@example.com")).thenReturn(true);

        assertThrows(ResponseStatusException.class, () -> service.sendRegistrationOtp("existing@example.com"));
    }

    @Test
    void sendRegistrationOtpRejectsBlankEmail() {
        OtpServiceImpl service = new OtpServiceImpl(mailSender, userRepository, null, 10, "", false);

        assertThrows(ResponseStatusException.class, () -> service.sendRegistrationOtp(" "));
    }

    @Test
    void sendAndConsumePasswordResetOtpRequiresExistingEmail() {
        OtpServiceImpl service = new OtpServiceImpl(mailSender, userRepository, null, 10, "noreply@spendsmart.com", false);
        when(userRepository.existsByEmail("user@example.com")).thenReturn(true);

        service.sendPasswordResetOtp("user@example.com");

        ArgumentCaptor<SimpleMailMessage> messageCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(messageCaptor.capture());

        String text = messageCaptor.getValue().getText();
        String otp = text.replaceAll("(?s).*OTP is (\\d{6}).*", "$1");

        service.verifyPasswordResetOtp("user@example.com", otp);
        service.consumePasswordResetOtp("user@example.com", otp);

        assertThrows(ResponseStatusException.class, () -> service.verifyPasswordResetOtp("user@example.com", otp));
    }

    @Test
    void sendPasswordResetOtpRejectsUnknownEmail() {
        OtpServiceImpl service = new OtpServiceImpl(mailSender, userRepository, null, 10, "noreply@spendsmart.com", false);
        when(userRepository.existsByEmail("missing@example.com")).thenReturn(false);

        assertThrows(ResponseStatusException.class, () -> service.sendPasswordResetOtp("missing@example.com"));
    }

    @Test
    void verifyRegistrationOtpRejectsBlankWrongAndExpiredOtp() {
        OtpServiceImpl service = new OtpServiceImpl(mailSender, userRepository, null, 10, "", false);
        when(userRepository.existsByEmail("user@example.com")).thenReturn(false);

        service.sendRegistrationOtp(" USER@example.com ");

        assertThrows(ResponseStatusException.class, () -> service.checkRegistrationOtp("user@example.com", " "));
        assertThrows(ResponseStatusException.class, () -> service.checkRegistrationOtp("user@example.com", "000000"));

        OtpServiceImpl expiredService = new OtpServiceImpl(mailSender, userRepository, null, 0, "", false);
        when(userRepository.existsByEmail("expired@example.com")).thenReturn(false);
        expiredService.sendRegistrationOtp("expired@example.com");
        assertThrows(ResponseStatusException.class, () -> expiredService.checkRegistrationOtp("expired@example.com", "000000"));
    }

    @Test
    void sendRegistrationOtpDeletesStoredCodeWhenMailFails() {
        OtpServiceImpl service = new OtpServiceImpl(mailSender, userRepository, null, 10, "noreply@spendsmart.com", false);
        when(userRepository.existsByEmail("user@example.com")).thenReturn(false);
        doThrow(new MailSendException("smtp down")).when(mailSender).send(any(SimpleMailMessage.class));

        assertThrows(ResponseStatusException.class, () -> service.sendRegistrationOtp("user@example.com"));
        assertThrows(ResponseStatusException.class, () -> service.checkRegistrationOtp("user@example.com", "123456"));
    }

    @Test
    void redisBackedRegistrationOtpStoresReadsAndDeletesCode() {
        StubRedisTemplate redisTemplate = new StubRedisTemplate();
        OtpServiceImpl service = new OtpServiceImpl(mailSender, userRepository, redisTemplate, 10, "noreply@spendsmart.com", true);
        when(userRepository.existsByEmail("user@example.com")).thenReturn(false);

        service.sendRegistrationOtp("user@example.com");

        String key = "spendsmart:auth:otp:registration:user@example.com";
        String otp = redisTemplate.values.get(key);
        service.checkRegistrationOtp("user@example.com", otp);
        service.verifyRegistrationOtp("user@example.com", otp);

        assertThrows(ResponseStatusException.class, () -> service.checkRegistrationOtp("user@example.com", otp));
    }

    @Test
    void redisFailuresFallBackToLocalStore() {
        StubRedisTemplate redisTemplate = new StubRedisTemplate();
        redisTemplate.fail = true;
        OtpServiceImpl service = new OtpServiceImpl(mailSender, userRepository, redisTemplate, 10, "", true);
        when(userRepository.existsByEmail("user@example.com")).thenReturn(false);

        service.sendRegistrationOtp("user@example.com");

        ArgumentCaptor<SimpleMailMessage> messageCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(messageCaptor.capture());
        String otp = messageCaptor.getValue().getText().replaceAll("(?s).*OTP is (\\d{6}).*", "$1");
        service.verifyRegistrationOtp("user@example.com", otp);
    }

    private static class StubRedisTemplate extends StringRedisTemplate {
        private final Map<String, String> values = new HashMap<>();
        private boolean fail;

        @Override
        public ValueOperations<String, String> opsForValue() {
            return (ValueOperations<String, String>) Proxy.newProxyInstance(
                    ValueOperations.class.getClassLoader(),
                    new Class<?>[]{ValueOperations.class},
                    (proxy, method, args) -> {
                        if (fail) {
                            throw new RedisConnectionFailureException("redis down");
                        }
                        if ("set".equals(method.getName())) {
                            values.put(String.valueOf(args[0]), String.valueOf(args[1]));
                            return null;
                        }
                        if ("get".equals(method.getName())) {
                            return values.get(String.valueOf(args[0]));
                        }
                        return null;
                    }
            );
        }

        @Override
        public Boolean delete(String key) {
            if (fail) {
                throw new RedisConnectionFailureException("redis down");
            }
            return values.remove(key) != null;
        }
    }
}
