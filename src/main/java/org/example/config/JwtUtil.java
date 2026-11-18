package org.example.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secretKeyString; //JWT membutuhkan secret key untuk membuat dan memverifikasi tanda tangan digital token.

    private static final long EXPIRATION_MS = 1000 * 60 * 60; //1 jam

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secretKeyString.getBytes());
    } //mengubah String menjadi array byte.

    public String generateToken(String username, String role) {
        return Jwts.builder()
                .setSubject(username) //default subject claim standar JWT
                .claim("role", role) // subject claim tambahan yang didefinisikan sendiri pake "claim"
                .setIssuedAt(new Date()) // Waktu ketika token dibuat. new Date() menghasil objek tanggal dan waktu bray.
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_MS))
                .signWith(getSigningKey()) //Memberikan tanda tangan digital pada token menggunakan secret key.
                .compact(); // Menyelesaikan proses pembuatan JWT dan menghasilkan token (String).
    }

    public boolean isTokenValid(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(getSigningKey()).build().parseClaimsJws(token);
            return true;
        } catch (Exception e){
            return false;
        }
    }

    public String extractUsername(String token) {
        return extractClaims(token).getSubject();
    } //ambil username yang disimpan dalam JWT

    public String extractRole(String token){
        return extractClaims(token).get("role", String.class);
    }

    private Claims extractClaims(String token) {
        return Jwts.parserBuilder().setSigningKey(getSigningKey()).build().parseClaimsJws(token).getBody();
    } //helper untuk mengambil seluruh payload JWT setelah token berhasil diparse dan diverifikasi.
}
