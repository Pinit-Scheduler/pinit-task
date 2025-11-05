package me.gg.pinit.pinittask.domain.converter.service;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import me.gg.pinit.pinittask.domain.schedule.model.*;

@Converter
public class ScheduleStateConverter implements AttributeConverter<ScheduleState, String> {

    @Override
    public String convertToDatabaseColumn(ScheduleState attribute) {
        return attribute.toString();
    }

    @Override
    public ScheduleState convertToEntityAttribute(String dbData) {
        return switch(dbData) {
            case NotStartedState.NOT_STARTED -> new NotStartedState();
            case InProgressState.IN_PROGRESS -> new InProgressState();
            case SuspendedState.SUSPENDED -> new SuspendedState();
            case CompletedState.COMPLETED -> new CompletedState();
            default -> throw new IllegalArgumentException("Unknown ScheduleState: " + dbData);
        };
    }
}
