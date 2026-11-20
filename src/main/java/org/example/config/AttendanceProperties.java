package org.example.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "attendance")
@Getter
@Setter
public class AttendanceProperties {
    private String timezone = "Asia/Jakarta";
    private double maxAccuracyMeters =300;
    private String workStartTime = "07:30";
    private Office office = new Office();
    private Checkin checkin = new Checkin();

    @Getter
    @Setter
    public static class Office{
        private double latitude;
        private double longitude;
        private double radiusMeters = 300;

    }
    @Getter
    @Setter
    public static class Checkin{
        private String startWeekday = "08:00";
        private String startFriday = "07:30";
        private String opensAt = "00:01";
        private int toleranceMinutes = 30;
        private String closeTime = "17.00";
    }

}
