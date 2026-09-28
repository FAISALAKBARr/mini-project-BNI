package org.example.controller;

import org.example.dto.LoginRequest;
import org.example.dto.LoginResponse;
import org.example.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "http://localhost:5173")
public class AuthController {

    @Autowired
    private AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        try {
            LoginResponse response = authService.login(request);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.status(401).body(e.getMessage());
        }
    }
}

//Dua catatan kecil:
//Endpoint-nya jadi /api/auth/login, sengaja dikelompokkan di bawah /api/auth, biar rapi kalau nanti nambah endpoint terkait auth lain (register, logout, dll).
//@CrossOrigin sekarang saya isi port 5173 (bukan 3000 di contoh generik sebelumnya) — ini port asli yang React kamu pakai sekarang, dikonfirmasi dari npm run dev tadi.
