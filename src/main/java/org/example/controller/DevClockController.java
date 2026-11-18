package org.example.controller;

import lombok.RequiredArgsConstructor;
import org.example.config.SettableClock;
import org.example.exception.ApiException;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/dev/clock")
@ConditionalOnProperty(name = "attendance.dev.enabled", havingValue = "true")
@RequiredArgsConstructor
public class DevClockController {

    private final SettableClock clock;

    @PutMapping
    public Map<String, String> setClock(@RequestBody Map<String, String> body) {
        String text = body.get("now");
        if (text == null) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Isi body: {\"now\": \"2026-10-05T08:10:00\"}");
        }
        try {
            clock.setFixed(LocalDateTime.parse(text).atZone(clock.getZone()).toInstant());
        } catch (DateTimeParseException e){
            throw new ApiException(HttpStatus.BAD_REQUEST, "Format harus yyyy-MM-ddTHH:mm:ss, contoh 2026-10-05T08:10:00");
        }
        return currentTime();
    }

    @DeleteMapping
    public Map<String, String> resetClock() {
        clock.reset();
        return currentTime();
    }

    private Map<String, String> currentTime() {
        return Map.of("now", ZonedDateTime.now(clock).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME));
    }
}
