package org.example.controller;

import org.example.dto.MenuDTO;
import org.example.service.MenuService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/menus")
public class MenuController {

    @Autowired
    private MenuService menuService;

    @GetMapping("/me")
    public List<MenuDTO> getMyMenus(Authentication authentication) {
        return menuService.getMenusForUsername(authentication.getName());
    }
}

// Parameter Authentication authentication diisi otomatis oleh Spring dengan objek yang di-set JwtAuthFilter lewat SecurityContextHolder.getContext().setAuthentication(authToken). getName() mengembalikan username, yaitu argumen pertama yang kita masukkan ke UsernamePasswordAuthenticationToken di filter itu. Jadi username yang dipakai berasal dari token yang ditandatangani server, bukan dari kiriman client. SecurityConfig nggak perlu diubah, karena /api/menus/me sudah tercakup aturan anyRequest().authenticated().
