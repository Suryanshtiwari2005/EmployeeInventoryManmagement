package com.inventoryEmployee.demo.config;

import com.inventoryEmployee.demo.entity.Role;
import com.inventoryEmployee.demo.enums.UserRole;
import com.inventoryEmployee.demo.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final RoleRepository roleRepository;

    @Override
    public void run(String... args) throws Exception {
        // Initialize roles if they don't exist
        initializeRoles();
    }

    private void initializeRoles() {
        for (UserRole userRole : UserRole.values()) {
            if (!roleRepository.existsByName(userRole)) {
                Role role = Role.builder()
                        .name(userRole)
                        .description(getDescriptionForRole(userRole))
                        .build();
                roleRepository.save(role);
                log.info("Created role: {}", userRole);
            }
        }
    }

    private String getDescriptionForRole(UserRole role) {
        return switch (role) {
            case ADMIN -> "Full system access and user management";
            case MANAGER -> "Manage inventory, employees, and view reports";
            case EMPLOYEE -> "Basic access to inventory operations";
            case VIEWER -> "Read-only access to system data";
        };
    }
}
