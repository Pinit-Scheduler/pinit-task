package me.gg.pinit.pinittask.domain.schedule.model;

import me.gg.pinit.pinittask.domain.schedule.exception.IllegalDescriptionException;
import me.gg.pinit.pinittask.domain.schedule.exception.IllegalTitleException;
import me.gg.pinit.pinittask.domain.schedule.exception.TimeOrderReversedException;
import me.gg.pinit.pinittask.domain.schedule.patch.SchedulePatch;
import me.gg.pinit.pinittask.domain.schedule.vo.ImportanceConstraint;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.ZonedDateTime;

import static me.gg.pinit.pinittask.domain.schedule.model.ScheduleUtils.*;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ScheduleTest {

    @Test
    void setTitle() {
        //given
        Schedule schedule = getNotStartedSchedule();

        //when
        String newTitle = "New Title";
        schedule.setTitle(newTitle);

        //then
        Assertions.assertThat(schedule.getTitle()).isEqualTo(newTitle);
    }

    @Test
    void setTitle_길이_0자(){
        //given
        Schedule schedule = getNotStartedSchedule();

        //when, then
        assertThatThrownBy(() -> schedule.setTitle(""))
                .isInstanceOf(IllegalTitleException.class)
                .hasMessage("제목의 길이는 1자 이상 20자 이하여야 합니다.");
    }

    @Test
    void setTitle_null_입력(){
        //given
        Schedule schedule = getNotStartedSchedule();
        //when, then
        assertThatThrownBy(() -> schedule.setTitle(null))
                .isInstanceOf(IllegalTitleException.class)
                .hasMessage("제목의 길이는 1자 이상 20자 이하여야 합니다.");
    }
    @Test
    void setTitle_20자_초과(){
        //given
        Schedule schedule = getNotStartedSchedule();
        //when
        String longTitle = "This title is definitely more than twenty characters long";

        //then
        assertThatThrownBy(() -> schedule.setTitle(longTitle))
                .isInstanceOf(IllegalTitleException.class)
                .hasMessage("제목의 길이는 1자 이상 20자 이하여야 합니다.");
    }

    @Test
    void setDescription() {
        //given
        Schedule schedule = getNotStartedSchedule();
        //when
        String newDescription = "New Description";
        schedule.setDescription(newDescription);
        //then
        Assertions.assertThat(schedule.getDescription()).isEqualTo(newDescription);
    }
    @Test
    void setDescription_길이_0자(){
        //given
        Schedule schedule = getNotStartedSchedule();

        //when
        String shortDescription = "";

        //then
        assertThatThrownBy(() -> schedule.setDescription(shortDescription))
                .isInstanceOf(IllegalDescriptionException.class)
                .hasMessage("설명의 길이는 1자 이상 100자 이하여야 합니다.");
    }

    @Test
    void setDescription_null_입력(){
        //given
        Schedule schedule = getNotStartedSchedule();

        //when
        String nullDescription = null;

        //then
        assertThatThrownBy(() -> schedule.setDescription(nullDescription))
                .isInstanceOf(IllegalDescriptionException.class)
                .hasMessage("설명의 길이는 1자 이상 100자 이하여야 합니다.");
    }

    @Test
    void setDescription_길이_100자_초과(){
        //given
        Schedule schedule = getNotStartedSchedule();

        //when
        String longDescription = "This description is intentionally made very long to exceed the maximum allowed length of one hundred characters. It should trigger an exception.";

        //then
        assertThatThrownBy(() -> schedule.setDescription(longDescription))
                .isInstanceOf(IllegalDescriptionException.class)
                .hasMessage("설명의 길이는 1자 이상 100자 이하여야 합니다.");
    }

    @Test
    void setDate() {
        //given
        Schedule schedule = getNotStartedSchedule();
        ZonedDateTime newDate = ZonedDateTime.of(2025, 10, 2, 10, 0, 0, 0, ZonedDateTime.now().getZone());
        //when
        schedule.setStartTime(newDate);

        //then
        Assertions.assertThat(schedule.getStartTime()).isEqualTo(newDate);
    }
    
    @Test
    void setDate_데드라인_초과(){
        //given
        Schedule schedule = getNotStartedSchedule();
        ZonedDateTime invalidDate = ZonedDateTime.of(2100, 1, 1, 0, 0, 0, 0, ZonedDateTime.now().getZone());

        //when, then
        assertThatThrownBy(() -> schedule.setStartTime(invalidDate))
                .isInstanceOf(TimeOrderReversedException.class)
                .hasMessage("일정의 날짜는 데드라인을 초과할 수 없습니다.");
    }

    @Test
    void changeDeadline_일정보다_늦은_데드라인() {
        //given
        Schedule schedule = getNotStartedSchedule();
        //when
        schedule.changeDeadline(DEADLINE_TIME.plusDays(3));
        //then
        Assertions.assertThat(schedule.getTemporalConstraint().getDeadline()).isEqualTo(DEADLINE_TIME.plusDays(3));
    }

    @Test
    void changeDeadline_일정보다_빠른_데드라인() {
        //given
        Schedule schedule = getNotStartedSchedule();
        //when, then
        Assertions.assertThatThrownBy(() -> schedule.changeDeadline(ENROLLED_TIME.minusDays(1)))
                .isInstanceOf(TimeOrderReversedException.class)
                .hasMessage("데드라인은 일정 등록 날짜보다 앞설 수 없습니다.");
    }

    @Test
    void 작업_타입_변경() {
        //given
        Schedule notStartedSchedule = getNotStartedSchedule();
        //when
        notStartedSchedule.changeTaskType(TaskType.QUICK_TASK);
        //then
        Assertions.assertThat(notStartedSchedule.getTemporalConstraint().getTaskType()).isEqualTo(TaskType.QUICK_TASK);
    }

    @Test
    void Patch_수정() {
        //given
        Schedule schedule = getNotStartedSchedule();
        ImportanceConstraint importanceConstraint = schedule.getImportanceConstraint();
        String title = schedule.getTitle();
        String description = schedule.getDescription();

        //when
        SchedulePatch schedulePatch = new SchedulePatch().setDate(ENROLLED_TIME.plusDays(1)).setTaskType(TaskType.QUICK_TASK);
        schedule.patch(schedulePatch);

        //then
        Assertions.assertThat(schedule.getStartTime()).isEqualTo(ENROLLED_TIME.plusDays(1));
        Assertions.assertThat(schedule.getTemporalConstraint().getTaskType()).isEqualTo(TaskType.QUICK_TASK);
        Assertions.assertThat(schedule.getImportanceConstraint()).isEqualTo(importanceConstraint);
        Assertions.assertThat(schedule.getTitle()).isEqualTo(title);
        Assertions.assertThat(schedule.getDescription()).isEqualTo(description);
    }
}