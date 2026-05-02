package com.spendsmart.auth.dto;

import com.spendsmart.auth.entity.User;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class AuthRegisterRequest {

    @NotBlank(message = "Full name is required")
    private String fullName;

    @Email(message = "Enter a valid email address in standard format, for example user@example.com.")
    @NotBlank(message = "Email is required")
    private String email;

    @NotBlank(message = "Password is required")
    @Pattern(
            regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^A-Za-z\\d]).{8,}$",
            message = "Password must contain at least one uppercase letter, one lowercase letter, one digit, one special character, and be at least 8 characters long."
    )
    private String password;

    @NotBlank(message = "OTP is required")
    private String otp;

    public User toUser() {
        return User.builder()
                .fullName(fullName)
                .email(email)
                .password(password)
                .otp(otp)
                .build();
    }
}
