package com.taskforge.taskforge_auth.controller;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.taskforge.taskforge_auth.dto.AuthResponse;
import com.taskforge.taskforge_auth.dto.LoginRequest;
import com.taskforge.taskforge_auth.dto.RegisterRequest;
import com.taskforge.taskforge_auth.dto.UserResponse;
import com.taskforge.taskforge_auth.entity.User;
import com.taskforge.taskforge_auth.repository.UserRepository;
import com.taskforge.taskforge_auth.security.JwtUtil;
import com.taskforge.taskforge_auth.service.AuthService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserRepository userRepository;
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
    
    @GetMapping("/users")
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        List<UserResponse> users = userRepository.findAll()
            .stream()
            .map(u -> new UserResponse(u.getId(), u.getUsername(), u.getEmail(), u.getRole().name()))
            .collect(Collectors.toList());
        return ResponseEntity.ok(users);
    }
}