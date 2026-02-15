package com.inventoryEmployee.demo.repository;

import com.inventoryEmployee.demo.entity.User;
import com.inventoryEmployee.demo.enums.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    // Find by username (for login)
    Optional<User> findByUsername(String username);

    // Find by email
    Optional<User> findByEmail(String email);

    // Find by employee ID
    Optional<User> findByEmployeeId(Long employeeId);

    // Check if username exists
    boolean existsByUsername(String username);

    // Check if email exists
    boolean existsByEmail(String email);


    // Find users who are NOT enabled (Pending users)
    List<User> findByEnabledFalse();

    // Find locked accounts
    List<User> findByAccountNonLockedFalse();

    // Find users by role
    @Query("SELECT u FROM User u JOIN u.roles r WHERE r.name = :roleName AND u.deleted = false")
    List<User> findByRoleName(@Param("roleName") String roleName);

    // Update last login date
    @Modifying
    @Query("UPDATE User u SET u.lastLoginDate = :loginDate WHERE u.id = :userId")
    void updateLastLoginDate(@Param("userId") Long userId, @Param("loginDate") LocalDateTime loginDate);

    // Increment failed login attempts
    @Modifying
    @Query("UPDATE User u SET u.failedLoginAttempts = u.failedLoginAttempts + 1 WHERE u.id = :userId")
    void incrementFailedLoginAttempts(@Param("userId") Long userId);

    // Reset failed login attempts
    @Modifying
    @Query("UPDATE User u SET u.failedLoginAttempts = 0 WHERE u.id = :userId")
    void resetFailedLoginAttempts(@Param("userId") Long userId);

    // Lock user account
    @Modifying
    @Query("UPDATE User u SET u.accountNonLocked = false WHERE u.id = :userId")
    void lockAccount(@Param("userId") Long userId);

    // Unlock user account
    @Modifying
    @Query("UPDATE User u SET u.accountNonLocked = true, u.failedLoginAttempts = 0 WHERE u.id = :userId")
    void unlockAccount(@Param("userId") Long userId);
}
