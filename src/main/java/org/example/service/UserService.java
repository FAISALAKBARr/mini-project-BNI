package org.example.service;

import org.example.dto.UserDTO;
import org.example.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    public List<UserDTO> getAllUsers() {
        return userRepository.findAll().stream()
                .map(u -> new UserDTO(
                        u.getId(),
                        u.getUsername(),
                        u.getRole() !=null ? u.getRole().getName() : "-"
                ))
                .collect(Collectors.toList());
    }
}
