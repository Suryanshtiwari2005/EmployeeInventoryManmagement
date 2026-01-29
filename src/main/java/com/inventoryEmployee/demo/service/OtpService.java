package com.inventoryEmployee.demo.service;

import com.inventoryEmployee.demo.entity.User;
import com.inventoryEmployee.demo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
@Slf4j
public class OtpService {

    private final JavaMailSender mailSender;
    private final UserRepository userRepository;

    // Store OTPs: Email -> OtpData (OTP + Expiry)
    private final Map<String, OtpData> otpStorage = new ConcurrentHashMap<>();

    private static final int OTP_EXPIRY_MINUTES = 5;

    // Inner class to hold OTP details
    private static class OtpData {
        String otp;
        LocalDateTime expiryTime;

        OtpData(String otp, LocalDateTime expiryTime) {
            this.otp = otp;
            this.expiryTime = expiryTime;
        }
    }

    // 1. Generate and Send OTP
    public void generateAndSendOtp(String email) {
        // Check if user exists
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found with email: " + email));

        if (!user.getEnabled()) {
            throw new IllegalArgumentException("Account is disabled. Contact Admin.");
        }

        // Generate 6-digit OTP
        String otp = String.valueOf(new Random().nextInt(900000) + 100000);

        // Store OTP
        otpStorage.put(email, new OtpData(otp, LocalDateTime.now().plusMinutes(OTP_EXPIRY_MINUTES)));

        // Send Email
        sendEmail(email, otp);
        log.info("OTP sent to {}", email);
    }

    // 2. Validate OTP
    public boolean validateOtp(String email, String otp) {
        if (!otpStorage.containsKey(email)) {
            return false; // No OTP found
        }

        OtpData data = otpStorage.get(email);

        if (LocalDateTime.now().isAfter(data.expiryTime)) {
            otpStorage.remove(email); // Expired
            return false;
        }

        if (data.otp.equals(otp)) {
            otpStorage.remove(email); // Success! Clear OTP
            return true;
        }

        return false;
    }

    private void sendEmail(String to, String otp) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject("Your Login OTP Code");
        message.setText("Hello,\n\nYour One-Time Password (OTP) for login is: " + otp +
                "\n\nThis code expires in 5 minutes.\n\nDo not share this code.");
        mailSender.send(message);
    }
}