package me.gg.pinit.pinittask.domain.schedule.model;

import me.gg.pinit.pinittask.domain.schedule.exception.IllegalDescriptionException;
import me.gg.pinit.pinittask.domain.schedule.exception.IllegalTitleException;
import me.gg.pinit.pinittask.domain.schedule.patch.SchedulePatch;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.ZonedDateTime;

import static me.gg.pinit.pinittask.domain.schedule.model.ScheduleUtils.ENROLLED_TIME;
import static me.gg.pinit.pinittask.domain.schedule.model.ScheduleUtils.getNotStartedSchedule;
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
        schedule.setDesignatedStartTime(newDate);

        //then
        Assertions.assertThat(schedule.getDesignatedStartTime()).isEqualTo(newDate);
    }
    @Test
    void setDate_null_throws() {
        Schedule schedule = getNotStartedSchedule();
        assertThatThrownBy(() -> schedule.setDesignatedStartTime(null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void Patch_수정() {
        //given
        Schedule schedule = getNotStartedSchedule();
        ZonedDateTime changedDate = ENROLLED_TIME.plusDays(1);
        String newTitle = "Patched title";
        String newDesc = "patched desc";

        //when
        SchedulePatch schedulePatch = new SchedulePatch()
                .setDate(changedDate)
                .setTitle(newTitle)
                .setDescription(newDesc);
        schedule.patch(schedulePatch);

        //then
        Assertions.assertThat(schedule.getDesignatedStartTime()).isEqualTo(changedDate);
        Assertions.assertThat(schedule.getTitle()).isEqualTo(newTitle);
        Assertions.assertThat(schedule.getDescription()).isEqualTo(newDesc);
    }
}
