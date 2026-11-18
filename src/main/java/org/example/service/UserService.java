package org.example.service;

import org.example.dto.CreateUserRequest;
import org.example.dto.UpdateUserRequest;
import org.example.dto.UserDTO;
import org.example.entity.Role;
import org.example.entity.User;
import org.example.exception.ApiException;
import org.example.repository.AttendanceRepository;
import org.example.repository.RoleRepository;
import org.example.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AttendanceRepository attendanceRepository;

    public List<UserDTO> getAllUsers() {
        return userRepository.findAll().stream()
                .map(u -> new UserDTO(
                        u.getId(),
                        u.getUsername(),
                        u.getRole() !=null ? u.getRole().getName() : "-"
                ))
                .collect(Collectors.toList());
    }

    public UserDTO createUser(CreateUserRequest request) {
        String username = request.getUsername() == null ? "" : request.getUsername().trim();
        String password = request.getPassword() == null ? "" : request.getPassword();
        String roleName = request.getRole() == null ? "" : request.getRole().trim();

        if (username.length() < 3) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Username minimal 3 karakter");
        }
        if (password.length() < 6) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Password minimal 6 karakter");
        }
        if (password.length() > 50) {
            // BCrypt hanya memproses 72 byte pertama, jadi dibatasi sejak awal
            throw new ApiException(HttpStatus.BAD_REQUEST, "Password minimal 50 karakter");
        }
        if (userRepository.existsByUsername(username)) {
            throw new ApiException(HttpStatus.CONFLICT, "Username sudah dipakai");
        }

        Role role = roleRepository.findByName(roleName)
                .orElseThrow(() -> new ApiException(HttpStatus.BAD_REQUEST, "Role tidak ditemukan"));

        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password));
        user.setRole(role);

        User saved = userRepository.save(user);
        return new UserDTO(saved.getId(), saved.getUsername(), role.getName());
    }

    public UserDTO updateUser(Long id, UpdateUserRequest request, String currentUsername){
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "User tidak ditemukan"));

        boolean isSelf = user.getUsername().equals(currentUsername);

        String newUsername = request.getUsername() == null ? "" : request.getUsername().trim();
        if (newUsername.length() < 3) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Username minimal 3 karakter");
        }
        if (!newUsername.equals(user.getUsername())) {
            userRepository.findByUsername(newUsername).ifPresent(existing -> {
                if (!existing.getId().equals(id)) {
                    throw new ApiException(HttpStatus.CONFLICT, "Username sudah dipakai");
                }
            });
        }

        String roleName = request.getRole() == null ? "" : request.getRole().trim();
        Role role = roleRepository.findByName(roleName)
                .orElseThrow(() -> new ApiException(HttpStatus.BAD_REQUEST, "Role tidak ditemukan"));

        if (isSelf && !role.getName().equals(user.getRole().getName())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Tidak bisa mengubah role akun yang sedang dipakai login");
        }

        user.setUsername(newUsername);
        user.setRole(role);

        if (request.getPassword() != null && !request.getPassword().isBlank()){
            if (request.getPassword().length() < 6){
                throw new ApiException(HttpStatus.BAD_REQUEST, "Password minimal 6 karakter");
            }

            user.setPassword(passwordEncoder.encode(request.getPassword()));
        }

        User saved = userRepository.save(user);
        return new UserDTO(saved.getId(), saved.getUsername(), role.getName());
    }
    //isSelf sengaja dihitung di baris paling awal, sebelum user.setUsername(...) dipanggil — supaya perbandingannya selalu memakai username asli target sebelum diedit dalam request ini, dibandingkan dengan username di token (currentUsername). Kalau dihitung belakangan, perbandingan itu bisa salah pas kasusnya justru "ganti username milik sendiri".
    //
    //Pengecekan keunikan (userRepository.findByUsername(...).ifPresent(...)) pakai pola yang sama persis dengan yang sudah kita bikin buat edit nama Role kemarin — nyari dulu, kalau ketemu tapi ID-nya beda, baru dianggap bentrok.

    public void deleteUser(Long id, String currentUsername) {
        User target = userRepository.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "User tidak ditemukan"));
        if (target.getUsername().equals(currentUsername)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Tidak bisa menghapus akun yang sedang dipakai login");
        }

        if (attendanceRepository.existsByUserId(id)) {
            throw new ApiException(HttpStatus.CONFLICT,
                    "User punya riwayat absensi, tidak bisa dihapus");
        }

        userRepository.delete(target);
    }
}

//passwordEncoder.encode(password): password mentah tidak pernah disimpan. Bean BCrypt-nya sama dengan yang dipakai matches() saat login.
//existsByUsername memberi pesan yang ramah, tapi penjaga terakhir tetap constraint unique di database.
//Di deleteUser, username akun sendiri berasal dari token (lewat authentication.getName() di controller), bukan dari client, polanya sama dengan /menus/me. Aturan "jangan hapus diri sendiri" mencegah ADMIN mengunci dirinya keluar.
