package com.inventoryEmployee.demo.service;

import com.inventoryEmployee.demo.dto.request.AuthResponse;
import com.inventoryEmployee.demo.dto.request.LoginRequest;
import com.inventoryEmployee.demo.dto.request.RegisterRequest;
import com.inventoryEmployee.demo.entity.Employee;
import com.inventoryEmployee.demo.entity.Role;
import com.inventoryEmployee.demo.entity.User;
import com.inventoryEmployee.demo.entity.UserSession;
import com.inventoryEmployee.demo.enums.EmployeeStatus;
import com.inventoryEmployee.demo.enums.NotificationPriority;
import com.inventoryEmployee.demo.enums.NotificationType;
import com.inventoryEmployee.demo.enums.UserRole;
import com.inventoryEmployee.demo.repository.EmployeeRepository;
import com.inventoryEmployee.demo.repository.RoleRepository;
import com.inventoryEmployee.demo.repository.UserRepository;
import com.inventoryEmployee.demo.repository.UserSessionRepository;
import com.inventoryEmployee.demo.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
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
import java.util.List;
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
    private final UserSessionRepository userSessionRepository;
    private final HttpServletRequest httpRequest;
    private final NotificationService notificationService;

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
                .status(EmployeeStatus.PENDING_APPROVAL)
                .build();

        Employee savedEmployee = employeeRepository.save(employee);

        // Create new user
        User user = User.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .email(request.getEmail())
                .employee(savedEmployee)
                .enabled(false)
                .accountNonLocked(true)
                .failedLoginAttempts(0)
                .build();

        // Assign default role (EMPLOYEE)
        Role defaultRole = roleRepository.findByName("EMPLOYEE")
                .orElseThrow(() -> new RuntimeException("Default role not found"));

        Set<Role> roles = new HashSet<>();
        roles.add(defaultRole);
        user.setRoles(roles);

        User savedUser = userRepository.save(user);

        notifyAdminsOfNewRegistration(savedUser);

//        // Generate JWT token
//        UserDetails userDetails = userDetailsService.loadUserByUsername(savedUser.getUsername());
//        String token = jwtUtil.generateToken(userDetails);

        return AuthResponse.builder()
                .token(null)
                .username(savedUser.getUsername())
                .email(savedUser.getEmail())
                .roles(List.of("EMPLOYEE"))
                .message("Registration successful. Please wait for Admin approval.")
                .build();
    }

    // 2. NEW: Approve User Method
    public void approveUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Enable User
        user.setEnabled(true);
        userRepository.save(user);

        // Update Employee Status
        if (user.getEmployee() != null) {
            user.getEmployee().setStatus(EmployeeStatus.ACTIVE);
            employeeRepository.save(user.getEmployee());
        }

        // Notify the User
        notificationService.createNotification(
                user,
                NotificationType.SYSTEM_ALERT,
                "Account Approved",
                "Your account has been approved. You can now login.",
                "/login",
                NotificationPriority.HIGH
        );
        log.info("User {} approved by admin", user.getUsername());
    }

    // 3. NEW: Reject User Method
    public void rejectUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Hard delete or Soft delete based on preference.
        // Usually hard delete for rejected registrations to clean up DB.
        userRepository.delete(user);
        if (user.getEmployee() != null) {
            employeeRepository.delete(user.getEmployee());
        }
        log.info("User {} rejected and deleted by admin", user.getUsername());
    }

    // Helper to notify admins
    private void notifyAdminsOfNewRegistration(User newUser) {
        // You need a repository method to find Admins, or filter them manually
        List<User> admins = userRepository.findByRoleName("ADMIN");

        for (User admin : admins) {
            notificationService.createNotification(
                    admin,
                    NotificationType.APPROVAL_REQUEST,
                    "New Registration Request",
                    "User " + newUser.getUsername() + " (" + newUser.getEmail() + ") requested access.",
                    "/admin/approvals", // Link to your frontend approval page
                    NotificationPriority.HIGH
            );
        }
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

            // 4. --- RECORD SESSION ---
            String ipAddress = httpRequest.getRemoteAddr();
            // Handle proxy headers if behind Nginx/AWS LB
            String xForwardedFor = httpRequest.getHeader("X-Forwarded-For");
            if (xForwardedFor != null) {
                ipAddress = xForwardedFor.split(",")[0];
            }

            UserSession session = UserSession.builder()
                    .user(user)
                    .sessionToken(token)
                    .loginTime(LocalDateTime.now())
                    .ipAddress(ipAddress)
                    .userAgent(httpRequest.getHeader("User-Agent"))
                    .isActive(true)
                    .build();

            userSessionRepository.save(session);

            user.setLastLoginDate(LocalDateTime.now());
            user.setFailedLoginAttempts(0);
            userRepository.save(user);

            return AuthResponse.builder()
                    .token(token)
                    .username(user.getUsername())
                    .email(user.getEmail())
                    .roles(user.getRoles().stream()
                            .map(Role::getName)
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

    // New Method for OTP Login
    public AuthResponse loginViaOtp(String email) {
        // 1. Find User
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // 2. Load UserDetails (Standard Spring Security user)
        UserDetails userDetails = userDetailsService.loadUserByUsername(user.getUsername());

        // 3. Generate JWT
        String token = jwtUtil.generateToken(userDetails);

        // 4. Record Session
        recordUserSession(user, token); // Use the logic you wrote earlier for saving sessions

        return AuthResponse.builder()
                .token(token)
                .username(user.getUsername())
                .email(user.getEmail())
                .roles(user.getRoles()
                        .stream()
                        .map(Role::getName)
                        .toList())
                .message("Login Successful via OTP")
                .build();
    }

    // Extract your session recording logic into a helper method if you haven't already
    private void recordUserSession(User user, String token) {
        String ipAddress = httpRequest.getRemoteAddr();
        String xForwardedFor = httpRequest.getHeader("X-Forwarded-For");
        if (xForwardedFor != null) {
            ipAddress = xForwardedFor.split(",")[0];
        }

        UserSession session = UserSession.builder()
                .user(user)
                .sessionToken(token)
                .loginTime(LocalDateTime.now())
                .ipAddress(ipAddress)
                .userAgent(httpRequest.getHeader("User-Agent"))
                .isActive(true)
                .build();

        userSessionRepository.save(session);
    }
}
