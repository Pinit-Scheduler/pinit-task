package me.gg.pinit.pinittask.domain.member.model;

import me.gg.pinit.pinittask.domain.member.exception.ObjectiveNotNullException;
import me.gg.pinit.pinittask.domain.member.exception.ObjectiveNotPositiveException;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static me.gg.pinit.pinittask.domain.member.model.MemberUtils.getMemberSample;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MemberTest {

    @Test
    void getStartOfDay() {
        Member member = getMemberSample();

    }

    @Test
    void setDailyObjectiveWork() {
        Member member = getMemberSample();

        member.setDailyObjectiveWork(Duration.ofHours(6));

        assertThat(member.getDailyObjectiveWork()).isEqualTo(Duration.ofHours(6));
    }

    @Test
    void setDailyObjectiveWork_0분(){
        //given
        Member member = getMemberSample();

        //when, then
        assertThatThrownBy(() -> member.setDailyObjectiveWork(Duration.ofMinutes(0)))
                .isInstanceOf(ObjectiveNotPositiveException.class)
                .hasMessageContaining("일일 목표 작업 시간은 0분을 넘어야 합니다.");
    }

    @Test
    void setDailyObjectiveWork_null_입력(){
        //given
        Member member = getMemberSample();

        //when, then
        assertThatThrownBy(() -> member.setDailyObjectiveWork(null))
                .isInstanceOf(ObjectiveNotNullException.class)
                .hasMessage("일일 목표 작업 시간이 비어 있을 수 없습니다.");

    }
}