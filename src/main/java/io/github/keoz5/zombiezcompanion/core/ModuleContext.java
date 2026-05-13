package io.github.keoz5.zombiezcompanion.core;

import io.github.keoz5.zombiezcompanion.config.ConfigManager;
import io.github.keoz5.zombiezcompanion.event.EventBus;

/**
 * Shared services handed to every module during {@link Module#onRegister}.
 * Kept as a record so modules see exactly what they may depend on.
 */
public record ModuleContext(ConfigManager configManager, EventBus eventBus) {
}
