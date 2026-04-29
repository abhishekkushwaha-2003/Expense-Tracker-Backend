package com.spendsmart.auth.controller;

import com.spendsmart.auth.dto.UserPreferencesRequest;
import com.spendsmart.auth.dto.SendOtpRequest;
import com.spendsmart.auth.dto.VerifyOtpRequest;
import com.spendsmart.auth.entity.User;
import com.spendsmart.auth.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    //Register API
    @PostMapping("/register")
    public User register(@RequestBody User user) {
        return authService.register(user);
    }

    @PostMapping("/register/send-otp")
    public String sendRegistrationOtp(@RequestBody SendOtpRequest request) {
        authService.sendRegistrationOtp(request.getEmail());
        return "OTP sent successfully";
    }

    @PostMapping("/register/verify-otp")
    public String verifyRegistrationOtp(@RequestBody VerifyOtpRequest request) {
        authService.verifyRegistrationOtp(request.getEmail(), request.getOtp());
        return "OTP verified successfully";
    }

    // Login API
    @PostMapping("/login")
    public String login(@RequestBody User user) {
        return authService.login(user.getEmail(), user.getPasswordHash());
    }

    @GetMapping("/users/{userId}")
    public User getUser(@PathVariable Long userId) {
        return authService.getUserById(userId);
    }

    @GetMapping("/internal/users/{userId}")
    public User getInternalUser(@PathVariable Long userId) {
        return authService.getUserById(userId);
    }

    @PutMapping("/users/{userId}/preferences")
    public User updatePreferences(@PathVariable Long userId, @RequestBody UserPreferencesRequest request) {
        return authService.updatePreferences(userId, request);
    }
}
