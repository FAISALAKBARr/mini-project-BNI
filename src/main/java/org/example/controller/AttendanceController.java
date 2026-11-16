package org.example.controller;

import lombok.RequiredArgsConstructor;
import org.example.dto.AttendanceCheckRequest;
import org.example.dto.AttendanceDTO;
import org.example.entity.Attendance;
import org.example.service.AttendanceService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/attendance")
@RequiredArgsConstructor
public class AttendanceController {

    private final AttendanceService attendanceService;

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
