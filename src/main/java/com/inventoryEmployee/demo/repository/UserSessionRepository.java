package com.inventoryEmployee.demo.repository;

import com.inventoryEmployee.demo.entity.UserSession;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserSessionRepository extends JpaRepository<UserSession, Long> {

    // Find by session token
    Optional<UserSession> findBySessionToken(String sessionToken);

    // Find by user ID
    List<UserSession> findByUserId(Long userId);
    Page<UserSession> findByUserId(Long userId, Pageable pageable);

    // Find active sessions
    List<UserSession> findByIsActiveTrue();

    // Find active sessions by user
    List<UserSession> findByUserIdAndIsActiveTrue(Long userId);

    // Find expired sessions
    @Query("SELECT us FROM UserSession us WHERE us.expiresAt < :currentTime AND us.isActive = true")
    List<UserSession> findExpiredSessions(@Param("currentTime") LocalDateTime currentTime);

    // Find sessions by IP address
    List<UserSession> findByIpAddress(String ipAddress);

    // Find sessions in date range
    List<UserSession> findByLoginTimeBetween(LocalDateTime startDate, LocalDateTime endDate);

    // Count active sessions
    Long countByIsActiveTrue();

    // Count active sessions by user
    Long countByUserIdAndIsActiveTrue(Long userId);

    // Deactivate session by token
    @Modifying
    @Query("UPDATE UserSession us SET us.isActive = false, us.logoutTime = :logoutTime " +
            "WHERE us.sessionToken = :sessionToken")
    void deactivateSession(@Param("sessionToken") String sessionToken,
                           @Param("logoutTime") LocalDateTime logoutTime);

    // Deactivate all user sessions
    @Modifying
    @Query("UPDATE UserSession us SET us.isActive = false, us.logoutTime = :logoutTime " +
            "WHERE us.user.id = :userId AND us.isActive = true")
    void deactivateAllUserSessions(@Param("userId") Long userId,
                                   @Param("logoutTime") LocalDateTime logoutTime);

    // Delete old inactive sessions
    @Modifying
    @Query("DELETE FROM UserSession us WHERE us.isActive = false " +
            "AND us.logoutTime < :cutoffDate")
    void deleteOldSessions(@Param("cutoffDate") LocalDateTime cutoffDate);
}
