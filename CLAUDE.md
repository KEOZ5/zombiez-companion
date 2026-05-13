# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project

**ZombieZ Companion** – Fabric client-side QoL mod for Minecraft 1.21.4, targeting the ZombieZ public server.
The mod is **strictly read-only**: it reads visible client data (chat, scoreboard, bossbars) and displays overlays. It never automates gameplay, sends custom packets, or interacts on behalf of the player.

## Build & Run

```bash
# First time only: copy gradlew scripts from the Fabric example-mod template
# or run (requires Gradle installed locally):
gradle wrapper --gradle-version 8.10

# Compile
./gradlew build

# Run Minecraft client in dev environment
./gradlew runClient

# Release JAR → build/libs/zombiez-companion-<version>.jar
./gradlew build
```

Requires Java 21. IDE: IntelliJ IDEA with the Minecraft Development plugin.

## Dependency versions (`gradle.properties`)

| Property | Value |
|---|---|
| `minecraft_version` | `1.21.4` |
| `yarn_mappings` | `1.21.4+build.8` |
| `loader_version` | `0.16.9` |
| `fabric_version` | `0.114.0+1.21.4` |
| `modmenu_version` | `13.0.0` |

Check current Fabric API version at [fabricmc.net/develop](https://fabricmc.net/develop/).

## Architecture

### Package layout

```
com.keoz5.zombiezcompanion
├── ZombieZCompanionClient       ← entry-point; wires all Fabric events
├── config/
│   ├── ModConfig                ← root; contains debugMode + one sub-config per module
│   ├── EventAlertsConfig
│   ├── SmartHudConfig
│   ├── SessionTrackerConfig
│   └── ConfigManager            ← Gson JSON load/save with null-safe fallback
├── events/
│   ├── ServerEventType          ← enum of all publishable event types
│   ├── InternalEvent            ← typed wrapper (type + generic payload)
│   └── InternalEventBus         ← simple synchronous pub/sub
├── modules/
│   ├── IModule                  ← lifecycle interface (onTick, onHudRender, onChatMessage…)
│   ├── ModuleRegistry           ← holds all modules; broadcasts lifecycle calls
│   ├── alerts/EventAlertsModule
│   ├── hud/SmartHudModule
│   └── tracker/
│       ├── SessionTrackerModule
│       ├── SessionManager
│       ├── SessionStats
│       └── LootPattern          ← record(pattern, displayName, rare) for loot detection
├── parser/                      ← stateless utils; return typed records, never raw strings
│   ├── ChatMessageParser        ← regex EVENT_PATTERNS table → ParsedEventInfo
│   ├── ScoreboardParser         ← reads sidebar → ParsedZoneInfo / ParsedClassInfo
│   └── BossBarParser            ← reads bossbar map (via mixin) → ParsedMutationState
├── storage/
│   ├── SessionRecord            ← serializable snapshot of a finished session
│   └── SessionHistoryStorage    ← JSON array at config/zombiezcompanion/session_history.json
├── ui/
│   ├── MainConfigScreen         ← lists modules + ON/OFF toggle + opens detail screen
│   ├── EventAlertsConfigScreen
│   ├── SmartHudConfigScreen     ← X/Y text fields for HUD position
│   ├── SessionTrackerConfigScreen
│   ├── ModMenuIntegration       ← loaded only when Mod Menu is installed
│   └── widgets/ToggleButtonWidget
├── mixin/
│   └── BossBarHudAccessor       ← @Accessor exposing BossBarHud.bossBars (read-only)
└── util/
    ├── ModLogger                ← thin SLF4J wrapper
    ├── TextUtils                ← strip § codes, Text → plain String
    └── TimeUtils                ← format ms durations and countdowns
```

### Data flow

```
Fabric event (chat / tick / HUD)
        │
        ▼
ZombieZCompanionClient
  ├─ moduleRegistry.onChatMessage()   → each module parses what it needs
  ├─ dispatchParsedEvent()            → ChatMessageParser → InternalEventBus.publish()
  └─ moduleRegistry.onHudRender()     → modules draw their overlays

InternalEventBus (cross-module):
  SessionTrackerModule ──publishes──▶ SESSION_STARTED ──▶ SmartHudModule (sessionStartMs)
  EventAlertsModule    ──publishes──▶ GENERIC_SERVER_EVENT
```

### Config persistence

- Main config: `.minecraft/config/zombiezcompanion/config.json`
- Session history: `.minecraft/config/zombiezcompanion/session_history.json`
- Modules directly mutate sub-config fields; `configManager.save()` serializes the whole tree.

---

## Key API notes for Fabric 1.21.4

| Area | API | Note |
|---|---|---|
| HUD rendering | `HudRenderCallback.EVENT` | Signature: `(DrawContext, RenderTickCounter)` — call `tickCounter.getTickDelta(true)` for partial tick. Valid for Fabric API ≥ 0.100 (our target: 0.114.0+1.21.4). |
| Drawing | `DrawContext.drawTextWithShadow()` / `.fill()` | Colors are ARGB ints |
| Scoreboard | `Scoreboard.getScoreboardEntries(objective)` | Returns `Collection<ScoreboardEntry>`; each entry: `.owner()` (raw string) `.value()` (score int) |
| BossBar | `BossBarHudAccessor` mixin | Exposes private `bossBars` field; declared in `zombiezcompanion.mixins.json` |
| Client command | `ClientCommandRegistrationCallback` + `ClientCommandManager` | Package: `net.fabricmc.fabric.api.client.command.v2` |
| Keybind | `KeyBindingHelper.registerKeyBinding()` | Package: `net.fabricmc.fabric.api.client.keybinding.v1` |

---

## Debug mode

Set `debugMode: true` in `config/zombiezcompanion/config.json`, or toggle with `/zzc debug` in-game.

When enabled:
- All raw chat messages are logged: `[SessionTracker] Chat: "..."`
- Scoreboard sidebar is logged every ~5 s: `[ScoreboardParser] === Sidebar: "..." ===` with each line's `raw=` and `clean=` values
- Detected events are logged: `[EventBus] Dispatching TYPE – "label" from: "..."`
- Detected loot is logged: `[SessionTracker] Loot [RARE|normal] → "label" from: "..."`

**This is the primary tool to calibrate patterns against real ZombieZ data.**

---

## Provisional patterns — what to calibrate in-game

The following patterns are guesses and must be verified on the real server:

### ScoreboardParser (`ScoreboardParser.java`)

```java
ZONE_PATTERN   → "(?i)^(?:zone|secteur|area)\\s*[:\\-]?\\s*(.+)$"
CLASS_PATTERN  → "(?i)^(?:classe?|class)\\s*[:\\-]?\\s*(.+)$"
STREAK_PATTERN → "(?i)(?:streak|s[eé]rie|kill.?streak)\\s*[:\\-]?\\s*(\\d+)"
```

**How to calibrate:** enable debugMode → join ZombieZ → look for `[ScoreboardParser]` entries in the log → copy the `clean=` values → update the patterns.

### ChatMessageParser (`ChatMessageParser.java`)

```java
ZOMBIE_BOMB   → "(?i)zombie.?bomb|bombe.?zombie"
MUTATION_READY→ "(?i)mutation.{0,15}pr[eê]te?|ready.{0,10}mutation"
DEFUSED       → "(?i)d[eé]samor[cç][eé]e?"
SUCCESS       → "(?i)succ[eè]s|mission.{0,10}r[eé]ussie?"
BONUS_ACTIVE  → "(?i)bonus.{0,20}activ[eé]|[eé]v[eé]nement.{0,10}sp[eé]cial"
KILL          → see KILL_PATTERN in same file
```

### SessionTrackerModule (`SessionTrackerModule.java`)

```java
KILL_DIRECT_PATTERN → "tu as (tué|éliminé)|\\+\\s*1\\s*kill|..."
LOOT_PATTERNS       → 8 provisional entries (currency, crates, rare drops, XP)
```

---

## Commands

| Command | Effect |
|---|---|
| `/zzc menu` | Opens the config screen |
| `/zzc debug` | Toggles debugMode on/off (saved to config) |
| `/zzc status` | Prints all module states to chat + log |

## Keybind

Default: **Right Shift** (`GLFW_KEY_RIGHT_SHIFT`). Rebindable in Options → Controls → ZombieZ Companion.
To change the default, edit `ZombieZCompanionClient.registerKeybind()`.

---

## Adding a new module

1. `config/MyFeatureConfig.java` — public fields with defaults
2. Add `public MyFeatureConfig myFeature = new MyFeatureConfig();` to `ModConfig`
3. `modules/myfeature/MyFeatureModule.java` implementing `IModule`
4. Register in `ZombieZCompanionClient.onInitializeClient()`
5. Add a detail screen in `ui/` and wire it in `MainConfigScreen.buildDetailScreen()`

## Adding a new detectable event

Edit `EVENT_PATTERNS` in `ChatMessageParser.java` — each entry is a `EventPattern(regex, ServerEventType, displayName)` record. Add the new enum value to `ServerEventType` first.

---

## Open issues (create with `create-issues.ps1` after installing `gh` CLI)

| # | Title | Label |
|---|---|---|
| 1 | Calibrate ScoreboardParser patterns for real ZombieZ data | parser, needs-real-data |
| 2 | Calibrate loot/kill detection patterns for real ZombieZ messages | parser, needs-real-data |
| 3 | Verify HudRenderCallback API with installed Fabric version | build, hud, compatibility |
| 4 | Default menu key (Right Shift) may conflict with sprint | keybind, ux |
| 5 | Add in-game debug overlay showing raw scoreboard + HUD state | enhancement, debug, hud |

Run `.\create-issues.ps1` (requires `gh` CLI: https://cli.github.com/) to push all issues to GitHub.

---

## What NOT to do

- Never send packets or call server-side methods
- Never automate mouse clicks, movements, or attacks
- Never use mixins to inject into packet handlers or movement code
- Never modify `ClientPlayerEntity` motion or target fields
