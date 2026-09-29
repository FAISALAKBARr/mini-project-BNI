package org.example.controller;

import org.example.dto.CreateRoleRequest;
import org.example.entity.Role;
import org.example.service.RoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/roles")
public class RoleController {

    @Autowired
    private RoleService roleService;

    @GetMapping
    public List<Role> getAllRoles() {
        return roleService.getAllRoles();
    }

    @PostMapping
    public ResponseEntity<Role> createRole(@RequestBody CreateRoleRequest request) {
        Role created = roleService.createRole(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRole(@PathVariable Long id){
        roleService.deleteRole(id);
        return ResponseEntity.noContent().build();
    }
}

//getAllRoles() mengembalikan List<Role> — Entity langsung, bukan DTO.
// Ini beda dari User/Menu, dan sengaja: Role cuma punya id dan name,
// tidak ada field sensitif atau relasi ke Entity lain yang perlu disaring. DTO bukan aturan "selalu wajib dipakai", tapi dipakai kalau memang ada sesuatu yang perlu disembunyikan atau disederhanakan.
