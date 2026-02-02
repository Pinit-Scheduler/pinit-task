package me.gg.pinit.pinittask.domain.datetime;


import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Objects;

@Embeddable
public class ZonedDateAttribute {
    @Column(name = "date")
    private LocalDate date;

    @Column(name = "offset_id")
    private String offsetId;

    @Column(name = "zone_id")
    private String zoneId;

    protected ZonedDateAttribute() {
    }

    private ZonedDateAttribute(LocalDate date, String offsetId, String zoneId) {
        this.date = Objects.requireNonNull(date, "date must not be null");
        this.offsetId = Objects.requireNonNull(offsetId, "offsetId must not be null");
        this.zoneId = Objects.requireNonNull(zoneId, "zoneId must not be null");
    }

    public static ZonedDateAttribute from(ZonedDateTime zonedDateTime) {
        Objects.requireNonNull(zonedDateTime, "zonedDateTime must not be null");
        return new ZonedDateAttribute(
                zonedDateTime.toLocalDate(),
                zonedDateTime.getOffset().getId(),
                zonedDateTime.getZone().getId()
        );
    }

    public static ZonedDateAttribute of(LocalDate date, ZoneId zoneId) {
        Objects.requireNonNull(zoneId, "zoneId must not be null");
        ZoneOffset offset = date.atStartOfDay(zoneId).getOffset();
        return new ZonedDateAttribute(date, offset.getId(), zoneId.getId());
    }

    public static ZonedDateAttribute of(LocalDate date, ZoneId zoneId, ZoneOffset offset) {
        Objects.requireNonNull(date, "date must not be null");
        Objects.requireNonNull(zoneId, "zoneId must not be null");
        Objects.requireNonNull(offset, "offset must not be null");
        ZoneOffset expected = date.atStartOfDay(zoneId).getOffset();
        if (!expected.equals(offset)) {
            throw new IllegalArgumentException("제공된 offset이 zoneId의 규칙과 일치하지 않습니다.");
        }
        return new ZonedDateAttribute(date, offset.getId(), zoneId.getId());
    }

    public ZonedDateTime toZonedDateTime() {
        ZoneId zone = getZoneId();
        return date.atStartOfDay(zone);
    }

    public ZoneOffset getOffset() {
        ZoneId zone = getZoneId();
        return date.atStartOfDay(zone).getOffset();
    }

    public LocalDate getDate() {
        return date;
    }

    public String getOffsetId() {
        return offsetId;
    }

    public ZoneId getZoneId() {
        Objects.requireNonNull(zoneId, "zoneId must not be null");
        return ZoneId.of(zoneId);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ZonedDateAttribute that = (ZonedDateAttribute) o;
        return Objects.equals(date, that.date)
                && Objects.equals(offsetId, that.offsetId)
                && Objects.equals(zoneId, that.zoneId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(date, offsetId, zoneId);
    }
}
