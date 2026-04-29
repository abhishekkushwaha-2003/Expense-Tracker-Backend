package com.spendsmart.auth.service;

import com.spendsmart.auth.dto.UserPreferencesRequest;
import com.spendsmart.auth.entity.User;

public interface AuthService {

    User register(User user);

    String login(String email, String password);

    User getUserById(Long userId);

    User updatePreferences(Long userId, UserPreferencesRequest request);
}
