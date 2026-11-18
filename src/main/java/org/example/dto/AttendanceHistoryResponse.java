package org.example.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class AttendanceHistoryResponse {
    private List<AttendanceDTO> content;
    private int page;                 // dimulai dari 0
    private int size;
    private long totalElements;
    private int totalPages;
    private long totalWorkMinutes;    // akumulasi SELURUH rentang tanggal, bukan hanya halaman ini
}
