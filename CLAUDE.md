# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project

**ZombieZ Companion** – Fabric client-side QoL mod for Minecraft 1.21.4, targeting the ZombieZ public server.
The mod is **strictly read-only**: it reads visible client data (chat, scoreboard, bossbars) and displays overlays. It never automates gameplay, sends custom packets, or interacts on behalf of the player.

---

## Build & Run

```bash
# First time: get gradlew scripts from the Fabric example-mod template, or:
gradle wrapper --gradle-version 8.10

./gradlew build          # compile
./gradlew runClient      # launch Minecraft in dev environment
```

Release JAR → `build/libs/zombiez-companion-<version>.jar`
Requires Java 21. IDE: IntelliJ IDEA with Minecraft Development plugin.

## Dependency versions (`gradle.properties`)

| Property | Value |
|---|---|
| `minecraft_version` | `1.21.4` |
| `yarn_mappings` | `1.21.4+build.8` |
| `loader_version` | `0.16.9` |
| `fabric_version` | `0.114.0+1.21.4` |
| `modmenu_version` | `13.0.0` |

---

## Architecture

### Package layout

```
com.keoz5.zombiezcompanion
├── ZombieZCompanionClient       ← entry-point; wires all Fabric events
├── config/
│   ├── ModConfig                ← root: debugMode + one sub-config per module
│   ├── EventAlertsConfig / SmartHudConfig / SessionTrackerConfig
│   └── ConfigManager            ← Gson JSON load/save, null-safe defaults
├── events/
│   ├── ServerEventType          ← enum of all publishable event types
│   ├── InternalEvent            ← typed wrapper (type + payload)
│   └── InternalEventBus         ← synchronous pub/sub
├── modules/
│   ├── IModule                  ← lifecycle interface
│   ├── ModuleRegistry           ← holds all modules, broadcasts events
│   ├── alerts/EventAlertsModule
│   ├── hud/SmartHudModule, HudState, HudStateService, HudRenderer
│   └── tracker/
│       ├── SessionTrackerModule
│       ├── SessionManager / SessionStats
│       └── LootPattern          ← record(pattern, displayName, rare)
├── parser/                      ← stateless; return typed records
│   ├── ChatMessageParser        ← EVENT_PATTERNS + KILL_PATTERN → ParsedEventInfo
│   ├── ScoreboardParser         ← sidebar → ParsedZoneInfo / ParsedClassInfo
│   └── BossBarParser            ← bossbar map (mixin) → ParsedMutationState
├── storage/
│   ├── SessionRecord / SessionHistoryStorage
├── ui/
│   ├── MainConfigScreen, EventAlertsConfigScreen
│   ├── SmartHudConfigScreen, SessionTrackerConfigScreen
│   ├── ModMenuIntegration, widgets/ToggleButtonWidget
├── mixin/
│   └── BossBarHudAccessor       ← @Accessor for BossBarHud.bossBars (read-only)
└── util/
    ├── DebugLogger              ← categorized debug output [ZombieZ][DEBUG][Category]
    ├── ModLogger                ← SLF4J wrapper
    ├── TextUtils / TimeUtils
```

### Data flow

```
Fabric chat event
   │
   ├─ moduleRegistry.onChatMessage()   → each module handles its own parsing
   ├─ dispatchParsedEvent()            → ChatMessageParser → InternalEventBus.publish()
   └─ DebugLogger.chatRaw(raw)         → [ZombieZ][DEBUG][Chat:Raw]

Fabric tick event
   ├─ moduleRegistry.onTick()
   └─ runPeriodicDebug() every 100 ticks
         ├─ ScoreboardParser.debugPrintSidebar() → [ZombieZ][DEBUG][Scoreboard]
         ├─ BossBarParser.debugPrintBossBars()   → [ZombieZ][DEBUG][Bossbar]
         └─ HudState snapshot                    → [ZombieZ][DEBUG][HudState]

InternalEventBus cross-module:
   SessionTrackerModule ──▶ SESSION_STARTED ──▶ SmartHudModule (sessionStartMs)
```

### Config persistence

- Main config: `.minecraft/config/zombiezcompanion/config.json`
- Session history: `.minecraft/config/zombiezcompanion/session_history.json`

---

## Debug mode

Toggle with `/zzc debug` in-game or set `"debugMode": true` in `config.json`.

### Log categories

All lines share the prefix `[ZombieZ][DEBUG]` — filter in the log file with:

