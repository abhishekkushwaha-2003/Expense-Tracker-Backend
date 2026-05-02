package com.spendsmart.auth.controller;

import com.spendsmart.auth.dto.AuthLoginRequest;
import com.spendsmart.auth.dto.AuthRegisterRequest;
import com.spendsmart.auth.dto.UserPreferencesRequest;
import com.spendsmart.auth.dto.SendOtpRequest;
import com.spendsmart.auth.dto.VerifyOtpRequest;
import com.spendsmart.auth.entity.User;
import java.util.List;
import com.spendsmart.auth.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    //Register API
    @PostMapping("/register")
    public User register(@Valid @RequestBody AuthRegisterRequest request) {
        return authService.register(request.toUser());
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
    public String login(@Valid @RequestBody AuthLoginRequest request) {
        return authService.login(request.getEmail(), request.getPassword());
    }

    @GetMapping("/users/{userId}")
    public User getUser(@PathVariable Long userId) {
        return authService.getUserById(userId);
    }

    @GetMapping("/internal/users/{userId}")
    public User getInternalUser(@PathVariable Long userId) {
        return authService.getUserById(userId);
    }

    @GetMapping("/internal/users")
    public List<User> getAllInternalUsers() {
        return authService.getAllUsers();
    }

    @PutMapping("/internal/users/{userId}/status")
    public User updateInternalUserStatus(@PathVariable Long userId, @RequestParam boolean active) {
        return authService.updateUserStatus(userId, active);
    }

    @DeleteMapping("/internal/users/{userId}")
    public void deleteInternalUser(@PathVariable Long userId) {
        authService.deleteUser(userId);
    }

    @PutMapping("/users/{userId}/preferences")
    public User updatePreferences(@PathVariable Long userId, @RequestBody UserPreferencesRequest request) {
        return authService.updatePreferences(userId, request);
    }
}
