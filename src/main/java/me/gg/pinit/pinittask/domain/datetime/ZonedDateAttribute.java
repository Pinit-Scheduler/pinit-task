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
        this.offsetId = offsetId;
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
        return ZoneOffset.of(offsetId);
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
