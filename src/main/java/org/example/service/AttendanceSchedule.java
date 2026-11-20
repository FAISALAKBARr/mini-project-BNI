package org.example.service;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;

/** Aturan jam absensi (murni logika, tanpa Spring, supaya mudah diuji). */
public class AttendanceSchedule {

    private final LocalTime startWeekday;      // jam resmi Senin-Kamis
    private final LocalTime startFriday;       // jam resmi Jumat
    private final LocalTime checkInOpensAt;    // check-in dibuka (awal hari, 00:01)
    private final int toleranceMinutes;        // batas flex: lewat dari ini dianggap terlambat
    private final LocalTime checkInClosesAt;   // check-in ditutup mulai jam ini
    private final LocalTime workStartTime;     // perhitungan jam kerja paling awal dimulai dari jam ini

    public AttendanceSchedule(LocalTime startWeekday, LocalTime startFriday, LocalTime checkInOpensAt,
                              int toleranceMinutes, LocalTime checkInClosesAt, LocalTime workStartTime) {
        this.startWeekday = startWeekday;
        this.startFriday = startFriday;
        this.checkInOpensAt = checkInOpensAt;
        this.toleranceMinutes = toleranceMinutes;
        this.checkInClosesAt = checkInClosesAt;
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

    public LocalTime checkInClosesAt() {
        return checkInClosesAt;
    }

    public LocalTime workStartTime() {
        return workStartTime;
    }

    /** Titik mulai perhitungan jam kerja: jam check-in, tapi tidak lebih awal dari workStartTime. */
    public LocalDateTime countedStart(LocalDateTime checkIn) {
        LocalDateTime floor = checkIn.toLocalDate().atTime(workStartTime);
        return checkIn.isBefore(floor) ? floor : checkIn;
    }

    /** Total menit kerja (detik diabaikan). Tidak pernah negatif. */
    public long workMinutes(LocalDateTime checkIn, LocalDateTime checkOut) {
        LocalDateTime start = countedStart(checkIn).truncatedTo(ChronoUnit.MINUTES);
        LocalDateTime end = checkOut.truncatedTo(ChronoUnit.MINUTES);
        return Math.max(0, Duration.between(start, end).toMinutes());
    }
}