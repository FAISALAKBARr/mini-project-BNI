package org.example.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AttendanceTodayDTO {
    private String serverTime;        // jam server (zona kantor), format ISO
    private String workDate;
    private boolean workday;
    private String officialStart;     // "08:00" / "07:30", null kalau bukan hari kerja
    private String checkInOpensAt;
    private String lateAfter;
    private String checkOutOpensAt;
    private String workStartTime;      // "07:30", untuk teks "jam kerja dihitung mulai ..."
    private boolean canCheckIn;       // menurut jam dan status. Lokasi dicek terpisah
    private boolean canCheckOut;
    private String hint;              // alasan tombol mati, atau null
    private double officeLatitude;
    private double officeLongitude;
    private int radiusMeters;
    private AttendanceDTO today;      // record hari ini, null kalau belum check-in
}
