package me.gg.pinit.pinittask.domain.events;

import java.util.ArrayDeque;
import java.util.Deque;

public class DomainEvents {
    private static final ThreadLocal<Deque<DomainEvent>> events = ThreadLocal.withInitial(ArrayDeque::new);

    public static void raise(DomainEvent event) {
        events.get().add(event);
    }

    public static Deque<DomainEvent> getEventsAndClear() {
        Deque<DomainEvent> target = new ArrayDeque<>(events.get());
        events.remove();
        return target;
    }
}
