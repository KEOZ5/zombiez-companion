package io.github.keoz5.zombiezcompanion.ui;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import io.github.keoz5.zombiezcompanion.ZombieZCompanionClient;

/**
 * Wires Mod Menu's "Configure" button to {@link ConfigScreen}.
 *
 * <p>Mod Menu is an optional compileOnly dependency; this class is only loaded
 * when Mod Menu is present at runtime (entry-point is declared in
 * {@code fabric.mod.json}).
 */
public final class ModMenuIntegration implements ModMenuApi {
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return parent -> new ConfigScreen(
                parent,
                ZombieZCompanionClient.configManager(),
                ZombieZCompanionClient.moduleManager()
        );
    }
}
