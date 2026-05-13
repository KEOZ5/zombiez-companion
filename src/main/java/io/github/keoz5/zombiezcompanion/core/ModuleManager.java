package io.github.keoz5.zombiezcompanion.core;

import io.github.keoz5.zombiezcompanion.config.ConfigManager;
import io.github.keoz5.zombiezcompanion.config.ModConfig;
import io.github.keoz5.zombiezcompanion.log.Log;
import io.github.keoz5.zombiezcompanion.log.LogCategory;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Owns the set of registered modules, their enable state, and lifecycle
 * dispatch.
 *
 * <p>A module's enable flag is persisted in {@link ModConfig#moduleEnabled}.
 * The flag is created with {@link Module#defaultEnabled} the first time the
 * module is registered, then never overwritten.
 *
 * <p>Every dispatch hook ({@link #onClientTick}, {@link #onChatMessage}, etc.)
 * filters to enabled modules and traps exceptions so one broken module cannot
 * disable the others.
 */
public final class ModuleManager {

    private final ConfigManager configManager;
    private final ModuleContext context;
    private final List<Module> modules = new ArrayList<>();
    private final Map<String, Module> byId = new HashMap<>();

    public ModuleManager(ConfigManager configManager, ModuleContext context) {
        this.configManager = configManager;
        this.context = context;
    }

    public void register(Module module) {
        if (byId.containsKey(module.id())) {
            throw new IllegalStateException("Duplicate module id: " + module.id());
        }
        modules.add(module);
        byId.put(module.id(), module);

        ModConfig cfg = configManager.get();
        cfg.moduleEnabled.putIfAbsent(module.id(), module.defaultEnabled());

        try {
            module.onRegister(context);
        } catch (Throwable t) {
            Log.error("Module " + module.id() + " threw during onRegister", t);
        }
        Log.debug(LogCategory.MODULE, "registered " + module.id());
    }

    /** Call once after all modules are registered. */
    public void startEnabledModules() {
        for (Module m : modules) {
            if (isEnabled(m.id())) safeEnable(m);
        }
    }

    public List<Module> modules() {
        return Collections.unmodifiableList(modules);
    }

    public Optional<Module> findById(String id) {
        return Optional.ofNullable(byId.get(id));
    }

    public boolean isEnabled(String id) {
        return Boolean.TRUE.equals(configManager.get().moduleEnabled.get(id));
    }

    public void setEnabled(String id, boolean enabled) {
        Module m = byId.get(id);
        if (m == null) return;
        boolean was = isEnabled(id);
        if (was == enabled) return;
        configManager.get().moduleEnabled.put(id, enabled);
        configManager.save();
        if (enabled) safeEnable(m);
        else safeDisable(m);
    }

    private void safeEnable(Module m) {
        try { m.onEnable(); Log.debug(LogCategory.MODULE, "enabled " + m.id()); }
        catch (Throwable t) { Log.error("Module " + m.id() + " threw during onEnable", t); }
    }

    private void safeDisable(Module m) {
        try { m.onDisable(); Log.debug(LogCategory.MODULE, "disabled " + m.id()); }
        catch (Throwable t) { Log.error("Module " + m.id() + " threw during onDisable", t); }
    }

    // ── Dispatch hooks ────────────────────────────────────────────────────

    public void onClientTick(MinecraftClient client) {
        for (Module m : modules) {
            if (!isEnabled(m.id())) continue;
            try { m.onClientTick(client); }
            catch (Throwable t) { Log.error("Module " + m.id() + " threw onClientTick", t); }
        }
    }

    public void onChatMessage(Text message, boolean overlay) {
        for (Module m : modules) {
            if (!isEnabled(m.id())) continue;
            try { m.onChatMessage(message, overlay); }
            catch (Throwable t) { Log.error("Module " + m.id() + " threw onChatMessage", t); }
        }
    }

    public void onHudRender(DrawContext drawContext, float tickDelta) {
        for (Module m : modules) {
            if (!isEnabled(m.id())) continue;
            try { m.onHudRender(drawContext, tickDelta); }
            catch (Throwable t) { Log.error("Module " + m.id() + " threw onHudRender", t); }
        }
    }

    public void onJoinWorld() {
        for (Module m : modules) {
            if (!isEnabled(m.id())) continue;
            try { m.onJoinWorld(); }
            catch (Throwable t) { Log.error("Module " + m.id() + " threw onJoinWorld", t); }
        }
    }

    public void onLeaveWorld() {
        for (Module m : modules) {
            if (!isEnabled(m.id())) continue;
            try { m.onLeaveWorld(); }
            catch (Throwable t) { Log.error("Module " + m.id() + " threw onLeaveWorld", t); }
        }
    }
}
