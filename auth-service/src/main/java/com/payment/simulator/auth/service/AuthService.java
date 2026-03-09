package com.payment.simulator.auth.service;

import com.payment.simulator.auth.entity.User;
import com.payment.simulator.auth.repository.UserRepository;
import com.payment.simulator.common.dto.AuthRequest;
import com.payment.simulator.common.dto.AuthResponse;
import com.payment.simulator.common.dto.RegisterRequest;
import com.payment.simulator.common.enums.UserRole;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("Username already exists");
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(UserRole.valueOf(request.getRole().toUpperCase()));
        user.setMerchantId(request.getMerchantId());

        userRepository.save(user);

        String token = jwtService.generateToken(user.getUsername(),
                user.getRole().name(), user.getMerchantId());

        return new AuthResponse(token, user.getUsername(), user.getRole().name(), 86400);
    }

    public AuthResponse login(AuthRequest request) {
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new RuntimeException("Invalid credentials"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("Invalid credentials");
        }

        String token = jwtService.generateToken(user.getUsername(),
                user.getRole().name(), user.getMerchantId());

        return new AuthResponse(token, user.getUsername(), user.getRole().name(), 86400);
    }

    public boolean validateToken(String token) {
        return jwtService.isTokenValid(token);
    }
}
