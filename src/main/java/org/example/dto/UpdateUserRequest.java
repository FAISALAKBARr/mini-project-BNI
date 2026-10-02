package org.example.dto;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class UpdateUserRequest {
    private String username;
    private String role;
    private String password; // opsional — null/kosong berarti password tidak diubah
}
