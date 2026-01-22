package me.gg.pinit.pinittask.domain.task.vo;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Getter;

import java.util.Objects;
import java.util.Set;

@Getter
@Embeddable
public class ImportanceConstraint {
    private static final Set<Integer> VALID_DIFFICULTY_LEVELS = Set.of(1, 2, 3, 5, 8, 13, 21);

    @Column(name = "importance_level")
    private int importance;

    @Column(name = "difficulty_level")
    private int difficulty;

    protected ImportanceConstraint() {
    }

    public ImportanceConstraint(int importance, int difficulty) {
        validateImportanceLevel(importance);
        this.importance = importance;
        validateDifficultyLevel(difficulty);
        this.difficulty = difficulty;
    }

    public ImportanceConstraint changeImportance(int importance) {
        validateImportanceLevel(importance);
        return new ImportanceConstraint(importance, this.difficulty);
    }

    public ImportanceConstraint changeDifficultyLevel(int difficulty) {
        validateDifficultyLevel(difficulty);
        return new ImportanceConstraint(this.importance, difficulty);
    }

    public ImportanceConstraint changeDifficulty(int difficulty) {
        return changeDifficultyLevel(difficulty);
    }
    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        ImportanceConstraint that = (ImportanceConstraint) o;
        return importance == that.importance && difficulty == that.difficulty;
    }

    @Override
    public int hashCode() {
        return Objects.hash(importance, difficulty);
    }

    private void validateImportanceLevel(int level) {
        if (level < 1 || level > 9) {
            throw new IllegalArgumentException("중요도 레벨은 1에서 9 사이여야 합니다.");
        }
    }

    private void validateDifficultyLevel(int difficulty) {
        if (!VALID_DIFFICULTY_LEVELS.contains(difficulty)) {
            throw new IllegalArgumentException("난이도 레벨은 1, 2, 3, 5, 8, 13, 21 중 하나여야 합니다.");
        }
    }
}
