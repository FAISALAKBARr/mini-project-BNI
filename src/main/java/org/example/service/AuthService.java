package org.example.service;

import org.example.config.JwtUtil;
import org.example.dto.LoginRequest;
import org.example.dto.LoginResponse;
import org.example.entity.User;
import org.example.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    public LoginResponse login(LoginRequest request) {
        Optional<User> userOpt =userRepository.findByUsername(request.getUsername());

        if (userOpt.isEmpty()) {
            throw new RuntimeException("Username tidak ditemukan");
        }

        User user = userOpt.get();

//        // Sementara perbandingan teks biasaa, nanti diganti ama BCrypt.matches() pas masuk Spring Security
//        if (!user.getPassword().equals(request.getPassword())) {
//            throw new RuntimeException("Password salah bor");
//        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("Password salah");
        }

        String token = jwtUtil.generateToken(user.getUsername(), user.getRole().getName());

//        return new LoginResponse(user.getUsername(), user.getRole().getId(), user.getRole().getName(), "Login Berhasil bor");
        return new LoginResponse(user.getUsername(), user.getRole().getId(), user.getRole().getName(), token,"Login Berhasil bor");
    }
}
