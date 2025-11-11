package me.gg.pinit.pinittask.domain.schedule.vo;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Getter;

import java.util.Objects;

@Getter
@Embeddable
public class ImportanceConstraint {
    @Column(name = "importance_level")
    private int importance;

    @Column(name = "urgency_level")
    private int urgency;

    protected ImportanceConstraint() {}

    public ImportanceConstraint(int importance, int urgency) {
        this.importance = importance;
        this.urgency = urgency;
    }

    public ImportanceConstraint changeImportance(int importance) {
        validateLevel(importance);
        return new ImportanceConstraint(importance, this.urgency);
    }

    public ImportanceConstraint changeUrgency(int urgency) {
        validateLevel(urgency);
        return new ImportanceConstraint(this.importance, urgency);
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

    private void validateLevel(int level) {
        if (level < 1 || level > 9) {
            throw new IllegalArgumentException("중요도와 긴급도 레벨은 1에서 9 사이여야 합니다.");
        }
    }
}
