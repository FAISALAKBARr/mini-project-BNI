package org.example.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(
        name = "attendances",
        uniqueConstraints = @UniqueConstraint(name = "uk_attendance_user_date", columnNames = {"user_id", "work_date"}),
        indexes = @Index(name = "idx_attendance_work_date", columnList = "work_date")
)
@Getter
@Setter
@NoArgsConstructor
public class Attendance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "work_date", nullable = false)
    private LocalDate workDate;

    @Column(name = "check_in_at", nullable = false)
    private Instant checkInAt;

    @Column(name = "check_in_lat")
    private Double checkInLat;

    @Column(name = "check_in_lng")
    private Double checkInLng;

    @Column(name = "check_in_distance_m")
    private Integer checkInDistanceM;

    @Enumerated(EnumType.STRING)
    @Column(name = "check_in_status", nullable = false, length = 20)
    private CheckInStatus checkInStatus;

    @Column(name = "check_out_at")
    private Instant checkOutAt;

    @Column(name = "check_out_lat")
    private Double checkOutLat;

    @Column(name = "check_out_lng")
    private Double checkOutLng;

    @Column(name = "check_out_distance_m")
    private Integer checkOutDistanceM;

    @Column(name = "work_minutes")
    private Integer workMinutes;
}
