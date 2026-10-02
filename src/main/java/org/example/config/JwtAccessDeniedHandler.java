package org.example.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;

import java.io.IOException;

public class JwtAccessDeniedHandler implements AccessDeniedHandler {

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException) throws IOException {
        response.setStatus(HttpServletResponse.SC_FORBIDDEN); // 403
        response.setContentType("text/plain");
        response.getWriter().write("Login sudah benar, tapi role kamu tidak punya akses ke endpoint ini");
    }
}

//AccessDeniedHandler (custom): penjaga baru buat kasus "identitas jelas, tapi role nggak cukup", dipaksa balikin 403.