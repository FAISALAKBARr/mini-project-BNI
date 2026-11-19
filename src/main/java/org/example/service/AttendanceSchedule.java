package org.example.service;

import java.time.*;
import java.time.temporal.ChronoUnit;

public class AttendanceSchedule {
    private final LocalTime startWeekday;
    private final LocalTime startFriday;
    private final LocalTime checkInOpensAt;
    private final int toleranceMinutes;
    private final LocalTime checkOutOpensAt;
    private final LocalTime workStartTime;

    public AttendanceSchedule(LocalTime startWeekday, LocalTime startFriday, LocalTime checkInOpensAt, int toleranceMinutes, LocalTime checkOutOpensAt, LocalTime workStartTime){
        this.startWeekday = startWeekday;
        this.startFriday = startFriday;
        this.checkInOpensAt = checkInOpensAt;
        this.toleranceMinutes = toleranceMinutes;
        this.checkOutOpensAt = checkOutOpensAt;
        this.workStartTime = workStartTime;
    }

    public boolean isWorkday(LocalDate date) {
        DayOfWeek day = date.getDayOfWeek();
        return day != DayOfWeek.SATURDAY && day != DayOfWeek.SUNDAY;
    }

    public LocalTime officialStart(LocalDate date) {
        return date.getDayOfWeek() == DayOfWeek.FRIDAY ? startFriday : startWeekday;
    }

    public LocalTime checkInOpensAt(LocalDate date) {
        return checkInOpensAt;
    }

    public LocalTime lateAfter(LocalDate date) {
        return officialStart(date).plusMinutes(toleranceMinutes);
    }

    public LocalTime checkOutOpensAt() {
        return checkOutOpensAt;
    }

    public LocalTime workStartTime() {
        return workStartTime;
    }

    // Titik mulai perhitungan jam kerja: jam check-in, tapi tidak lebih awal dari workStartTime.
    public LocalDateTime countedStart(LocalDateTime checkIn) {
        LocalDateTime floor = checkIn.toLocalDate().atTime(workStartTime);
        return checkIn.isBefore(floor) ? floor : checkIn;
    }

    // Total menit kerja (detik diabaikan). Tidak pernah negatif.
    public long workMinutes(LocalDateTime checkIn, LocalDateTime checkOut) {
        LocalDateTime start = countedStart(checkIn).truncatedTo(ChronoUnit.MINUTES);
        LocalDateTime end = checkOut.truncatedTo(ChronoUnit.MINUTES);
        return Math.max(0, Duration.between(start, end).toMinutes());
    }
}
