package com.spendsmart.auth.service.impl;

import com.spendsmart.auth.repository.UserRepository;
import com.spendsmart.auth.service.OtpService;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class OtpServiceImpl implements OtpService {

    private static final SecureRandom RANDOM = new SecureRandom();
    private static final String REGISTRATION_OTP_KEY_PREFIX = "spendsmart:auth:otp:registration:";
    private static final String PASSWORD_RESET_OTP_KEY_PREFIX = "spendsmart:auth:otp:password-reset:";

    private final JavaMailSender mailSender;
    private final UserRepository userRepository;
    private final StringRedisTemplate redisTemplate;
    private final Map<String, OtpEntry> registrationOtps = new ConcurrentHashMap<>();
    private final Map<String, OtpEntry> passwordResetOtps = new ConcurrentHashMap<>();
    private final int expiryMinutes;
    private final String fromEmail;
    private final boolean redisEnabled;

    public OtpServiceImpl(
            JavaMailSender mailSender,
            UserRepository userRepository,
            StringRedisTemplate redisTemplate,
            @Value("${app.otp.expiry-minutes:10}") int expiryMinutes,
            @Value("${app.otp.from-email:}") String fromEmail,
            @Value("${app.otp.redis-enabled:false}") boolean redisEnabled
    ) {
        this.mailSender = mailSender;
        this.userRepository = userRepository;
        this.redisTemplate = redisTemplate;
        this.expiryMinutes = expiryMinutes;
        this.fromEmail = fromEmail;
        this.redisEnabled = redisEnabled;
    }

    @Override
    public void sendRegistrationOtp(String email) {
        String normalizedEmail = normalizeEmail(email);
        if (userRepository.existsByEmail(normalizedEmail)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already registered.");
        }

        String otp = String.format("%06d", RANDOM.nextInt(1_000_000));
        storeOtp(normalizedEmail, otp);

        SimpleMailMessage message = new SimpleMailMessage();
        if (fromEmail != null && !fromEmail.isBlank()) {
            message.setFrom(fromEmail);
        }
        message.setTo(normalizedEmail);
        message.setSubject("Spend Smart registration OTP");
        message.setText("""
                Your Spend Smart registration OTP is %s.

                This code is valid for %d minutes. Do not share it with anyone.
                """.formatted(otp, expiryMinutes));

        try {
            mailSender.send(message);
        } catch (MailException ex) {
            deleteOtp(normalizedEmail);
            throw new ResponseStatusException(
                    HttpStatus.SERVICE_UNAVAILABLE,
                    "Unable to send OTP email. Please check SMTP configuration.",
                    ex
            );
        }
    }

    @Override
    public void sendPasswordResetOtp(String email) {
        String normalizedEmail = normalizeEmail(email);
        if (!userRepository.existsByEmail(normalizedEmail)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
        }

        String otp = String.format("%06d", RANDOM.nextInt(1_000_000));
        storeOtp(normalizedEmail, otp, PASSWORD_RESET_OTP_KEY_PREFIX, passwordResetOtps);

        SimpleMailMessage message = new SimpleMailMessage();
        if (fromEmail != null && !fromEmail.isBlank()) {
            message.setFrom(fromEmail);
        }
        message.setTo(normalizedEmail);
        message.setSubject("Spend Smart password reset OTP");
        message.setText("""
                Your Spend Smart password reset OTP is %s.

                This code is valid for %d minutes. If you did not request this, you can ignore this email.
                """.formatted(otp, expiryMinutes));

        try {
            mailSender.send(message);
        } catch (MailException ex) {
            deleteOtp(normalizedEmail, PASSWORD_RESET_OTP_KEY_PREFIX, passwordResetOtps);
            throw new ResponseStatusException(
                    HttpStatus.SERVICE_UNAVAILABLE,
                    "Unable to send OTP email. Please check SMTP configuration.",
                    ex
            );
        }
    }

    @Override
    public void verifyRegistrationOtp(String email, String otp) {
        validateOtp(email, otp, true, REGISTRATION_OTP_KEY_PREFIX, registrationOtps);
    }

    @Override
    public void verifyPasswordResetOtp(String email, String otp) {
        validateOtp(email, otp, false, PASSWORD_RESET_OTP_KEY_PREFIX, passwordResetOtps);
    }

    @Override
    public void checkRegistrationOtp(String email, String otp) {
        validateOtp(email, otp, false, REGISTRATION_OTP_KEY_PREFIX, registrationOtps);
    }

    @Override
    public void consumePasswordResetOtp(String email, String otp) {
        validateOtp(email, otp, true, PASSWORD_RESET_OTP_KEY_PREFIX, passwordResetOtps);
    }

    private void validateOtp(String email, String otp, boolean consumeOtp, String keyPrefix, Map<String, OtpEntry> localStore) {
        String normalizedEmail = normalizeEmail(email);
        if (otp == null || otp.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "OTP is required");
        }

        OtpEntry entry = getOtpEntry(normalizedEmail, keyPrefix, localStore);
        if (entry == null || entry.expiresAt().isBefore(LocalDateTime.now())) {
            deleteOtp(normalizedEmail, keyPrefix, localStore);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "OTP expired. Please send a new OTP.");
        }

        if (!entry.otp().equals(otp.trim())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Wrong OTP. Please try again.");
        }

        if (consumeOtp) {
            deleteOtp(normalizedEmail, keyPrefix, localStore);
        }
    }

    private String normalizeEmail(String email) {
        if (email == null || email.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email is required");
        }

        return email.trim().toLowerCase();
    }

    private String buildOtpKey(String keyPrefix, String normalizedEmail) {
        return keyPrefix + normalizedEmail;
    }

    private void storeOtp(String normalizedEmail, String otp) {
        storeOtp(normalizedEmail, otp, REGISTRATION_OTP_KEY_PREFIX, registrationOtps);
    }

    private void storeOtp(String normalizedEmail, String otp, String keyPrefix, Map<String, OtpEntry> localStore) {
        if (redisEnabled) {
            try {
                redisTemplate.opsForValue().set(buildOtpKey(keyPrefix, normalizedEmail), otp, Duration.ofMinutes(expiryMinutes));
                return;
            } catch (RedisConnectionFailureException ignored) {
                // Fall back to in-memory storage when Redis is unavailable.
            }
        }

        localStore.put(normalizedEmail, new OtpEntry(otp, LocalDateTime.now().plusMinutes(expiryMinutes)));
    }

    private OtpEntry getOtpEntry(String normalizedEmail, String keyPrefix, Map<String, OtpEntry> localStore) {
        if (redisEnabled) {
            try {
                String storedOtp = redisTemplate.opsForValue().get(buildOtpKey(keyPrefix, normalizedEmail));
                if (storedOtp != null && !storedOtp.isBlank()) {
                    return new OtpEntry(storedOtp, LocalDateTime.now().plusMinutes(expiryMinutes));
                }
            } catch (RedisConnectionFailureException ignored) {
                // Fall back to in-memory storage when Redis is unavailable.
            }
        }

        return localStore.get(normalizedEmail);
    }

    private void deleteOtp(String normalizedEmail) {
        deleteOtp(normalizedEmail, REGISTRATION_OTP_KEY_PREFIX, registrationOtps);
    }

    private void deleteOtp(String normalizedEmail, String keyPrefix, Map<String, OtpEntry> localStore) {
        if (redisEnabled) {
            try {
                redisTemplate.delete(buildOtpKey(keyPrefix, normalizedEmail));
            } catch (RedisConnectionFailureException ignored) {
                // Remove from local fallback instead.
            }
        }
        localStore.remove(normalizedEmail);
    }

    private record OtpEntry(String otp, LocalDateTime expiresAt) {
    }
}
