package com.spendsmart.auth.service.impl;

import com.spendsmart.auth.dto.UserPreferencesRequest;
import com.spendsmart.auth.entity.AuthProvider;
import com.spendsmart.auth.entity.User;
import com.spendsmart.auth.repository.UserRepository;
import com.spendsmart.auth.security.JwtUtil;
import com.spendsmart.auth.service.AuthService;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final RestTemplate restTemplate;

    public AuthServiceImpl(UserRepository userRepository, JwtUtil jwtUtil, RestTemplate restTemplate) {
        this.userRepository = userRepository;
        this.jwtUtil = jwtUtil;
        this.restTemplate = restTemplate;
    }

    @Override
    public User register(User user) {

        if (user.getEmail() == null || user.getEmail().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email is required");
        }

        if (user.getPasswordHash() == null || user.getPasswordHash().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Password is required");
        }

        if (userRepository.existsByEmail(user.getEmail())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already exists");
        }

        user.setProvider(AuthProvider.LOCAL);
        user.setIsActive(true);
        if (user.getCurrency() == null || user.getCurrency().isBlank()) {
            user.setCurrency("INR");
        }
        if (user.getTimezone() == null || user.getTimezone().isBlank()) {
            user.setTimezone("Asia/Kolkata");
        }

        User savedUser = userRepository.save(user);
        sendWelcomeNotification(savedUser);
        return savedUser;
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

        if (!Boolean.TRUE.equals(user.getIsActive())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Account is inactive");
        }

        if (!user.getPasswordHash().equals(password)) {
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

            restTemplate.postForObject(
                    "http://NOTIFICATION-SERVICE/notifications/send",
                    request,
                    Object.class
            );
        } catch (Exception ex) {
            // Registration should still succeed even if notification delivery is unavailable.
        }
    }
}
