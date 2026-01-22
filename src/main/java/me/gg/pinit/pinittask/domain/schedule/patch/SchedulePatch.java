package me.gg.pinit.pinittask.domain.schedule.patch;

import me.gg.pinit.pinittask.domain.schedule.model.ScheduleType;

import java.time.ZonedDateTime;
import java.util.Optional;

public final class SchedulePatch {
    private String title;
    private String description;
    private ZonedDateTime designatedStartTime;
    private ScheduleType scheduleType;

    public SchedulePatch setTitle(String v) {
        this.title = v;
        return this;
    }

    public SchedulePatch setDescription(String v) {
        this.description = v;
        return this;
    }

    public SchedulePatch setDesignatedStartTime(ZonedDateTime v) {
        this.designatedStartTime = v;
        return this;
    }

    public SchedulePatch setScheduleType(ScheduleType v) {
        this.scheduleType = v;
        return this;
    }

    public Optional<String> title() {
        return Optional.ofNullable(title);
    }

    public Optional<String> description() {
        return Optional.ofNullable(description);
    }

    public Optional<ZonedDateTime> designatedStartTime() {
        return Optional.ofNullable(designatedStartTime);
    }

    public Optional<ScheduleType> scheduleType() {
        return Optional.ofNullable(scheduleType);
    }

    public boolean isEmpty() {
        return title == null && description == null && designatedStartTime == null && scheduleType == null;
    }
}
