package com.mapic.backend.service;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService {

    @Async
    public void sendOtpEmail(String to, String otp) {
        System.out.println("==========================================");
        System.out.println("EMAILING OTP TO: " + to);
        System.out.println("OTP CODE: " + otp);
        System.out.println("==========================================");
    }
}
