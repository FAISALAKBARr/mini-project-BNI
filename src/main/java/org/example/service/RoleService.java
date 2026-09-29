package org.example.service;

import org.example.dto.CreateRoleRequest;
import org.example.entity.Role;
import org.example.exception.ApiException;
import org.example.repository.MenuRepository;
import org.example.repository.RoleRepository;
import org.example.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RoleService {

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MenuRepository menuRepository;

    public List<Role> getAllRoles() {
        return roleRepository.findAll();
    }

    public Role createRole(CreateRoleRequest request) {
        String name = request.getName() == null ? "" : request.getName().trim().toUpperCase();

        if (name.length() < 3) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Nama role minimal 3 karakter");
        }
        if (roleRepository.findByName(name).isPresent()) {
            throw new ApiException(HttpStatus.CONFLICT, "Role sudah ada");
        }

        Role role = new Role();
        role.setName(name);
        return roleRepository.save(role);
    }

    public void deleteRole(Long id) {
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Role tidak ditemukan"));

        if (userRepository.existsByRoleId(id)) {
            throw new ApiException(HttpStatus.CONFLICT, "Role masih dipakai oleh user, tidak bisa dihapus");
        }
        if (menuRepository.existsByRoleId(id)) {
            throw new ApiException(HttpStatus.CONFLICT, "Role masih dipakai oleh menu, tidak bisa dihapus");
        }

        roleRepository.delete(role);
    }
}

//.toUpperCase() supaya nama role konsisten dengan ADMIN/STAFF yang sudah ada, apa pun huruf besar-kecil yang diketik di form nanti.
