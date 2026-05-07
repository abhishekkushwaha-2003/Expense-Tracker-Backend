package com.spendsmart.auth.service.impl;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.spendsmart.auth.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.MailSendException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
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
    void sendRegistrationOtpDeletesStoredCodeWhenMailFails() {
        OtpServiceImpl service = new OtpServiceImpl(mailSender, userRepository, null, 10, "noreply@spendsmart.com", false);
        when(userRepository.existsByEmail("user@example.com")).thenReturn(false);
        doThrow(new MailSendException("smtp down")).when(mailSender).send(any(SimpleMailMessage.class));

        assertThrows(ResponseStatusException.class, () -> service.sendRegistrationOtp("user@example.com"));
        assertThrows(ResponseStatusException.class, () -> service.checkRegistrationOtp("user@example.com", "123456"));
    }
}
