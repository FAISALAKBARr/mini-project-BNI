package org.example.config;

// Jam yang secara normal mengikuti jam sistem, tetapi bisa "dibekukan" di waktu tertentu
// (khusus untuk testing aturan jam absensi tanpa harus menunggu jamnya tiba).

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;

public class SettableClock extends Clock {
    private final ZoneId zone;
    private volatile Instant fixedInstant; //null = pakai jam sistem

    public SettableClock(ZoneId zone){
        this.zone = zone;
    }

    public void setFixed(Instant instant){
        this.fixedInstant = instant;
    }

    public void reset(){
        this.fixedInstant=null;
    }

    @Override
    public ZoneId getZone() {
        return zone;
    }

    @Override
    public Clock withZone(ZoneId newZone) {
        SettableClock copy = new SettableClock(newZone);
        copy.fixedInstant = this.fixedInstant;
        return copy;
    }

    @Override
    public Instant instant() {
        Instant fixed = fixedInstant;

        return fixed != null ? fixed : Instant.now();
    }
}
