package me.gg.pinit.pinittask.domain.datetime;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Objects;

@Embeddable
public class ZonedDateTimeAttribute {
    @Column(name = "date_time")
    private LocalDateTime dateTime;

    @Column(name = "zone_id")
    private String zoneId;

    protected ZonedDateTimeAttribute() {
    }

    private ZonedDateTimeAttribute(LocalDateTime dateTime, String zoneId) {
        this.dateTime = dateTime;
        this.zoneId = zoneId;
    }

    public static ZonedDateTimeAttribute from(ZonedDateTime zonedDateTime) {
        Objects.requireNonNull(zonedDateTime, "zonedDateTime must not be null");
        return new ZonedDateTimeAttribute(zonedDateTime.toLocalDateTime(), zonedDateTime.getZone().getId());
    }

    public ZonedDateTime toZonedDateTime() {
        Objects.requireNonNull(dateTime, "dateTime must not be null");
        Objects.requireNonNull(zoneId, "zoneId must not be null");
        return dateTime.atZone(ZoneId.of(zoneId));
    }

    public LocalDateTime getDateTime() {
        return dateTime;
    }

    public String getZoneId() {
        return zoneId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ZonedDateTimeAttribute that = (ZonedDateTimeAttribute) o;
        return Objects.equals(dateTime, that.dateTime) && Objects.equals(zoneId, that.zoneId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(dateTime, zoneId);
    }
}
