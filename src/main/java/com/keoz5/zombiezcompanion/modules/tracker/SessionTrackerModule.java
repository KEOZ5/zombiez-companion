package com.keoz5.zombiezcompanion.modules.tracker;

import com.keoz5.zombiezcompanion.config.ModConfig;
import com.keoz5.zombiezcompanion.config.SessionTrackerConfig;
import com.keoz5.zombiezcompanion.events.InternalEventBus;
import com.keoz5.zombiezcompanion.events.ServerEventType;
import com.keoz5.zombiezcompanion.modules.IModule;
import com.keoz5.zombiezcompanion.parser.ChatMessageParser;
import com.keoz5.zombiezcompanion.parser.ParsedEventInfo;
import com.keoz5.zombiezcompanion.util.TextUtils;
import net.minecraft.text.Text;

public final class SessionTrackerModule implements IModule {

    public static final String ID = "session_tracker";

    private SessionTrackerConfig config;
    private final SessionManager sessionManager;

    public SessionTrackerModule(SessionTrackerConfig config,
                                SessionManager sessionManager,
                                InternalEventBus eventBus) {
        this.config         = config;
        this.sessionManager = sessionManager;

        // React to zone changes reported via the event bus
        eventBus.subscribe(ServerEventType.ZONE_CHANGED, e ->
                sessionManager.onZoneChanged(e.getPayload(String.class)));
        eventBus.subscribe(ServerEventType.STREAK_UPDATED, e ->
                sessionManager.onStreakUpdate(e.getPayload(Integer.class)));
    }

    // ---- IModule --------------------------------------------------------

    @Override public String getId()          { return ID; }
    @Override public String getDisplayName() { return "Session Tracker"; }
    @Override public boolean isEnabled()     { return config.enabled; }
    @Override public void setEnabled(boolean v) { config.enabled = v; }

    @Override
    public void onInitialize(ModConfig modConfig) {
        this.config = modConfig.sessionTracker;
    }

    @Override
    public void onGameJoin() {
        if (config.autoStartOnJoin) {
            sessionManager.startSession();
        }
    }

    @Override
    public void onGameLeave() {
        sessionManager.endSession();
    }

    @Override
    public void onChatMessage(Text message, boolean isSystem) {
        String raw = TextUtils.plain(message);

        // Kill detection
        if (config.trackKills && ChatMessageParser.isKillMessage(raw)) {
            sessionManager.onKill();
        }

        // Event completion detection via shared parser
        if (config.trackEvents) {
            ParsedEventInfo info = ChatMessageParser.parseEvent(raw);
            if (info != null && info.type() == com.keoz5.zombiezcompanion.events.ServerEventType.SUCCESS) {
                sessionManager.onEventCompleted(info.displayName());
            }
        }

        // Basic loot detection – adapt regex to ZombieZ loot message format
        if (config.trackLoot && raw.matches("(?i).*vous avez reçu.*|.*\\+\\d+.*item.*")) {
            sessionManager.onLootDetected(raw);
        }
    }

    @Override
    public void onConfigChanged(ModConfig modConfig) {
        this.config = modConfig.sessionTracker;
    }

    public SessionManager getSessionManager() { return sessionManager; }
}
