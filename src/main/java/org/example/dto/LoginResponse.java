package org.example.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter @AllArgsConstructor
public class LoginResponse {
    private String username;
    private Long roleId;
    private String role;
    private String token;
    private String message;
}

//kalau User (yang punya field password) di-return langsung dari Controller, field password-nya ikut ke-serialize jadi JSON dan terkirim balik ke browser. celah keamanan nyata.
//DTO ini yang jadi "penyaring", nentuin persis data apa yang boleh keluar lewat API.
//frontend nanti butuh roleId (angka) buat nentuin menu mana yang harus di-fetch
