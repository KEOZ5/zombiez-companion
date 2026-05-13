package io.github.keoz5.zombiezcompanion.event;

import io.github.keoz5.zombiezcompanion.log.Log;
import io.github.keoz5.zombiezcompanion.log.LogCategory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Typed, synchronous, in-process event bus.
 *
 * <p>Subscribers register by event class. Publishing dispatches to every
 * subscriber registered against the runtime type. Listener errors are caught
 * and logged so one faulty module cannot break the others.
 */
public final class EventBus {

    private final Map<Class<? extends Event>, List<Consumer<? extends Event>>> subscribers = new HashMap<>();

    public <E extends Event> void subscribe(Class<E> type, Consumer<E> listener) {
        subscribers.computeIfAbsent(type, k -> new ArrayList<>()).add(listener);
    }

    @SuppressWarnings("unchecked")
    public <E extends Event> void publish(E event) {
        List<Consumer<? extends Event>> listeners = subscribers.get(event.getClass());
        if (listeners == null) return;
        for (Consumer<? extends Event> listener : listeners) {
            try {
                ((Consumer<E>) listener).accept(event);
            } catch (Throwable t) {
                Log.error("Event listener threw for " + event.getClass().getSimpleName(), t);
            }
        }
        Log.debug(LogCategory.EVENT, event.getClass().getSimpleName());
    }
}
