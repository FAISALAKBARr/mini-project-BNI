package org.example.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.example.config.AttendanceProperties;
import org.example.dto.AttendanceCheckRequest;
import org.example.dto.AttendanceDTO;
import org.example.entity.Attendance;
import org.example.entity.CheckInStatus;
import org.example.entity.User;
import org.example.exception.ApiException;
import org.example.repository.AttendanceRepository;
import org.example.repository.UserRepository;
import org.example.util.GeoUtils;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

import static org.apache.catalina.manager.StatusTransformer.formatTime;

@Service
@RequiredArgsConstructor
public class AttendanceService {
    private static final DateTimeFormatter HH_MM = DateTimeFormatter.ofPattern("HH:mm");
    private final AttendanceRepository attendanceRepository;
    private final UserRepository userRepository;
    private final AttendanceProperties props;
    private final AttendanceSchedule schedule;
    private final Clock clock;

    @Transactional
    public AttendanceDTO checkIn(String username, AttendanceCheckRequest request){
        User user = findUser(username);
        ZonedDateTime now = ZonedDateTime.now(clock);
        LocalDate today = now.toLocalDate();
        LocalTime time = now.toLocalTime().truncatedTo(ChronoUnit.MINUTES);

        if (!schedule.isWorkday(today)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, " Hari ini bukan hari kerja");
        }
        LocalTime opensAt = schedule.checkInOpensAt(today);
        if (time.isBefore(opensAt)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Check-in belum dibuka. Mulai pukul " + opensAt.format(HH_MM));
        }
        LocalTime closesAt = LocalTime.parse(props.getCheckin().getCloseTime());
        if (!time.isBefore(closesAt)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Check-in sudah ditutup (setelah pukul "+ closesAt.format(HH_MM) + ")");
        }
        if (attendanceRepository.findByUserIdAndWorkDate(user.getId(), today).isPresent()) {
            throw new ApiException(HttpStatus.CONFLICT, "Kamu sudah check-in hari ini");
        }

        int distance = validateLocation(request);

        Attendance attendance = new Attendance();
        attendance.setUser(user);
        attendance.setWorkDate(today);
        attendance.setCheckInAt(now.toInstant());
        attendance.setCheckInLat(request.getLatitude());
        attendance.setCheckInLng(request.getLongitude());
        attendance.setCheckInDistanceM(distance);
        attendance.setCheckInStatus(time.isAfter(schedule.laterAfter(today)) ? CheckInStatus.LATE : CheckInStatus.ON_TIME);

        Attendance saved;
        try {
            saved = attendanceRepository.saveAndFlush(attendance);
        } catch (DataIntegrityViolationException e) {
            // dua request bersamaan lolos pengecekan di atas; constraint database yang menolak yang kedua
            throw new ApiException(HttpStatus.CONFLICT, "Kamu sudah check-in hari ini");
        }
        return toDto(saved);
    }

    public AttendanceDTO checkOut(String username, AttendanceCheckRequest request){
        User user = findUser(username);
        ZonedDateTime now = ZonedDateTime.now(clock);
        LocalDate today = now.toLocalDate();
        LocalTime time = now.toLocalTime().truncatedTo(ChronoUnit.MINUTES);

        Attendance attendance = attendanceRepository.findByUserIdAndWorkDate(user.getId(), today)
                .orElseThrow(() -> new ApiException(HttpStatus.CONFLICT, "Kamu belum check-in hari ini"));
        if (attendance.getCheckOutAt() != null){
            throw new ApiException(HttpStatus.CONFLICT, "Kamu sudah chek-out hari ini");
        }
        LocalTime opensAt = schedule.checkOutOpensAt();
        if (time.isBefore(opensAt)) {
            throw new ApiException(HttpStatus.BAD_REQUEST,
                    "Check-out baru dibuka pukul " + opensAt.format(HH_MM));
        }

        int distance = validateLocation(request);

        Instant checkOutAt = now.toInstant();
        attendance.setCheckOutAt(checkOutAt);
        attendance.setCheckOutLat(request.getLatitude());
        attendance.setCheckOutLng(request.getLongitude());
        attendance.setCheckOutDistanceM(distance);
        long minutes = Duration.between(attendance.getCheckInAt(), checkOutAt).toMinutes();
        attendance.setWorkMinutes((int) Math.max(0, minutes));

        return toDto (attendanceRepository.save(attendance));
    }

    private int validateLocation(AttendanceCheckRequest request){
        Double lat = request.getLatitude();
        Double lng = request.getLongitude();

        if (lat == null || lng == null){
            throw new ApiException(HttpStatus.BAD_REQUEST, "Koordinat lokasi (latitude dan longitude) wajib dikirim");
        }
        if (!Double.isFinite(lat) || !Double.isFinite(lng) || lat < -90 || lat > 90 || lng < -180 || lng > 180) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Kooridnat lokasi tidak valid");
        }

        Double accuracy = request.getAccuracy();
        if (accuracy != null && accuracy > props.getMaxAccuracyMeters()) {
            throw new ApiException(HttpStatus.BAD_REQUEST,
                    "Akurasi lokasi terlalu rendah (sekitar" + Math.round(accuracy) + " m). Aktifkan GPS atau coba di area terbuka");
        }

        AttendanceProperties.Office office = props.getOffice();
        double distance = GeoUtils.distanceMeters(lat, lng, office.getLatitude(), office.getLongitude());
        if (distance > office.getRadiusMeters()) {
            throw new ApiException(HttpStatus.BAD_REQUEST,
                    "Kamu berada sekitar " + Math.round(distance) + " m dari kantor. Absen hanya bisa dalam radius " + Math.round(office.getRadiusMeters()) + " m");
        }
        return (int) Math.round(distance);
    }

    private User findUser(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "Akun tidak ditemukan, silakan login ulang"));
    }

    private AttendanceDTO toDto(Attendance a) {
        return new AttendanceDTO(
                a.getId(),
                a.getUser().getId(),
                a.getUser().getUsername(),
                a.getWorkDate().toString(),
                formatTime(a.getCheckInAt()),
                a.getCheckInStatus().name(),
                a.getCheckInDistanceM(),
                formatTime(a.getCheckOutAt()),
                a.getCheckOutDistanceM(),
                a.getWorkMinutes()
        );
    }
    private String formatTime(Instant instant){
        return instant == null ? null
                : instant.atZone(clock.getZone()).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
    }
}
