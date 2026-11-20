package org.example.config;

import org.example.service.AttendanceSchedule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalTime;
import java.time.ZoneId;

@Configuration
public class AttendanceConfig {

    @Bean
    public SettableClock clock(AttendanceProperties props){
        return new SettableClock(ZoneId.of(props.getTimezone()));
    }

    @Bean
    public AttendanceSchedule attendanceSchedule(AttendanceProperties props) {
        AttendanceProperties.Checkin checkin = props.getCheckin();
        return new AttendanceSchedule(
                LocalTime.parse(checkin.getStartWeekday()),
                LocalTime.parse(checkin.getStartFriday()),
                LocalTime.parse(checkin.getOpensAt()),
                checkin.getToleranceMinutes(),
                LocalTime.parse(checkin.getCloseTime()),
                LocalTime.parse(props.getWorkStartTime())
        );
    }
}
