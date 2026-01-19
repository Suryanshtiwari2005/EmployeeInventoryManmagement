package com.inventoryEmployee.demo.service;

import com.inventoryEmployee.demo.dto.request.AuthResponse;
import com.inventoryEmployee.demo.dto.request.LoginRequest;
import com.inventoryEmployee.demo.dto.request.RegisterRequest;
import com.inventoryEmployee.demo.entity.Employee;
import com.inventoryEmployee.demo.entity.Role;
import com.inventoryEmployee.demo.entity.User;
import com.inventoryEmployee.demo.enums.UserRole;
import com.inventoryEmployee.demo.repository.EmployeeRepository;
import com.inventoryEmployee.demo.repository.RoleRepository;
import com.inventoryEmployee.demo.repository.UserRepository;
import com.inventoryEmployee.demo.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;
    private final CustomUserDetailsService userDetailsService;
    private final EmployeeRepository employeeRepository;

    // Register new user
    public AuthResponse register(RegisterRequest request) {
        log.info("Registering new user: {}", request.getUsername());

        // Check if username exists
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new IllegalArgumentException("Username already exists");
        }

        // Check if email exists
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email already exists");
        }

        Employee employee = Employee.builder()
                .firstName(request.getUsername())
                .lastName("Unknown")
                .email(request.getEmail())
                .position("Employee")
                .hireDate(LocalDate.now())
                .build();

        Employee savedEmployee = employeeRepository.save(employee);

        // Create new user
        User user = User.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .email(request.getEmail())
                .employee(savedEmployee)
                .enabled(true)
                .accountNonLocked(true)
                .failedLoginAttempts(0)
                .build();

        // Assign default role (EMPLOYEE)
        Role defaultRole = roleRepository.findByName(UserRole.EMPLOYEE)
                .orElseThrow(() -> new RuntimeException("Default role not found"));

        Set<Role> roles = new HashSet<>();
        roles.add(defaultRole);
        user.setRoles(roles);

        User savedUser = userRepository.save(user);

        // Generate JWT token
        UserDetails userDetails = userDetailsService.loadUserByUsername(savedUser.getUsername());
        String token = jwtUtil.generateToken(userDetails);

        return AuthResponse.builder()
                .token(token)
                .username(savedUser.getUsername())
                .email(savedUser.getEmail())
                .roles(savedUser.getRoles().stream()
                        .map(role -> role.getName().name())
                        .toList())
                .message("User registered successfully")
                .build();
    }

    // Login user
    @Transactional(noRollbackFor = BadCredentialsException.class)
    public AuthResponse login(LoginRequest request) {
        log.info("Login attempt for user: {}", request.getUsername());

        try {
            // Authenticate user
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getUsername(),
                            request.getPassword()
                    )
            );

            // Get user from database
            User user = userRepository.findByUsername(request.getUsername())
                    .orElseThrow(() -> new BadCredentialsException("Invalid credentials"));

            // Reset failed login attempts on successful login
            if (user.getFailedLoginAttempts()  > 0) {
                userRepository.resetFailedLoginAttempts(user.getId());
            }

            // Update last login date
            userRepository.updateLastLoginDate(user.getId(), LocalDateTime.now());

            // Generate JWT token
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            String token = jwtUtil.generateToken(userDetails);

            return AuthResponse.builder()
                    .token(token)
                    .username(user.getUsername())
                    .email(user.getEmail())
                    .roles(user.getRoles().stream()
                            .map(role -> role.getName().name())
                            .toList())
                    .message("Login successful")
                    .build();

        } catch (BadCredentialsException e) {
            // Handle failed login attempt
            userRepository.findByUsername(request.getUsername()).ifPresent(user -> {
                userRepository.incrementFailedLoginAttempts(user.getId());

                // Lock account after 5 failed attempts
                if (user.getFailedLoginAttempts() >= 4) {
                    userRepository.lockAccount(user.getId());
                    log.warn("Account locked for user: {}", request.getUsername());
                }
            });

            throw new BadCredentialsException("Invalid username or password");
        }
    }

    // Validate token
    public boolean validateToken(String token) {
        try {
            String username = jwtUtil.extractUsername(token);
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);
            return jwtUtil.validateToken(token, userDetails);
        } catch (Exception e) {
            return false;
        }
    }
}
