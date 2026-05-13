package com.keoz5.zombiezcompanion.modules;

import com.keoz5.zombiezcompanion.config.ModConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Central registry that holds all modules and broadcasts lifecycle events to them.
 * Registration order determines render/tick order.
 */
public final class ModuleRegistry {

    private final List<IModule> modules = new ArrayList<>();

    public void register(IModule module) {
        modules.add(module);
    }

    public List<IModule> getModules() {
        return Collections.unmodifiableList(modules);
    }

    /** Find a module by its concrete type (useful for inter-screen communication). */
    @SuppressWarnings("unchecked")
    public <T extends IModule> Optional<T> getModule(Class<T> type) {
        return modules.stream()
                .filter(type::isInstance)
                .map(m -> (T) m)
                .findFirst();
    }

    // ---- Broadcast methods ----------------------------------------------

    public void onTick(MinecraftClient client) {
        for (IModule m : modules) if (m.isEnabled()) m.onTick(client);
    }

    public void onHudRender(DrawContext context, float tickDelta) {
        for (IModule m : modules) if (m.isEnabled()) m.onHudRender(context, tickDelta);
    }

    public void onChatMessage(Text message, boolean isSystem) {
        for (IModule m : modules) if (m.isEnabled()) m.onChatMessage(message, isSystem);
    }

    public void onGameJoin() {
        for (IModule m : modules) m.onGameJoin();
    }

    public void onGameLeave() {
        for (IModule m : modules) m.onGameLeave();
    }

    public void onConfigChanged(ModConfig config) {
        for (IModule m : modules) m.onConfigChanged(config);
    }
}
