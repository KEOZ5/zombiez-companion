# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project

**ZombieZ Companion** – Fabric client-side QoL mod for Minecraft 1.21.4, targeting the ZombieZ public server.
The mod is **strictly read-only**: it reads visible client data (chat, scoreboard, bossbars) and displays overlays. It never automates gameplay, sends custom packets, or interacts on behalf of the player.

## Build & Run

```bash
# Generate Gradle wrapper (first time only – copy gradlew from Fabric template or run):
gradle wrapper --gradle-version 8.10

# Compile
./gradlew build

# Run in dev environment (Minecraft client with hot-reload)
./gradlew runClient

# Build release JAR → build/libs/zombiez-companion-<version>.jar
./gradlew build
```

Requires Java 21. IDE: IntelliJ IDEA with the Minecraft Development plugin (installs Fabric Loom automatically).

## Dependency versions

Defined in `gradle.properties`:

| Property | Value |
|---|---|
| `minecraft_version` | `1.21.4` |
| `yarn_mappings` | `1.21.4+build.8` |
| `loader_version` | `0.16.9` |
| `fabric_version` | `0.114.0+1.21.4` |
| `modmenu_version` | `13.0.0` |

Check current Fabric API version at [fabricmc.net/develop](https://fabricmc.net/develop/) if build fails.

## Architecture

### Package layout

```
com.keoz5.zombiezcompanion
├── ZombieZCompanionClient      ← entry-point; wires all Fabric events
├── config/                     ← POJOs loaded/saved as JSON via Gson
│   ├── ModConfig               ← root; contains one sub-config per module
│   ├── EventAlertsConfig
│   ├── SmartHudConfig
│   ├── SessionTrackerConfig
│   └── ConfigManager           ← load/save with null-safe fallback to defaults
├── events/
│   ├── ServerEventType         ← enum of all publishable event types
│   ├── InternalEvent           ← typed wrapper (type + generic payload)
│   └── InternalEventBus        ← simple synchronous pub/sub
├── modules/
│   ├── IModule                 ← lifecycle interface (onTick, onHudRender, onChatMessage…)
│   ├── ModuleRegistry          ← holds all modules; broadcasts lifecycle calls
│   ├── alerts/EventAlertsModule
│   ├── hud/SmartHudModule
│   └── tracker/SessionTrackerModule
├── parser/                     ← stateless utils; return typed records, never raw strings
│   ├── ChatMessageParser       ← regex pattern table → ParsedEventInfo
│   ├── ScoreboardParser        ← reads sidebar → ParsedZoneInfo / ParsedClassInfo
│   └── BossBarParser           ← reads bossbar map (via mixin) → ParsedMutationState
├── storage/
│   ├── SessionRecord           ← serializable snapshot of a finished session
│   └── SessionHistoryStorage   ← JSON array in config/zombiezcompanion/session_history.json
├── ui/
│   ├── MainConfigScreen        ← lists modules + ON/OFF + opens detail screen
│   ├── EventAlertsConfigScreen
│   ├── SmartHudConfigScreen    ← includes X/Y text fields for HUD position
│   ├── SessionTrackerConfigScreen
│   ├── ModMenuIntegration      ← loaded only when Mod Menu is installed
│   └── widgets/ToggleButtonWidget
├── mixin/
│   └── BossBarHudAccessor      ← @Accessor exposing BossBarHud.bossBars (read-only)
└── util/
    ├── ModLogger               ← thin wrapper around SLF4J
    ├── TextUtils               ← strip § codes, Text → plain String
    └── TimeUtils               ← format ms durations and countdowns
```

### Data flow

```
Fabric event (chat/tick/HUD)
       │
       ▼
ZombieZCompanionClient
  ├─ moduleRegistry.onChatMessage()  →  each module handles its own parsing
  ├─ dispatchParsedEvent()           →  ChatMessageParser → InternalEventBus.publish()
  └─ moduleRegistry.onHudRender()    →  modules render their own overlays

InternalEventBus (cross-module)
  SessionTrackerModule  ──publishes──▶  SESSION_STARTED  ──▶  SmartHudModule (sets sessionStartMs)
  EventAlertsModule     ──publishes──▶  GENERIC_SERVER_EVENT
```

### Module lifecycle

1. `ZombieZCompanionClient` instantiates each module with its sub-config object and the shared `InternalEventBus`.
2. Each module subscribes to relevant bus events in its constructor.
3. `onInitialize(ModConfig)` is called once after all modules are registered.
4. Fabric event callbacks broadcast to `ModuleRegistry`, which fans out to enabled modules only.
5. On disconnect: `onGameLeave()` → `configManager.save()`.

### Config persistence

- File: `.minecraft/config/zombiezcompanion/config.json`
- Session history: `.minecraft/config/zombiezcompanion/session_history.json`
- Modules directly mutate their sub-config fields (e.g. `config.smartHud.showZone = false`); `configManager.save()` serializes the whole `ModConfig` tree.

## Key API notes for Fabric 1.21.4

| Area | API | Note |
|---|---|---|
| HUD rendering | `HudRenderCallback.EVENT` | Callback signature is `(DrawContext, RenderTickCounter)` – call `tickCounter.getTickDelta(true)` |
| Drawing | `DrawContext.drawTextWithShadow()` / `.fill()` | Use ARGB int for colors |
| Scoreboard | `Scoreboard.getScoreboardEntries(objective)` | Returns `Collection<ScoreboardEntry>`; entries have `.owner()` and `.value()` |
| BossBar | `BossBarHudAccessor` mixin | Exposes private `bossBars` map; defined in `zombiezcompanion.mixins.json` |
| Client command | `ClientCommandRegistrationCallback` + `ClientCommandManager` | package `net.fabricmc.fabric.api.client.command.v2` |
| Keybind | `KeyBindingHelper.registerKeyBinding()` | package `net.fabricmc.fabric.api.client.keybinding.v1` |

## Adding a new module

1. Create `config/MyFeatureConfig.java` with public fields and defaults.
2. Add a `public MyFeatureConfig myFeature = new MyFeatureConfig();` field to `ModConfig`.
3. Create `modules/myfeature/MyFeatureModule.java` implementing `IModule`.
4. Register it in `ZombieZCompanionClient.onInitializeClient()`.
5. Add a detail screen in `ui/` and wire it in `MainConfigScreen.buildDetailScreen()`.

## Adding a new detectable event

Edit the `EVENT_PATTERNS` list in `ChatMessageParser`. Each entry is a `EventPattern(regex, ServerEventType, displayName)` record. Add the new `ServerEventType` value to the enum first.

## What NOT to do

- Never send packets or call server-side methods.
- Never automate mouse clicks, movements, or attacks.
- Never use mixins to inject into packet handlers or movement code.
- Never modify `ClientPlayerEntity` motion or target fields.
