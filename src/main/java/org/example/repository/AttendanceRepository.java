package org.example.repository;

import org.example.entity.Attendance;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.Optional;

public interface AttendanceRepository extends JpaRepository<Attendance, Long> {
    Optional<Attendance> findByUserIdAndWorkDate(Long userId, LocalDate workDate);

    boolean existsByUserId(Long userId);

    @EntityGraph(attributePaths = "user")
    Page<Attendance> findByWorkDateBetween(LocalDate from, LocalDate to, Pageable pegaeble);

    @EntityGraph(attributePaths = "user")
    Page<Attendance> findByUserIdAndWorkDateBetween(Long userId, LocalDate from, LocalDate to, Pageable pageable);

    @Query("select sum(a.workMinutes) from Attendance a where a.workDate between :startDate and :endDate")
    Long sumWorkMinutes(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    @Query("select sum(a.workMinutes) from Attendance a "
            + "where a.user.id = :userId and a.workDate between :startDate and :endDate")
    Long sumWorkMinutesByUser(@Param("userId") Long userId,
                              @Param("startDate") LocalDate startDate,
                              @Param("enDate") LocalDate endDate);
}
