package me.gg.pinit.pinittask.domain.converter.service;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;

@Converter(autoApply = false)
public class InstantToDatetime6UtcConverter
        implements AttributeConverter<Instant, LocalDateTime> {

    @Override
    public LocalDateTime convertToDatabaseColumn(Instant attribute) {
        if (attribute == null) return null;

        // DATETIME(6) 정밀도(마이크로초) 맞춤
        Instant truncated = attribute.truncatedTo(ChronoUnit.MICROS);
        return LocalDateTime.ofInstant(truncated, ZoneOffset.UTC);
    }

    @Override
    public Instant convertToEntityAttribute(LocalDateTime dbData) {
        if (dbData == null) return null;

        // DB의 DATETIME(6)을 "UTC 기준 로컬시간"으로 해석
        return dbData.toInstant(ZoneOffset.UTC);
    }
}