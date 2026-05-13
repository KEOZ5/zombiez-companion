package io.github.keoz5.zombiezcompanion.event;

/**
 * Marker for typed events published through {@link EventBus}.
 *
 * <p>Implementations are normally {@code record}s so payload shape is part of
 * the type. Subscribers register against a concrete event class and receive
 * only instances of that class.
 */
public interface Event {
}
