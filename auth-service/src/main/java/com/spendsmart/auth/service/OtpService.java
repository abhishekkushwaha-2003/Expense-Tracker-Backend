package com.spendsmart.auth.service;

public interface OtpService {

    void sendRegistrationOtp(String email);

    void verifyRegistrationOtp(String email, String otp);

    void checkRegistrationOtp(String email, String otp);
}
