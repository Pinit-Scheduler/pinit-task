package me.gg.pinit.pinittask.domain.schedule.vo;

import jakarta.persistence.Embeddable;

import java.util.Objects;

@Embeddable
public class ImportanceConstraint {
    private int importance;
    private int urgency;

    protected ImportanceConstraint() {}

    public ImportanceConstraint(int importance, int urgency) {
        this.importance = importance;
        this.urgency = urgency;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        ImportanceConstraint that = (ImportanceConstraint) o;
        return importance == that.importance && urgency == that.urgency;
    }

    @Override
    public int hashCode() {
        return Objects.hash(importance, urgency);
    }
}
