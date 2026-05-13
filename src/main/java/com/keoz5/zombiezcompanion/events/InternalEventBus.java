package com.keoz5.zombiezcompanion.events;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Simple synchronous event bus for cross-module communication.
 * Modules subscribe on init and publish whenever they detect something relevant.
 */
public final class InternalEventBus {

    private final Map<ServerEventType, List<Consumer<InternalEvent>>> listeners =
            new EnumMap<>(ServerEventType.class);

    public void subscribe(ServerEventType type, Consumer<InternalEvent> listener) {
        listeners.computeIfAbsent(type, k -> new ArrayList<>()).add(listener);
    }

    public void publish(InternalEvent event) {
        List<Consumer<InternalEvent>> list = listeners.get(event.getType());
        if (list != null) {
            for (Consumer<InternalEvent> l : list) {
                try {
                    l.accept(event);
                } catch (Exception e) {
                    // Never let a bad listener crash the game
                    com.keoz5.zombiezcompanion.util.ModLogger.error(
                            "EventBus listener error: " + e.getMessage());
                }
            }
        }
    }
}
