package org.example.controller;

import org.example.dto.LoginRequest;
import org.example.dto.LoginResponse;
import org.example.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    @PostMapping("/login")
    public LoginResponse login(@RequestBody LoginRequest request) {
        return authService.login(request);
    }
}

//Dua catatan kecil:
//Endpoint-nya jadi /api/auth/login, sengaja dikelompokkan di bawah /api/auth, biar rapi kalau nanti nambah endpoint terkait auth lain (register, logout, dll).
//@CrossOrigin sekarang saya isi port 5173 (bukan 3000 di contoh generik sebelumnya) — ini port asli yang React kamu pakai sekarang, dikonfirmasi dari npm run dev tadi.
