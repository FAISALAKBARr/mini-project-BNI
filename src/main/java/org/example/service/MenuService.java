package org.example.service;

import org.example.dto.MenuDTO;
import org.example.entity.Menu;
import org.example.entity.User;
import org.example.repository.MenuRepository;
import org.example.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class MenuService {

    @Autowired
    private MenuRepository menuRepository;

    @Autowired
    private UserRepository userRepository;

    public List<MenuDTO> getMenusForUsername(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User tidak ditemukan"));

        if (user.getRole() == null){
            return List.of();
        }

        return getMenusByRole(user.getRole().getId());
    }

    public List<MenuDTO> getMenusByRole(Long roleId) {
        List<Menu> menus = menuRepository.findByRoleId(roleId);
        return menus.stream()
                .map(m -> new MenuDTO(
                        m.getId(),
                        m.getName(),
                        m.getPath(),
                        m.getParent() != null ? m.getParent().getId() : null
                ))
                .collect(Collectors.toList());
    }
}

//getMenusByRole tetap ada sebagai fungsi internal, tapi nggak ada lagi pintu dari luar yang bisa memanggilnya langsung.
