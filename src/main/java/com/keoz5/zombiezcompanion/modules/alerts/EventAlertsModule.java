package com.keoz5.zombiezcompanion.modules.alerts;

import com.keoz5.zombiezcompanion.config.EventAlertsConfig;
import com.keoz5.zombiezcompanion.config.ModConfig;
import com.keoz5.zombiezcompanion.events.InternalEvent;
import com.keoz5.zombiezcompanion.events.InternalEventBus;
import com.keoz5.zombiezcompanion.events.ServerEventType;
import com.keoz5.zombiezcompanion.modules.IModule;
import com.keoz5.zombiezcompanion.parser.ChatMessageParser;
import com.keoz5.zombiezcompanion.parser.ParsedEventInfo;
import com.keoz5.zombiezcompanion.util.TextUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;

public final class EventAlertsModule implements IModule {

    public static final String ID = "event_alerts";

    private EventAlertsConfig config;
    private final InternalEventBus eventBus;
    private final AlertRenderer renderer = new AlertRenderer();

    public EventAlertsModule(EventAlertsConfig config, InternalEventBus eventBus) {
        this.config   = config;
        this.eventBus = eventBus;
    }

    // ---- IModule --------------------------------------------------------

    @Override public String getId()          { return ID; }
    @Override public String getDisplayName() { return "Event Alerts"; }
    @Override public boolean isEnabled()     { return config.enabled; }
    @Override public void setEnabled(boolean v) { config.enabled = v; }

    @Override
    public void onInitialize(ModConfig modConfig) {
        this.config = modConfig.eventAlerts;
        // Subscribe to events published by other modules (e.g. SessionTracker)
        eventBus.subscribe(ServerEventType.SESSION_STARTED, e -> renderer.clear());
    }

    @Override
    public void onChatMessage(Text message, boolean isSystem) {
        String raw = TextUtils.plain(message);
        ParsedEventInfo info = ChatMessageParser.parseEvent(raw);
        if (info == null) return;

        DetectedServerEvent event = new DetectedServerEvent(
                info.type(), info.displayName(), info.rawText(),
                System.currentTimeMillis(), info.durationSeconds()
        );

        renderer.addAlert(event, config.alertDurationMs);
        eventBus.publish(new InternalEvent(ServerEventType.GENERIC_SERVER_EVENT, event));

        if (config.playSound) {
            MinecraftClient client = MinecraftClient.getInstance();
            if (client.player != null) {
                client.getSoundManager().play(
                        PositionedSoundInstance.master(SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 0.8f)
                );
            }
        }
    }

    @Override
    public void onHudRender(DrawContext context, float tickDelta) {
        renderer.render(context, config);
    }

    @Override
    public void onGameLeave() {
        renderer.clear();
    }

    @Override
    public void onConfigChanged(ModConfig modConfig) {
        this.config = modConfig.eventAlerts;
    }

    public AlertRenderer getRenderer() { return renderer; }
}
