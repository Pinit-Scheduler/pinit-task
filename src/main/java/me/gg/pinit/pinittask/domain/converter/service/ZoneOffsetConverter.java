package me.gg.pinit.pinittask.domain.converter.service;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.time.ZoneOffset;

@Converter
public class ZoneOffsetConverter implements AttributeConverter<ZoneOffset, String> {

    @Override
    public String convertToDatabaseColumn(ZoneOffset attribute) {
        return attribute == null ? null : attribute.getId();
    }

    @Override
    public ZoneOffset convertToEntityAttribute(String dbData) {
        return dbData == null ? null : ZoneOffset.of(dbData);
    }
}
