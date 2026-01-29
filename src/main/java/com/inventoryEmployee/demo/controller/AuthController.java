package com.inventoryEmployee.demo.controller;

import com.inventoryEmployee.demo.dto.request.AuthResponse;
import com.inventoryEmployee.demo.dto.request.LoginRequest;
import com.inventoryEmployee.demo.dto.request.RegisterRequest;
import com.inventoryEmployee.demo.service.AuthService;
import com.inventoryEmployee.demo.service.OtpService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class AuthController {

    private final AuthService authService;
    private final OtpService otpService;

    // Register new user
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        AuthResponse response = authService.register(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    // Login user
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    // Validate token
    @GetMapping("/validate")
    public ResponseEntity<Boolean> validateToken(@RequestParam String token) {
        boolean isValid = authService.validateToken(token);
        return ResponseEntity.ok(isValid);
    }

    // 1. Request OTP
    @PostMapping("/request-otp")
    public ResponseEntity<String> requestOtp(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        otpService.generateAndSendOtp(email);
        return ResponseEntity.ok("OTP sent successfully to " + email);
    }

    // 2. Login with OTP
    @PostMapping("/login-otp")
    public ResponseEntity<AuthResponse> loginWithOtp(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        String otp = request.get("otp");

        // Validate OTP logic
        boolean isValid = otpService.validateOtp(email, otp);

        if (!isValid) {
            throw new BadCredentialsException("Invalid or Expired OTP");
        }

        // If valid, delegate to AuthService to generate JWT
        AuthResponse response = authService.loginViaOtp(email);
        return ResponseEntity.ok(response);
    }
}
