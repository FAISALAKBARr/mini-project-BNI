package org.example.controller;

import lombok.RequiredArgsConstructor;
import org.example.dto.AttendanceCheckRequest;
import org.example.dto.AttendanceDTO;
import org.example.dto.AttendanceHistoryResponse;
import org.example.dto.AttendanceTodayDTO;
import org.example.service.AttendanceService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/attendance")
@RequiredArgsConstructor
public class AttendanceController {

    private final AttendanceService attendanceService;

    @GetMapping("/today")
    public AttendanceTodayDTO today(Authentication authentication) {
        return attendanceService.getToday(authentication.getName());
    }

    @GetMapping("/me")
    public AttendanceHistoryResponse myHistory(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            Authentication authentication) {
        return attendanceService.getMyHistory(authentication.getName(), from, to, page, size);
    }

    @PostMapping("/check-in")
    public ResponseEntity<AttendanceDTO> checkIn(@RequestBody AttendanceCheckRequest request, Authentication authentication) {
        AttendanceDTO created = attendanceService.checkIn(authentication.getName(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PostMapping("/check-out")
    public AttendanceDTO checkOut(@RequestBody AttendanceCheckRequest request, Authentication authentication) {
        return attendanceService.checkOut(authentication.getName(), request);
    }
}
