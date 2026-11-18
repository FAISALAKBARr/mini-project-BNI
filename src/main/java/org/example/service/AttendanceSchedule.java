package org.example.service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;

public class AttendanceSchedule {
    private final LocalTime startWeekday;
    private final LocalTime startFriday;
    private final int openBeforeMinutes;
    private final int toleranceMinutes;
    private final LocalTime cheOutOpensAt;

    public AttendanceSchedule(LocalTime startWeekday, LocalTime startFriday, int openBeforeMinutes, int toleranceMinutes, LocalTime cheOutOpensAt){
        this.startWeekday = startWeekday;
        this.startFriday = startFriday;
        this.openBeforeMinutes = openBeforeMinutes;
        this.toleranceMinutes = toleranceMinutes;
        this.cheOutOpensAt = cheOutOpensAt;
    }

    public boolean isWorkday(LocalDate date){
        DayOfWeek day = date.getDayOfWeek();
        return day != DayOfWeek.SATURDAY && day != DayOfWeek.SUNDAY;
    }

    public LocalTime officialStart(LocalDate date){
        return date.getDayOfWeek() == DayOfWeek.FRIDAY ? startFriday : startWeekday;
    }

    public LocalTime checkInOpensAt(LocalDate date) {
        return officialStart(date).minusMinutes(openBeforeMinutes);
    }

    public LocalTime lateAfter(LocalDate date){
        return officialStart(date).plusMinutes(toleranceMinutes);
    }

    public LocalTime checkOutOpensAt(){
        return cheOutOpensAt;
    }

}
