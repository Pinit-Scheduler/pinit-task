package me.gg.pinit.pinittask.domain.schedule.vo;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.internal.matchers.Equals;

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
}