package me.gg.pinit.pinittask.domain.schedule.vo;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ImportanceConstraintTest {

    @Test
    void ImportanceConstraint_Equals_같은_경우() {
        ImportanceConstraint constraint = new ImportanceConstraint(1, 2);
        ImportanceConstraint sameConstraint = new ImportanceConstraint(1, 2);

        assertEquals(constraint, sameConstraint, "ImportanceConstraint 객체가 동일한 값으로 생성되었을 때 equals 메서드는 true를 반환해야 합니다.");
    }

    @Test
    void ImportanceConstraint_Equals_서로_다른_경우(){
        //given
        ImportanceConstraint constraint = new ImportanceConstraint(1, 2);
        ImportanceConstraint differentConstraint = new ImportanceConstraint(1, 3);
        //when

        //then
        assertNotEquals(constraint, differentConstraint, "ImportanceConstraint 객체가 서로 다른 값으로 생성되었을 때 equals 메서드는 false를 반환해야 합니다.");
    }

    @DisplayName("허용되지 않은 난이도 레벨로 생성 시 예외가 발생해야 한다")
    @Test
    void DifficultyLevel_허용되지_않은_값이라면_예외_발생() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> new ImportanceConstraint(1, 4),
                "허용되지 않은 난이도 레벨로 ImportanceConstraint를 생성하면 IllegalArgumentException이 발생해야 합니다.");

        assertEquals("난이도 레벨은 1, 2, 3, 5, 8, 13, 21 중 하나여야 합니다.", exception.getMessage());
    }

    @DisplayName("허용되지 않은 난이도 레벨로 변경 시 예외가 발생해야 한다")
    @Test
    void changeDifficultyLevel_허용되지_않은_값이라면_예외_발생() {
        ImportanceConstraint constraint = new ImportanceConstraint(1, 3);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> constraint.changeDifficultyLevel(0),
                "허용되지 않은 난이도 레벨로 변경을 시도하면 IllegalArgumentException이 발생해야 합니다.");

        assertEquals("난이도 레벨은 1, 2, 3, 5, 8, 13, 21 중 하나여야 합니다.", exception.getMessage());
    }
}