| Filter string | What it shows |
|---|---|
| `[ZombieZ][DEBUG][Scoreboard]` | All sidebar lines, cleaned, every ~5 s |
| `[ZombieZ][DEBUG][Bossbar]` | Active bossbars (name + %) every ~5 s |
| `[ZombieZ][DEBUG][HudState]` | Parsed HUD values every ~5 s |
| `[ZombieZ][DEBUG][Chat:Raw]` | Every chat message before parsing |
| `[ZombieZ][DEBUG][Chat:Parsed]` | Messages that matched an event pattern |
| `[ZombieZ][DEBUG][Kill]` | Messages that triggered kill detection |
| `[ZombieZ][DEBUG][Loot]` | Messages that matched a loot pattern |
| `[ZombieZ][DEBUG][Event]` | Events dispatched to the bus |

Log file location: `.minecraft/logs/latest.log`

---

## Provisional patterns — status & calibration guide

The sections below track every pattern that has not yet been verified against real ZombieZ data.

### ✅ Finalized (no server data required)

- Config system (load/save/defaults)
- Module lifecycle (IModule, ModuleRegistry, InternalEventBus)
- HUD rendering pipeline (HudState, HudRenderer, SmartHudModule)
- Session lifecycle (SessionManager start/end/persist)
- UI screens (MainConfig, detail screens, ToggleButtonWidget)
- Keybind (Right Shift, rebindable)
- Mod Menu integration
- Debug logging infrastructure (DebugLogger, all categories)

### ⚠️ Provisional — requires in-game calibration

| Location | What | TODO tag |
|---|---|---|
| `ScoreboardParser.java` | `ZONE_PATTERN` regex | `TODO[DATA-NEEDED]` |
| `ScoreboardParser.java` | `CLASS_PATTERN` regex | `TODO[DATA-NEEDED]` |
| `ScoreboardParser.java` | `STREAK_PATTERN` regex | `TODO[DATA-NEEDED]` |
| `ChatMessageParser.java` | `ZOMBIE_BOMB` pattern | `TODO[DATA-NEEDED]` |
| `ChatMessageParser.java` | `MUTATION_READY` pattern | `TODO[DATA-NEEDED]` |
| `ChatMessageParser.java` | `DEFUSED` pattern | `TODO[DATA-NEEDED]` |
| `ChatMessageParser.java` | `SUCCESS` pattern | `TODO[DATA-NEEDED]` |
| `ChatMessageParser.java` | `BONUS_ACTIVE` pattern | `TODO[DATA-NEEDED]` |
| `ChatMessageParser.java` | `KILL_PATTERN` | `TODO[DATA-NEEDED]` |
| `SessionTrackerModule.java` | `KILL_DIRECT_PATTERN` | `TODO[DATA-NEEDED]` |
| `SessionTrackerModule.java` | `LOOT_PATTERNS` (all 8) | `TODO[DATA-NEEDED]` |
| `BossBarParser.java` | mutation bossbar keyword | `TODO[DATA-NEEDED]` |
| `BossBarParser.java` | "mutation ready" keyword | `TODO[DATA-NEEDED]` |

Search for `TODO[DATA-NEEDED]` in the project to find all provisional locations.

---

## Procédure de collecte des données in-game

### Prérequis

1. Compiler le mod : `./gradlew build`
2. Copier le JAR dans `.minecraft/mods/`
3. Lancer Minecraft avec Fabric 0.16.9+ sur 1.21.4
4. Rejoindre le serveur ZombieZ

### Activation du mode debug

En jeu : `/zzc debug` → message de confirmation en vert

Ou manuellement dans `.minecraft/config/zombiezcompanion/config.json` :
```json
{
  "debugMode": true,
  ...
}
```

### Collecte — Scoreboard

**Objectif :** identifier le format exact des lignes de zone, classe, streak.

1. Rejoindre le serveur
2. Rester immobile ~10 secondes (le debug log toutes les 5 s)
3. Ouvrir `.minecraft/logs/latest.log`
4. Filtrer sur `[ZombieZ][DEBUG][Scoreboard]`
5. Collecter les lignes dans **plusieurs contextes** :
   - Zone spawn / hub
   - Zone combat
   - Pendant un event actif
   - Avec un streak en cours

**Format attendu dans le log :**
```
[ZombieZ][DEBUG][Scoreboard] title="<nom de l'objectif>"
[ZombieZ][DEBUG][Scoreboard] clean="<texte de la ligne>" score=<valeur>
```

### Collecte — Kill

**Objectif :** identifier le message exact affiché quand on tue.

1. Tuer quelques zombies
2. Filtrer sur `[ZombieZ][DEBUG][Chat:Raw]` dans le log
3. Repérer les lignes qui apparaissent immédiatement après chaque kill

**Format :**
```
[ZombieZ][DEBUG][Chat:Raw] message="<texte exact>"
```

### Collecte — Loot / Récompenses

**Objectif :** identifier les messages de gains (monnaie, caisses, items).

