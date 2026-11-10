//
//package org.example.config;
//
//import org.example.entity.Role;
//import org.example.entity.User;
//import org.example.repository.RoleRepository;
//import org.example.repository.UserRepository;
//import org.springframework.boot.CommandLineRunner;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.security.crypto.password.PasswordEncoder;
//
//@Configuration
//public class AdminInitializer {
//
//    @Bean
//    CommandLineRunner initializeAdmin(
//            UserRepository userRepository,
//            RoleRepository roleRepository,
//            PasswordEncoder passwordEncoder
//    ) {
//        return args -> {
//            // Jangan membuat akun jika sudah ada user.
//            if (userRepository.count() > 0) {
//                System.out.println(
//                        "Admin initializer dilewati: user sudah tersedia."
//                );
//                return;
//            }
//
//            String username = System.getenv("INITIAL_ADMIN_USERNAME");
//            String password = System.getenv("INITIAL_ADMIN_PASSWORD");
//
//            if (username == null || username.isBlank()
//                    || password == null || password.length() < 6) {
//                throw new IllegalStateException(
//                        "Atur INITIAL_ADMIN_USERNAME dan "
//                                + "INITIAL_ADMIN_PASSWORD sebelum menjalankan aplikasi."
//                );
//            }
//
//            Role adminRole = roleRepository.findByName("ADMIN")
//                    .orElseThrow(() -> new IllegalStateException(
//                            "Role ADMIN belum tersedia di database."
//                    ));
//
//            User admin = new User();
//            admin.setUsername(username.trim());
//            admin.setPassword(passwordEncoder.encode(password));
//            admin.setRole(adminRole);
//
//            userRepository.save(admin);
//
//            System.out.println(
//                    "Akun admin awal berhasil dibuat: " + username.trim()
//            );
//        };
//    }
//}
