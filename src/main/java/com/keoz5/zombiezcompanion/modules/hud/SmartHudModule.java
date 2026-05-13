package com.keoz5.zombiezcompanion.modules.hud;

import com.keoz5.zombiezcompanion.config.ModConfig;
import com.keoz5.zombiezcompanion.config.SmartHudConfig;
import com.keoz5.zombiezcompanion.events.InternalEventBus;
import com.keoz5.zombiezcompanion.events.ServerEventType;
import com.keoz5.zombiezcompanion.modules.IModule;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;

public final class SmartHudModule implements IModule {

    public static final String ID = "smart_hud";

    private SmartHudConfig config;
    private final HudState        hudState  = new HudState();
    private final HudStateService stateService = new HudStateService();
    private final HudRenderer     renderer  = new HudRenderer();

    public SmartHudModule(SmartHudConfig config, InternalEventBus eventBus) {
        this.config = config;
        // Listen to session lifecycle to show session time
        eventBus.subscribe(ServerEventType.SESSION_STARTED, e ->
                hudState.sessionStartMs = e.getPayload(Long.class));
        eventBus.subscribe(ServerEventType.SESSION_ENDED, e ->
                hudState.sessionStartMs = 0);
    }

    // ---- IModule --------------------------------------------------------

    @Override public String getId()          { return ID; }
    @Override public String getDisplayName() { return "Smart HUD"; }
    @Override public boolean isEnabled()     { return config.enabled; }
    @Override public void setEnabled(boolean v) { config.enabled = v; }

    @Override
    public void onInitialize(ModConfig modConfig) {
        this.config = modConfig.smartHud;
    }

    @Override
    public void onTick(MinecraftClient client) {
        stateService.update(client, hudState);
    }

    @Override
    public void onHudRender(DrawContext context, float tickDelta) {
        renderer.render(context, hudState, config);
    }

    @Override
    public void onGameLeave() {
        hudState.reset();
    }

    @Override
    public void onConfigChanged(ModConfig modConfig) {
        this.config = modConfig.smartHud;
    }

    public HudState getHudState()       { return hudState; }
    public SmartHudConfig getConfig()   { return config; }
}
