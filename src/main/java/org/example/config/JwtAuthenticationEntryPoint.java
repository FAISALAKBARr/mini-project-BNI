package org.example.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;

import java.io.IOException;

public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED); // 401
        response.setContentType("text/plain");
        response.getWriter().write("Belum login, atau token tidak ada/tidak valid/kadaluarsa");
    }
}

//AuthenticationEntryPoint (custom): penjaga baru yang dipanggil Spring Security khusus buat kasus "belum terbukti identitas", dipaksa balikin 401.