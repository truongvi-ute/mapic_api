package com.mapic.backend.config;

import com.mapic.backend.entity.Admin;
import com.mapic.backend.entity.User;
import com.mapic.backend.repository.AdminRepository;
import com.mapic.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
@RequiredArgsConstructor
@Slf4j
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;
    private final AdminRepository adminRepository;

    @Override
    public UserDetails loadUserByUsername(String usernameOrEmail) throws UsernameNotFoundException {
        log.info("CustomUserDetailsService.loadUserByUsername called for: {}", usernameOrEmail);
        
        // Try to find in admins table first (by username or email)
        var adminOpt = adminRepository.findByUsername(usernameOrEmail);
        if (adminOpt.isEmpty() && usernameOrEmail.contains("@")) {
            adminOpt = adminRepository.findByEmail(usernameOrEmail);
        }
        
        if (adminOpt.isPresent()) {
            Admin admin = adminOpt.get();
            log.info("Found admin: {} with role: {}", admin.getUsername(), admin.getRole());
            
            return org.springframework.security.core.userdetails.User.builder()
                    .username(admin.getUsername())
                    .password(admin.getPassword())
                    .authorities(Collections.singletonList(
                            new SimpleGrantedAuthority("ROLE_" + admin.getRole().name())
                    ))
                    .accountExpired(false)
                    .accountLocked(!admin.getIsActive())
                    .credentialsExpired(false)
                    .disabled(!admin.getIsActive())
                    .build();
        }
        
        // Try to find in users table (by username or email)
        var userOpt = userRepository.findByUsername(usernameOrEmail);
        if (userOpt.isEmpty() && usernameOrEmail.contains("@")) {
            userOpt = userRepository.findByEmail(usernameOrEmail);
        }
        
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            log.info("Found user: {} (searched by: {})", user.getUsername(), usernameOrEmail);
            
            return org.springframework.security.core.userdetails.User.builder()
                    .username(user.getUsername())
                    .password(user.getPassword())
                    .authorities(Collections.singletonList(
                            new SimpleGrantedAuthority("ROLE_USER")
                    ))
                    .accountExpired(false)
                    .accountLocked(false)
                    .credentialsExpired(false)
                    .disabled(false)
                    .build();
        }
        
        log.warn("User not found: {}", usernameOrEmail);
        throw new UsernameNotFoundException("User not found: " + usernameOrEmail);
    }
}
