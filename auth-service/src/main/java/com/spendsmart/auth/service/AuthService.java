package com.spendsmart.auth.service;

import com.spendsmart.auth.entity.User;

public interface AuthService {

    User register(User user);

    String login(String email, String password);
}