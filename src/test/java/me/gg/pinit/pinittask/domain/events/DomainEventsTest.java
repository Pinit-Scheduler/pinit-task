package me.gg.pinit.pinittask.domain.events;

import org.junit.jupiter.api.Test;

import java.util.Deque;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

class DomainEventsTest {

    @Test
    void collectsAndClearsEventsInOrder() {
        //given
        DummyEvent first = new DummyEvent("first");
        DummyEvent second = new DummyEvent("second");
        DomainEvents.raise(first);
        DomainEvents.raise(second);

        //when
        Deque<DomainEvent> events = DomainEvents.getEventsAndClear();

        //then
        assertThat(events).containsExactly(first, second);
    }

    @Test
    void keepsEventsIsolatedPerThread() throws Exception {
        //given
        DummyEvent mainThreadEvent = new DummyEvent("main");
        DummyEvent otherThreadEvent = new DummyEvent("other");
        DomainEvents.raise(mainThreadEvent);

        //when
        Deque<DomainEvent> otherThreadEvents = CompletableFuture.supplyAsync(() -> {
            DomainEvents.raise(otherThreadEvent);
            return DomainEvents.getEventsAndClear();
        }).get(1, TimeUnit.SECONDS);

        //then
        assertThat(otherThreadEvents).containsExactly(otherThreadEvent);
        assertThat(DomainEvents.getEventsAndClear()).containsExactly(mainThreadEvent);
    }

    private record DummyEvent(String name) implements DomainEvent {
    }
}
