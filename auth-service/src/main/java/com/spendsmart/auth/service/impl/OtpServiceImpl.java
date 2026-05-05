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

    private final JavaMailSender mailSender;
    private final UserRepository userRepository;
    private final StringRedisTemplate redisTemplate;
    private final Map<String, OtpEntry> registrationOtps = new ConcurrentHashMap<>();
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
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already exists");
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
    public void verifyRegistrationOtp(String email, String otp) {
        validateOtp(email, otp, true);
    }

    @Override
    public void checkRegistrationOtp(String email, String otp) {
        validateOtp(email, otp, false);
    }

    private void validateOtp(String email, String otp, boolean consumeOtp) {
        String normalizedEmail = normalizeEmail(email);
        if (otp == null || otp.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "OTP is required");
        }

        OtpEntry entry = getOtpEntry(normalizedEmail);
        if (entry == null || entry.expiresAt().isBefore(LocalDateTime.now())) {
            deleteOtp(normalizedEmail);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "OTP expired. Please send a new OTP.");
        }

        if (!entry.otp().equals(otp.trim())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Wrong OTP. Please try again.");
        }

        if (consumeOtp) {
            deleteOtp(normalizedEmail);
        }
    }

    private String normalizeEmail(String email) {
        if (email == null || email.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email is required");
        }

        return email.trim().toLowerCase();
    }

    private String buildRegistrationOtpKey(String normalizedEmail) {
        return REGISTRATION_OTP_KEY_PREFIX + normalizedEmail;
    }

    private void storeOtp(String normalizedEmail, String otp) {
        if (redisEnabled) {
            try {
                redisTemplate.opsForValue().set(buildRegistrationOtpKey(normalizedEmail), otp, Duration.ofMinutes(expiryMinutes));
                return;
            } catch (RedisConnectionFailureException ignored) {
                // Fall back to in-memory storage when Redis is unavailable.
            }
        }

        registrationOtps.put(normalizedEmail, new OtpEntry(otp, LocalDateTime.now().plusMinutes(expiryMinutes)));
    }

    private OtpEntry getOtpEntry(String normalizedEmail) {
        if (redisEnabled) {
            try {
                String storedOtp = redisTemplate.opsForValue().get(buildRegistrationOtpKey(normalizedEmail));
                if (storedOtp != null && !storedOtp.isBlank()) {
                    return new OtpEntry(storedOtp, LocalDateTime.now().plusMinutes(expiryMinutes));
                }
            } catch (RedisConnectionFailureException ignored) {
                // Fall back to in-memory storage when Redis is unavailable.
            }
        }

        return registrationOtps.get(normalizedEmail);
    }

    private void deleteOtp(String normalizedEmail) {
        if (redisEnabled) {
            try {
                redisTemplate.delete(buildRegistrationOtpKey(normalizedEmail));
            } catch (RedisConnectionFailureException ignored) {
                // Remove from local fallback instead.
            }
        }
        registrationOtps.remove(normalizedEmail);
    }

    private record OtpEntry(String otp, LocalDateTime expiresAt) {
    }
}
