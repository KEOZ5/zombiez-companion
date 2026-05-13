package com.keoz5.zombiezcompanion.ui;

import com.keoz5.zombiezcompanion.ZombieZCompanionClient;
import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;

/**
 * Hooks into Mod Menu so users can open ZZC settings from the mods list.
 * This class is only loaded when Mod Menu is installed (via the "modmenu" entrypoint in fabric.mod.json).
 */
public final class ModMenuIntegration implements ModMenuApi {

    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return parent -> new MainConfigScreen(
                parent,
                ZombieZCompanionClient.getConfigManager(),
                ZombieZCompanionClient.getModuleRegistry()
        );
    }
}
