package me.gg.pinit.pinittask.domain.datetime;


import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Objects;

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
        return new ZonedDateAttribute(zonedDateTime.toLocalDate(), zonedDateTime.getOffset().getId());
    }

    public ZonedDateTime toZonedDateTime() {
        Objects.requireNonNull(date, "dateTime must not be null");
        Objects.requireNonNull(offsetId, "offsetId must not be null");

        ZoneOffset to = ZoneOffset.of(offsetId);

        return date.atStartOfDay(to);
    }

    public LocalDate getDate() {
        return date;
    }

    public String getOffsetId() {
        return offsetId;
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
