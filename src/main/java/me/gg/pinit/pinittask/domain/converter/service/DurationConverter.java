package me.gg.pinit.pinittask.domain.converter.service;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.time.Duration;

@Converter
public class DurationConverter implements AttributeConverter<Duration, Long> {

    @Override
    public Long convertToDatabaseColumn(Duration attribute) {
        return attribute == null ? null : attribute.getSeconds();
    }

    @Override
    public Duration convertToEntityAttribute(Long dbData) {
        return dbData == null ? null : Duration.ofSeconds(dbData);
    }
}
