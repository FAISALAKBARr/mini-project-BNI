package org.example.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AttendanceDTO {
    private Long id;
    private Long userId;
    private String username;
    private String workDate; // "2026-10-05"
    private String checkInAt; // "2026-10-05T08:10:00+07:00"
    private String checkInStatus; // ON_TIME / LATE
    private Integer checkInDistanceMeters;
    private String checkOutAt; // null sampai check-out
    private Integer checkOutDistanceMeters;
    private Integer workMinutes; // diisi saat check-out
}
