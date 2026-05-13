package com.keoz5.zombiezcompanion.events;

/**
 * A typed event flowing through the InternalEventBus.
 * Payload can be any object; use getPayload(Class) with the expected type.
 */
public final class InternalEvent {

    private final ServerEventType type;
    private final Object payload;

    public InternalEvent(ServerEventType type, Object payload) {
        this.type    = type;
        this.payload = payload;
    }

    public InternalEvent(ServerEventType type) {
        this(type, null);
    }

    public ServerEventType getType() {
        return type;
    }

    @SuppressWarnings("unchecked")
    public <T> T getPayload(Class<T> clazz) {
        return clazz.cast(payload);
    }

    public boolean hasPayload() {
        return payload != null;
    }
}
