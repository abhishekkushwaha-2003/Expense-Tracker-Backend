package com.spendsmart.auth.controller;

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

    // Login API
    @PostMapping("/login")
    public String login(@RequestBody User user) {
        return authService.login(user.getEmail(), user.getPasswordHash());
    }
}