1. Obtenir une récompense (fin de round, caisse, drop)
2. Filtrer sur `[ZombieZ][DEBUG][Chat:Raw]`
3. Collecter toutes les lignes apparues lors du gain

### Collecte — Bossbars

**Objectif :** identifier le texte exact des bossbars de mutation et d'events.

1. Laisser une mutation se charger ou provoquer un event avec bossbar
2. Filtrer sur `[ZombieZ][DEBUG][Bossbar]`

**Format :**
```
[ZombieZ][DEBUG][Bossbar] name="<texte exact>" percent=<xx%>
```

### Collecte — Events (Zombie Bombe, Mutation, etc.)

**Objectif :** identifier les textes d'annonce d'événements (chat, title, system).

1. Attendre ou déclencher un événement du serveur
2. Filtrer sur `[ZombieZ][DEBUG][Chat:Raw]`
3. Aussi noter les textes visibles dans le title/subtitle si applicable

---

## Template de retour — à remplir et m'envoyer

Copier ce bloc, remplir les sections, et m'envoyer le tout pour que je mette à jour les patterns.

```
## Rapport de collecte ZombieZ

### Scoreboard — lignes clean= (plusieurs contextes si possible)
- clean="" score=
- clean="" score=
- ...

### Kill — messages bruts
- message=""
- ...

### Loot / Récompenses — messages bruts
- message=""
- ...

### Bossbars
- name="" percent=
- ...

### Events — messages bruts (chat ou title)
- message=""
- ...

### Notes complémentaires
(tout ce qui semble utile : format inhabituel, couleurs spéciales, etc.)
```

---

## Commandes

| Commande | Effet |
|---|---|
| `/zzc menu` | Ouvre l'écran de configuration |
| `/zzc debug` | Active/désactive le debug mode (sauvegardé) |
| `/zzc status` | Affiche l'état de tous les modules |

## Keybind

Défaut : **Right Shift** (`GLFW_KEY_RIGHT_SHIFT`). Rebindable dans Options → Controls → ZombieZ Companion.
Pour changer le défaut : modifier `GLFW.GLFW_KEY_RIGHT_SHIFT` dans `ZombieZCompanionClient.registerKeybind()`.

---

## Key API notes for Fabric 1.21.4

| Area | API | Note |
|---|---|---|
| HUD rendering | `HudRenderCallback.EVENT` | Signature: `(DrawContext, RenderTickCounter)` — `tickCounter.getTickDelta(true)`. Valid for Fabric API ≥ 0.100 |
| Drawing | `DrawContext.drawTextWithShadow()` / `.fill()` | Colors are ARGB ints |
| Scoreboard | `Scoreboard.getScoreboardEntries(objective)` | Returns `Collection<ScoreboardEntry>`; `.owner()` = raw string, `.value()` = score |
| BossBar | `BossBarHudAccessor` mixin | Exposes private `bossBars`; declared in `zombiezcompanion.mixins.json` |
| Client command | `ClientCommandRegistrationCallback` | Package: `net.fabricmc.fabric.api.client.command.v2` |
| Keybind | `KeyBindingHelper` | Package: `net.fabricmc.fabric.api.client.keybinding.v1` |

---

## Open issues (create with `create-issues.ps1`)

Install `gh` CLI → `gh auth login` → `.\create-issues.ps1`

| # | Title | Priority |
|---|---|---|
| 1 | Calibrate ScoreboardParser patterns | needs-real-data |
| 2 | Calibrate kill/loot/event patterns | needs-real-data |
| 3 | Calibrate BossBarParser | needs-real-data |
| 4 | Verify HudRenderCallback builds | build |
| 5 | Right Shift keybind conflict | ux |
| **6** | **Collect real ZombieZ in-game data** | **priority** |
| 7 | Add in-game debug overlay | enhancement |

---

## Adding a new module

1. `config/MyFeatureConfig.java` with public fields + defaults
2. Add `public MyFeatureConfig myFeature = new MyFeatureConfig();` to `ModConfig`
3. `modules/myfeature/MyFeatureModule.java` implementing `IModule`
4. Register in `ZombieZCompanionClient.onInitializeClient()`
5. Add detail screen in `ui/` and wire in `MainConfigScreen.buildDetailScreen()`

## Adding a new detectable event

1. Add value to `ServerEventType`
2. Add `EventPattern(regex, type, displayName)` to `EVENT_PATTERNS` in `ChatMessageParser`

---

## What NOT to do

- Never send packets or call server-side methods
- Never automate mouse clicks, movements, or attacks
- Never use mixins to inject into packet handlers or movement code
- Never modify `ClientPlayerEntity` motion or target fields
