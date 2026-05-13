package com.keoz5.zombiezcompanion.modules.tracker;

import com.keoz5.zombiezcompanion.config.ModConfig;
import com.keoz5.zombiezcompanion.config.SessionTrackerConfig;
import com.keoz5.zombiezcompanion.events.InternalEventBus;
import com.keoz5.zombiezcompanion.events.ServerEventType;
import com.keoz5.zombiezcompanion.modules.IModule;
import com.keoz5.zombiezcompanion.parser.ChatMessageParser;
import com.keoz5.zombiezcompanion.parser.ParsedEventInfo;
import com.keoz5.zombiezcompanion.util.ModLogger;
import com.keoz5.zombiezcompanion.util.TextUtils;
import net.minecraft.text.Text;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class SessionTrackerModule implements IModule {

    public static final String ID = "session_tracker";

    private SessionTrackerConfig config;
    private ModConfig            modConfig;
    private final SessionManager sessionManager;

    // ---- Loot / reward patterns -----------------------------------------
    // ┌──────────────────────────────────────────────────────────────────────┐
    // │  All patterns below are PROVISIONAL — based on generic French/       │
    // │  Minecraft server conventions.                                       │
    // │  Enable debugMode and join ZombieZ to observe real reward messages,  │
    // │  then update these patterns to match the actual chat format.         │
    // └──────────────────────────────────────────────────────────────────────┘
    private static final List<LootPattern> LOOT_PATTERNS = List.of(

        // ---- Currency / points gains ------------------------------------
        // TODO: replace with actual ZombieZ coin/point gain message format
        LootPattern.of("\\+\\s*\\d+\\s*(coins?|gold|€|pièces?|points?|crédits?)",
                "Gains monétaires", false),
        LootPattern.of("vous avez (reçu|obtenu|gagné).{0,40}",
                "Récompense", false),

        // ---- Items / crates ---------------------------------------------
        // TODO: replace with actual ZombieZ item reward message
        LootPattern.of("(caisse|crate|coffre|box).{0,20}(obtenu|reçu|ouvert|ouverte?)",
                "Caisse obtenue", false),
        LootPattern.of("(obtenu|reçu).{0,20}(caisse|crate|coffre)",
                "Caisse obtenue", false),

        // ---- Rare / notable items ---------------------------------------
        // TODO: replace with actual ZombieZ legendary/rare drop message
        LootPattern.of("(rare|légendaire|legendary|épique|epic|mythique)",
                "Loot rare", true),
        LootPattern.of("félicitations.{0,30}(gagné|obtenu|trouvé)",
                "Drop notable", true),

        // ---- XP / level -------------------------------------------------
        LootPattern.of("\\+\\s*\\d+\\s*(xp|exp|expérience)", "Gain XP", false),
        LootPattern.of("niveau\\s*(supérieur|atteint|up)|level\\s*up", "Level up", false)
    );

    // ---- Kill detection -------------------------------------------------
    // TODO: calibrate to real ZombieZ kill notification format
    private static final Pattern KILL_DIRECT_PATTERN = Pattern.compile(
            "tu as (tué|éliminé)|vous avez (tué|éliminé)|\\+\\s*1\\s*kill|kill\\s*\\+\\s*1",
            Pattern.CASE_INSENSITIVE
    );

    // ---- Constructor ----------------------------------------------------

    public SessionTrackerModule(SessionTrackerConfig config,
                                SessionManager sessionManager,
                                InternalEventBus eventBus) {
        this.config         = config;
        this.sessionManager = sessionManager;

        eventBus.subscribe(ServerEventType.ZONE_CHANGED,
                e -> sessionManager.onZoneChanged(e.getPayload(String.class)));
        eventBus.subscribe(ServerEventType.STREAK_UPDATED,
                e -> sessionManager.onStreakUpdate(e.getPayload(Integer.class)));
    }

    // ---- IModule --------------------------------------------------------

    @Override public String getId()             { return ID; }
    @Override public String getDisplayName()    { return "Session Tracker"; }
    @Override public boolean isEnabled()        { return config.enabled; }
    @Override public void setEnabled(boolean v) { config.enabled = v; }

    @Override
    public void onInitialize(ModConfig mc) {
        this.config    = mc.sessionTracker;
        this.modConfig = mc;
    }

    @Override
    public void onGameJoin() {
        if (config.autoStartOnJoin) sessionManager.startSession();
    }

    @Override
    public void onGameLeave() {
        sessionManager.endSession();
    }

    @Override
    public void onChatMessage(Text message, boolean isSystem) {
        String raw = TextUtils.plain(message);
        if (isDebug()) ModLogger.debug("[SessionTracker] Chat: \"" + raw + "\"");

        handleKillDetection(raw);
        handleEventDetection(raw);
        handleLootDetection(raw);
    }

    @Override
    public void onConfigChanged(ModConfig mc) {
        this.config    = mc.sessionTracker;
        this.modConfig = mc;
    }

    // ---- Private handlers -----------------------------------------------

    private void handleKillDetection(String raw) {
        if (!config.trackKills) return;
        boolean isKill = ChatMessageParser.isKillMessage(raw)
                || KILL_DIRECT_PATTERN.matcher(raw).find();
        if (isKill) {
            sessionManager.onKill();
            ModLogger.debug("[SessionTracker] Kill detected: \"" + raw + "\"");
        }
    }

    private void handleEventDetection(String raw) {
        if (!config.trackEvents) return;
        ParsedEventInfo info = ChatMessageParser.parseEvent(raw);
        if (info != null && info.type() == ServerEventType.SUCCESS) {
            sessionManager.onEventCompleted(info.displayName());
            ModLogger.debug("[SessionTracker] Event completed: " + info.displayName());
        }
    }

    private void handleLootDetection(String raw) {
        if (!config.trackLoot) return;
        for (LootPattern lp : LOOT_PATTERNS) {
            Matcher m = lp.pattern().matcher(raw);
            if (m.find()) {
                String label = lp.displayName() + (lp.rare() ? " ★" : "");
                sessionManager.onLootDetected(label + " – " + truncate(raw, 60));
                if (isDebug()) {
                    ModLogger.debug("[SessionTracker] Loot ["
                            + (lp.rare() ? "RARE" : "normal") + "] → \""
                            + lp.displayName() + "\" from: \"" + raw + "\"");
                }
                return; // first match wins
            }
        }
    }

    // ---- Util -----------------------------------------------------------

    private boolean isDebug() {
        return modConfig != null && modConfig.debugMode;
    }

    private static String truncate(String s, int max) {
        return s.length() <= max ? s : s.substring(0, max) + "…";
    }

    public SessionManager getSessionManager() { return sessionManager; }
}
