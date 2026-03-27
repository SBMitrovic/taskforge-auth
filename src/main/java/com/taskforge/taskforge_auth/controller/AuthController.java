package com.taskforge.taskforge_auth.controller;

import com.taskforge.taskforge_auth.dto.AuthResponse;
import com.taskforge.taskforge_auth.dto.LoginRequest;
import com.taskforge.taskforge_auth.dto.RegisterRequest;
import com.taskforge.taskforge_auth.entity.User;
import com.taskforge.taskforge_auth.security.JwtUtil;
import com.taskforge.taskforge_auth.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        User user = authService.register(
                request.getUsername(),
                request.getEmail(),
                request.getPassword()
        );
        String token = jwtUtil.generateToken(user.getUsername(), user.getRole().name(), user.getId());
        return ResponseEntity.ok(new AuthResponse(token, user.getUsername(), user.getRole().name()));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        User user = authService.findByUsername(request.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            return ResponseEntity.status(401).build();
        }

        String token = jwtUtil.generateToken(user.getUsername(), user.getRole().name(), user.getId());
        return ResponseEntity.ok(new AuthResponse(token, user.getUsername(), user.getRole().name()));
    }

    @GetMapping("/me")
    public ResponseEntity<AuthResponse> getCurrentUser(@RequestHeader("Authorization") String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(401).build();
        }

        String token = authHeader.substring(7);

        if (!jwtUtil.isTokenValid(token)) {
            return ResponseEntity.status(401).build();
        }

        String username = jwtUtil.extractUsername(token);
        User user = authService.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        String newToken = jwtUtil.generateToken(user.getUsername(), user.getRole().name(), user.getId());
        return ResponseEntity.ok(new AuthResponse(newToken, user.getUsername(), user.getRole().name()));
    }
    
    
}