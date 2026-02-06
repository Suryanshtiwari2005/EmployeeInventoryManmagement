package com.inventoryEmployee.demo.config;

import com.inventoryEmployee.demo.entity.Role;
import com.inventoryEmployee.demo.enums.UserRole;
import com.inventoryEmployee.demo.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.Map;

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

        Map<String, String> defaultRoles = Map.of(
                "ADMIN", "Full system access and user management",
                "MANAGER", "Manage inventory, employees, and view reports",
                "EMPLOYEE", "Basic access to inventory operations"
        );


        defaultRoles.forEach((roleName, description) -> {
            // Check if role exists using String (matches your new Repository)
            if (!roleRepository.existsByName(roleName)) {
                Role role = Role.builder()
                        .name(roleName) // Uses String now
                        .description(description)
                        .build();

                roleRepository.save(role);
                log.info("Created default role: {}", roleName);
            }
        });
    }

//    private String getDescriptionForRole(UserRole role) {
//        return switch (role) {
//            case ADMIN -> "Full system access and user management";
//            case MANAGER -> "Manage inventory, employees, and view reports";
//            case EMPLOYEE -> "Basic access to inventory operations";
//        };
//    }
}
