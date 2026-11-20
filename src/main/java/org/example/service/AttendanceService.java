package org.example.service;

import lombok.RequiredArgsConstructor;
import org.example.config.AttendanceProperties;
import org.example.dto.AttendanceCheckRequest;
import org.example.dto.AttendanceDTO;
import org.example.dto.AttendanceHistoryResponse;
import org.example.dto.AttendanceTodayDTO;
import org.example.entity.Attendance;
import org.example.entity.CheckInStatus;
import org.example.entity.User;
import org.example.exception.ApiException;
import org.example.repository.AttendanceRepository;
import org.example.repository.UserRepository;
import org.example.util.GeoUtils;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AttendanceService {
    private static final DateTimeFormatter HH_MM = DateTimeFormatter.ofPattern("HH:mm");
    private static final int MAX_RANGE_DAYS = 366;
    private static final int MAX_PAGE_SIZE = 100;
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
        String blocked = checkInBlockedReason(today, time);

        if (blocked != null) {
            throw new ApiException(HttpStatus.BAD_REQUEST, blocked);
        }
//        if (!schedule.isWorkday(today)) {
//            throw new ApiException(HttpStatus.BAD_REQUEST, " Hari ini bukan hari kerja");
//        }
//        LocalTime opensAt = schedule.checkInOpensAt(today);
//        if (time.isBefore(opensAt)) {
//            throw new ApiException(HttpStatus.BAD_REQUEST, "Check-in belum dibuka. Mulai pukul " + opensAt.format(HH_MM));
//        }
//        LocalTime closesAt = LocalTime.parse(props.getCheckin().getCloseTime());
//        if (!time.isBefore(closesAt)) {
//            throw new ApiException(HttpStatus.BAD_REQUEST, "Check-in sudah ditutup (setelah pukul "+ closesAt.format(HH_MM) + ")");
//        }
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
        attendance.setCheckInStatus(time.isAfter(schedule.lateAfter(today)) ? CheckInStatus.LATE : CheckInStatus.ON_TIME);

        Attendance saved;
        try {
            saved = attendanceRepository.saveAndFlush(attendance);
        } catch (DataIntegrityViolationException e) {
            // dua request bersamaan lolos pengecekan di atas; constraint database yang menolak yang kedua
            throw new ApiException(HttpStatus.CONFLICT, "Kamu sudah check-in hari ini");
        }
        return toDto(saved);
    }

    @Transactional
    public AttendanceDTO checkOut(String username, AttendanceCheckRequest request) {
        User user = findUser(username);
        ZonedDateTime now = ZonedDateTime.now(clock);
        LocalDate today = now.toLocalDate();

        Attendance attendance = attendanceRepository.findByUserIdAndWorkDate(user.getId(), today)
                .orElseThrow(() -> new ApiException(HttpStatus.CONFLICT, "Kamu belum check-in hari ini"));

        int distance = validateLocation(request);

        // Boleh kapan saja setelah check-in, dan boleh diulang sampai pergantian hari.
        Instant checkOutAt = now.toInstant();
        attendance.setCheckOutAt(checkOutAt);
        attendance.setCheckOutLat(request.getLatitude());
        attendance.setCheckOutLng(request.getLongitude());
        attendance.setCheckOutDistanceM(distance);
        attendance.setWorkMinutes((int) schedule.workMinutes(
                toLocal(attendance.getCheckInAt()), toLocal(checkOutAt)));

        return toDto(attendanceRepository.save(attendance));
    }

    // Dipakai checkIn() untuk menolak, dan getToday() untuk menyalakan tombol. Null berarti boleh.
    private String checkInBlockedReason(LocalDate today, LocalTime time) {
        if (!schedule.isWorkday(today)) {
            return "Hari ini bukan hari kerja";
        }
        LocalTime opensAt = schedule.checkInOpensAt(today);
        if (time.isBefore(opensAt)) {
            return "Check-in belum dibuka. Mulai pukul " + opensAt.format(HH_MM);
        }
        if (!time.isBefore(schedule.checkInClosesAt())) {
            return "Check-in sudah ditutup (setelah pukul " + schedule.checkInClosesAt().format(HH_MM) + ")";
        }
        return null;
    }

    @Transactional(readOnly = true)
    public AttendanceTodayDTO getToday(String username) {
        User user = findUser(username);
        ZonedDateTime now = ZonedDateTime.now(clock);
        LocalDate today = now.toLocalDate();
        LocalTime time = now.toLocalTime().truncatedTo(ChronoUnit.MINUTES);

        boolean workday = schedule.isWorkday(today);
        Attendance todayRecord = attendanceRepository.findByUserIdAndWorkDate(user.getId(), today).orElse(null);

        boolean canCheckIn = todayRecord == null && checkInBlockedReason(today, time) == null;
        boolean canCheckOut = todayRecord != null;

        String hint;
        if (todayRecord == null) {
            hint = checkInBlockedReason(today, time);
        } else if (todayRecord.getCheckOutAt() != null) {
            hint = "Check-out terakhir pukul " + todayRecord.getCheckOutAt().atZone(clock.getZone()).format(HH_MM)
                    + ". Masih bisa diperbarui sampai tengah malam";
        } else {
            hint = null;
        }

        return new AttendanceTodayDTO(
                now.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME),
                today.toString(),
                workday,
                workday ? schedule.officialStart(today).format(HH_MM) : null,
                workday ? schedule.checkInOpensAt(today).format(HH_MM) : null,
                workday ? schedule.lateAfter(today).format(HH_MM) : null,
                schedule.checkInClosesAt().format(HH_MM),
                schedule.workStartTime().format(HH_MM),
                canCheckIn,
                canCheckOut,
                hint,
                props.getOffice().getLatitude(),
                props.getOffice().getLongitude(),
                (int) Math.round(props.getOffice().getRadiusMeters()),
                todayRecord == null ? null : toDto(todayRecord));
    }

    @Transactional(readOnly = true)
    public AttendanceHistoryResponse getMyHistory(String username, LocalDate from, LocalDate to, int page, int size) {
        User user = findUser(username);
        return query(user.getId(), from, to, page, size);
    }

    @Transactional(readOnly = true)
    public AttendanceHistoryResponse getAllHistory(Long userId, LocalDate from, LocalDate to, int page, int size) {
        return query(userId, from, to, page, size);
    }

    // userId null = semua pegawai
    private AttendanceHistoryResponse query(Long userId, LocalDate from, LocalDate to, int page, int size){
        LocalDate end = to != null ? to : LocalDate.now(clock);
        LocalDate start = from != null ? from : end.minusDays(29);

        if (end.isBefore(start)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Tanggal akhir tidak boleh sebelum tanggal awal");
        }

        if (ChronoUnit.DAYS.between(start, end) > MAX_RANGE_DAYS) {
            throw new ApiException(HttpStatus.BAD_REQUEST,
                    "Rentang tanggal maksimal " + MAX_RANGE_DAYS + " hari per pencarian");
        }

        Pageable pageable = PageRequest.of(
                Math.max(page, 0),
                Math.min(Math.max(size, 1), MAX_PAGE_SIZE),
                Sort.by(Sort.Order.desc("workDate"), Sort.Order.asc("checkInAt")));

        Page<Attendance> result = (userId == null)
                ? attendanceRepository.findByWorkDateBetween(start, end, pageable)
                : attendanceRepository.findByUserIdAndWorkDateBetween(userId, start, end, pageable);

        Long total = (userId == null)
                ? attendanceRepository.sumWorkMinutes(start, end)
                : attendanceRepository.sumWorkMinutesByUser(userId, start, end);

        List<AttendanceDTO> content = result.getContent().stream()
                .map(this::toDto)
                .collect(Collectors.toList());

        return new AttendanceHistoryResponse(content, result.getNumber(), result.getSize(),
                result.getTotalElements(), result.getTotalPages(), total == null ? 0 : total);
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
        LocalDateTime checkInLocal = toLocal(a.getCheckInAt());
        return new AttendanceDTO(
                a.getId(),
                a.getUser().getId(),
                a.getUser().getUsername(),
                a.getWorkDate().toString(),
                formatTime(a.getCheckInAt()),
                schedule.countedStart(checkInLocal).atZone(clock.getZone())
                        .format(DateTimeFormatter.ISO_OFFSET_DATE_TIME),
                a.getCheckInStatus().name(),
                a.getCheckInDistanceM(),
                formatTime(a.getCheckOutAt()),
                a.getCheckOutDistanceM(),
                a.getWorkMinutes());
    }

    private LocalDateTime toLocal(Instant instant) {
        return instant.atZone(clock.getZone()).toLocalDateTime();
    }

    private String formatTime(Instant instant){
        return instant == null ? null
                : instant.atZone(clock.getZone()).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
    }
}
