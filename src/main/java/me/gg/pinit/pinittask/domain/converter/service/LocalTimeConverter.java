package me.gg.pinit.pinittask.domain.converter.service;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.time.LocalTime;

@Converter
public class LocalTimeConverter implements AttributeConverter<LocalTime, String> {
    @Override
    public String convertToDatabaseColumn(LocalTime attribute) {
        return attribute == null ? null : attribute.toString();
    }

    @Override
    public LocalTime convertToEntityAttribute(String dbData) {
        return dbData == null ? null : LocalTime.parse(dbData);
    }
}
