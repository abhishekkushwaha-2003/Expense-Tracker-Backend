package com.spendsmart.auth.service.impl;

import com.spendsmart.auth.entity.AuthProvider;
import com.spendsmart.auth.entity.User;
import com.spendsmart.auth.repository.UserRepository;
import com.spendsmart.auth.security.JwtUtil;
import com.spendsmart.auth.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AuthServiceImpl implements AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtUtil jwtUtil;

    @Override
    public User register(User user) {

        if (userRepository.existsByEmail(user.getEmail())) {
            throw new RuntimeException("Email already exists");
        }

        user.setProvider(AuthProvider.LOCAL);
        user.setIsActive(true);

        return userRepository.save(user);
    }

    @Override
    public String login(String email, String password) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!user.getPasswordHash().equals(password)) {
            throw new RuntimeException("Invalid password");
        }

        // Generate JWT
        return jwtUtil.generateToken(user.getEmail());
    }
}