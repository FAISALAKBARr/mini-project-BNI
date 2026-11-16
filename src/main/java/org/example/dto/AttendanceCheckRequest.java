package org.example.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AttendanceCheckRequest {
    private Double latitude;
    private Double longitude;
    private Double accuracy;
}
