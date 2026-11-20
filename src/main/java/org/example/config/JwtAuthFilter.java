package org.example.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jspecify.annotations.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil; //Membuat variabel untuk menyimpan objek JwtUtil.

    public JwtAuthFilter(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull FilterChain filterChain)
            throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization"); //authHeader berisi nilai bearer token. dimana ini untuk mengambil authorization header

        if (authHeader != null && authHeader.startsWith("Bearer ")) { // format Authorization: Bearer <token>
            String token = authHeader.substring(7); //menghapus 7 karakter pertama dari header, 'Bearer '. jdi sekarang 'token' hanya menyimpan jwt nya saja.

            if (jwtUtil.isTokenValid(token)) { //manggil method istokenvalid, dari JwtUtil. cukup memeriksa boolean. Intinya: Jika token valid, if mlaku. jika token ga mlaku, if iki di lewati bos.
                String username = jwtUtil.extractUsername(token);
                String role = jwtUtil.extractRole(token); //ambil inpo sub ama claim role ning payload jwt bos.

                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken( // class iki implementasi Authentication soko Spring Security. nggo gawe objek Authentication
                        username, null, List.of(new SimpleGrantedAuthority("ROLE_" + role)) // constructor e ono telu parameter: principal, credentials, authorities. class iki representasikan authority atau hak akses user soko Spring Security. ROLE_ soale nganggo .hasRole("ADMIN")
                );
                SecurityContextHolder.getContext().setAuthentication(authToken); // nyimpen Authentication ke SecurityContext
            }
        }
        filterChain.doFilter(request, response); //request dilanjutkan ke filter berikutnya dalam rantai filter.
    }
}