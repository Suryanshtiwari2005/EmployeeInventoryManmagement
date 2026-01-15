package com.inventoryEmployee.demo.service;

import com.inventoryEmployee.demo.config.CustomUserDetails;
import com.inventoryEmployee.demo.entity.User;
import com.inventoryEmployee.demo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

        if (user.getDeleted()) {
            throw new UsernameNotFoundException("User account is deleted: " + username);
        }

        if (!user.getEnabled()) {
            throw new UsernameNotFoundException("User account is disabled: " + username);
        }

        if (!user.getAccountNonLocked()) {
            throw new UsernameNotFoundException("User account is locked: " + username);
        }

        Set<GrantedAuthority> authorities = user.getRoles().stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role.getName().name()))
                .collect(Collectors.toSet());

        return new CustomUserDetails(user);
    }
}
