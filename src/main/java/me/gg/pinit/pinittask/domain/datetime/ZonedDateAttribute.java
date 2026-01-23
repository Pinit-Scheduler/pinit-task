package me.gg.pinit.pinittask.domain.datetime;


import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Getter;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Objects;

@Getter
@Embeddable
public class ZonedDateAttribute {
    @Column(name = "date")
    private LocalDate date;

    @Column(name = "offset_id")
    private String offsetId;

    protected ZonedDateAttribute() {
    }

    private ZonedDateAttribute(LocalDate date, String offsetId) {
        this.date = date;
        this.offsetId = normalizeOffsetId(offsetId);
    }

    public static ZonedDateAttribute from(ZonedDateTime zonedDateTime) {
        Objects.requireNonNull(zonedDateTime, "zonedDateTime must not be null");
        return of(zonedDateTime.toLocalDate(), zonedDateTime.getOffset());
    }

    public static ZonedDateAttribute of(LocalDate date, ZoneOffset offset) {
        Objects.requireNonNull(date, "date must not be null");
        Objects.requireNonNull(offset, "offset must not be null");
        return new ZonedDateAttribute(date, offset.getId());
    }

    public ZonedDateTime toZonedDateTime() {
        Objects.requireNonNull(date, "dateTime must not be null");
        Objects.requireNonNull(offsetId, "offsetId must not be null");

        ZoneOffset to = getOffset();

        return date.atStartOfDay(to);
    }

    public ZoneOffset getOffset() {
        Objects.requireNonNull(offsetId, "offsetId must not be null");
        return ZoneOffset.of(normalizeOffsetId(offsetId));
    }

    private String normalizeOffsetId(String raw) {
        String trimmed = Objects.requireNonNull(raw, "offsetId must not be null").trim();
        if (trimmed.isEmpty()) {
            throw new IllegalArgumentException("offsetId must not be blank");
        }
        String upper = trimmed.toUpperCase();
        // Common UTC variants
        if (upper.equals("Z") || upper.equals("UTC") || upper.equals("UT") || upper.equals("GMT") || upper.equals("Z0") || upper.equals("UTC+0") || upper.equals("UTC+00:00") || upper.equals("UTC+0000")) {
            return "Z";
        }
        // +0900 -> +09:00
        if (upper.matches("[+-]\\d{4}")) {
            return upper.substring(0, 3) + ":" + upper.substring(3);
        }
        // +09:00 already valid
        if (upper.matches("[+-]\\d{2}:\\d{2}")) {
            return upper;
        }
        throw new IllegalArgumentException("Invalid offsetId: " + raw);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ZonedDateAttribute that = (ZonedDateAttribute) o;
        return Objects.equals(date, that.date) && Objects.equals(offsetId, that.offsetId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(date, offsetId);
    }
}
