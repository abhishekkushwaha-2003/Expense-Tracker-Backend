package com.spendsmart.auth.service;

import com.spendsmart.auth.dto.UserPreferencesRequest;
import com.spendsmart.auth.dto.AuthLoginResponse;
import com.spendsmart.auth.entity.User;
import java.util.List;

public interface AuthService {

    User register(User user);

    void sendRegistrationOtp(String email);

    void sendPasswordResetOtp(String email);

    void verifyRegistrationOtp(String email, String otp);

    void verifyPasswordResetOtp(String email, String otp);

    void resetPassword(String email, String otp, String newPassword);

    AuthLoginResponse login(String email, String password);

    User getUserById(Long userId);

    User updatePreferences(Long userId, UserPreferencesRequest request);

    List<User> getAllUsers();

    User updateUserStatus(Long userId, boolean active);

    void deleteUser(Long userId);
}
