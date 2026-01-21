package me.gg.pinit.pinittask.domain.task.vo;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ImportanceConstraintTest {

    @Test
    void equals_whenValuesMatch() {
        ImportanceConstraint constraint = new ImportanceConstraint(1, 2);
        ImportanceConstraint sameConstraint = new ImportanceConstraint(1, 2);

        assertEquals(constraint, sameConstraint, "동일한 값이면 equals가 true를 반환해야 합니다.");
    }

    @Test
    void equals_whenValuesDiffer() {
        ImportanceConstraint constraint = new ImportanceConstraint(1, 2);
        ImportanceConstraint differentConstraint = new ImportanceConstraint(2, 3);

        assertNotEquals(constraint, differentConstraint, "다른 값이면 equals가 false여야 합니다.");
    }

    @DisplayName("허용되지 않은 난이도 레벨로 생성 시 예외 발생")
    @Test
    void constructor_throwsWhenDifficultyOutOfRange() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> new ImportanceConstraint(1, 4));

        assertEquals("난이도 레벨은 1, 2, 3, 5, 8, 13, 21 중 하나여야 합니다.", exception.getMessage());
    }

    @DisplayName("허용되지 않은 난이도 레벨로 변경 시 예외 발생")
    @Test
    void changeDifficulty_throwsWhenOutOfRange() {
        ImportanceConstraint constraint = new ImportanceConstraint(1, 3);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> constraint.changeDifficulty(0));

        assertEquals("난이도 레벨은 1, 2, 3, 5, 8, 13, 21 중 하나여야 합니다.", exception.getMessage());
    }
}
