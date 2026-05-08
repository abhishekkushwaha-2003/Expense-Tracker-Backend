package com.spendsmart.auth.service.impl;

import com.spendsmart.auth.dto.UserPreferencesRequest;
import com.spendsmart.auth.entity.User;
import com.spendsmart.auth.messaging.NotificationPublisher;
import com.spendsmart.auth.repository.UserRepository;
import com.spendsmart.auth.security.JwtUtil;
import com.spendsmart.auth.service.AuthService;
import com.spendsmart.auth.service.OtpService;
import java.util.List;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final RestTemplate restTemplate;
    private final OtpService otpService;
    private final PasswordEncoder passwordEncoder;
    private final NotificationPublisher notificationPublisher;
    private final boolean asyncNotificationEnabled;

    public AuthServiceImpl(UserRepository userRepository,
                           JwtUtil jwtUtil,
                           RestTemplate restTemplate,
                           OtpService otpService,
                           PasswordEncoder passwordEncoder,
                           @org.springframework.beans.factory.annotation.Autowired(required = false) NotificationPublisher notificationPublisher,
                           @Value("${app.messaging.notification.async-enabled:false}") boolean asyncNotificationEnabled) {
        this.userRepository = userRepository;
        this.jwtUtil = jwtUtil;
        this.restTemplate = restTemplate;
        this.otpService = otpService;
        this.passwordEncoder = passwordEncoder;
        this.notificationPublisher = notificationPublisher;
        this.asyncNotificationEnabled = asyncNotificationEnabled;
    }

    @Override
    public User register(User user) {

        if (user.getEmail() == null || user.getEmail().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email is required");
        }

        if (user.getPassword() == null || user.getPassword().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Password is required");
        }

        if (userRepository.existsByEmail(user.getEmail())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already registered.");
        }

        otpService.verifyRegistrationOtp(user.getEmail(), user.getOtp());

        user.setStatus("active");
        if (user.getCurrency() == null || user.getCurrency().isBlank()) {
            user.setCurrency("INR");
        }
        if (user.getTimezone() == null || user.getTimezone().isBlank()) {
            user.setTimezone("Asia/Kolkata");
        }
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        User savedUser = userRepository.save(user);
        sendWelcomeNotification(savedUser);
        return savedUser;
    }

    @Override
    public void sendRegistrationOtp(String email) {
        otpService.sendRegistrationOtp(email);
    }

    @Override
    public void sendPasswordResetOtp(String email) {
        otpService.sendPasswordResetOtp(email);
    }

    @Override
    public void verifyRegistrationOtp(String email, String otp) {
        otpService.checkRegistrationOtp(email, otp);
    }

    @Override
    public void verifyPasswordResetOtp(String email, String otp) {
        otpService.verifyPasswordResetOtp(email, otp);
    }

    @Override
    public void resetPassword(String email, String otp, String newPassword) {
        if (newPassword == null || newPassword.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Password is required");
        }

        User user = userRepository.findByEmail(email == null ? "" : email.trim().toLowerCase())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        otpService.consumePasswordResetOtp(user.getEmail(), otp);
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    @Override
    public String login(String email, String password) {

        if (email == null || email.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email is required");
        }

        if (password == null || password.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Password is required");
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        if (!"active".equalsIgnoreCase(user.getStatus())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Account is suspended. Please contact the admin.");
        }

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid password");
        }

        return jwtUtil.generateToken(user.getEmail());
    }

    @Override
    public User getUserById(Long userId) {
        return userRepository.findByUserId(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
    }

    @Override
    public User updatePreferences(Long userId, UserPreferencesRequest request) {
        User user = getUserById(userId);

        if (request.getFullName() != null && !request.getFullName().isBlank()) {
            user.setFullName(request.getFullName().trim());
        }

        if (request.getCurrency() != null && !request.getCurrency().isBlank()) {
            user.setCurrency(request.getCurrency().trim().toUpperCase());
        }

        if (request.getTimezone() != null && !request.getTimezone().isBlank()) {
            user.setTimezone(request.getTimezone().trim());
        }

        if (request.getMonthlyBudget() != null) {
            if (request.getMonthlyBudget() < 0) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Monthly goal cannot be negative");
            }
            user.setMonthlyBudget(request.getMonthlyBudget());
        }

        return userRepository.save(user);
    }

    @Override
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @Override
    public User updateUserStatus(Long userId, boolean active) {
        User user = getUserById(userId);
        user.setStatus(active ? "active" : "deactive");
        return userRepository.save(user);
    }

    @Override
    public void deleteUser(Long userId) {
        User user = getUserById(userId);
        userRepository.delete(user);
    }

    private void sendWelcomeNotification(User user) {
        try {
            Map<String, Object> request = new LinkedHashMap<>();
            request.put("recipientId", user.getUserId());
            request.put("recipientEmail", user.getEmail());
            request.put("type", "SYSTEM");
            request.put("severity", "INFO");
            request.put("title", "Welcome to Spend Smart");
            request.put(
                    "message",
                    String.format(
                            "Hi %s, your Spend Smart account is ready. Start by setting your monthly goal and tracking your first expense.",
                            user.getFullName() == null || user.getFullName().isBlank() ? "there" : user.getFullName()
                    )
            );
            request.put("relatedId", user.getUserId());
            request.put("relatedType", "USER");
            request.put("emailEnabled", true);

            if (asyncNotificationEnabled && notificationPublisher != null) {
                notificationPublisher.publishNotification(request);
            } else {
                restTemplate.postForObject(
                        "http://NOTIFICATION-SERVICE/notifications/send",
                        request,
                        Object.class
                );
            }
        } catch (Exception ex) {
            // Registration should still succeed even if notification delivery is unavailable.
        }
    }
}

