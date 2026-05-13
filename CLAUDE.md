# CLAUDE.md

Guidance for Claude Code when working in this repo.

## Project

**ZombieZ Companion** — Fabric, client-side QoL mod for Minecraft **1.21.4**,
targeting the ZombieZ public server. Intended to be **published on CurseForge**
and used by external players.

**Strictly read-only.** The mod reads visible client data (chat, scoreboard,
bossbars) and renders overlays. It must never:

- send custom packets or call server-side methods
- automate input (mouse, attack, movement, inventory)
- inject mixins into packet handlers or movement code
- mutate `ClientPlayerEntity` motion or target fields

This rule survives every refactor. If a feature seems to require any of the
above, do not implement it.

---

## Build & run

```bash
./gradlew build            # compile + jar at build/libs/
./gradlew runClient        # launch dev client
```

- Java **21** required.
- Wrapper: `gradle wrapper --gradle-version 8.10` if `gradlew` is missing.
- Release JAR: `build/libs/zombiez-companion-<version>.jar`.

Versions live in `gradle.properties`:

| Property | Value |
|---|---|
| `minecraft_version` | `1.21.4` |
| `yarn_mappings`     | `1.21.4+build.8` |
| `loader_version`    | `0.16.9` |
| `fabric_version`    | `0.114.0+1.21.4` |
| `modmenu_version`   | `13.0.0` (optional) |
| `mod_version`       | `0.1.0-alpha.1` |
| `maven_group`       | `io.github.keoz5` |

---

## Architecture (base skeleton)

The project is currently a **clean base** — no business modules ship in the
JAR yet. Modules are added one at a time on top of this skeleton.

```
io.github.keoz5.zombiezcompanion
├── ZombieZCompanionClient        ← entry-point; one place wires everything
├── ModInfo                       ← MOD_ID, MOD_NAME constants
├── config/
│   ├── ModConfig                 ← root POJO (debug, moduleEnabled map, schemaVersion)
│   └── ConfigManager             ← Gson load/save, atomic write, corrupt-file backup
├── core/
│   ├── Module                    ← interface; all module hooks live here
│   ├── ModuleContext             ← config + eventBus handed to each module
│   └── ModuleManager             ← register, enable/disable, dispatch, error-trap
├── event/
│   ├── Event                     ← marker for typed events
│   └── EventBus                  ← class<E> → List<Consumer<E>>, sync, in-process
├── log/
│   ├── Log                       ← info/warn/error + debug(category, msg)
│   └── LogCategory               ← Core | Config | Module | Event | Chat | Hud
├── command/Commands              ← /zzc menu | debug | status | reload
├── keybind/Keybinds              ← Right Shift opens config screen
├── ui/
│   ├── ConfigScreen              ← lists modules with enable toggles
│   └── ModMenuIntegration        ← optional Mod Menu entry
└── mixin/                        ← empty; add only when truly required
```

### Lifecycle of a module

```
ModuleManager.register(m)
   ├─ m.onRegister(ctx)                        ← always
   └─ stores m, ensures moduleEnabled[id] in config

startEnabledModules()
   └─ for each enabled module → m.onEnable()

Fabric tick / chat / hud / join / leave
   └─ ModuleManager dispatches to enabled modules only (try/catch each)

setEnabled(id, false)
   └─ m.onDisable()
```

### Config persistence

- File: `.minecraft/config/zombiezcompanion/config.json`
- Atomic write (`config.json.tmp` → rename)
- Corrupt file is moved to `config.json.bak`, defaults are restored
- New fields must have a safe default initializer in `ModConfig`
- Bump `schemaVersion` on breaking changes; migration goes in `ConfigManager.load()`

### Debug logging

- Toggle: `/zzc debug` in-game, or set `debugMode: true` in `config.json`
- Format: `[ZombieZ][DEBUG][<Category>] <message>`
- Filter the log with `[ZombieZ][DEBUG]`; categories live in `LogCategory`
- Log file: `.minecraft/logs/latest.log`

---

## Commands

| Command | Effect |
|---|---|
| `/zzc menu`   | Open the config screen |
| `/zzc debug`  | Toggle global debug mode (persisted) |
| `/zzc status` | List modules + enable state |
| `/zzc reload` | Force-save the current config |

## Keybind

Default: **Right Shift** (`GLFW_KEY_RIGHT_SHIFT`). Rebindable in
Options → Controls → ZombieZ Companion. Change the default in
`Keybinds.register()`.

---

## Adding a new module

1. Create a class implementing `core.Module`. Give it a stable `id()` —
   lowercase, no spaces.
2. Override only the hooks the module needs. Hooks fire only while enabled.
3. Add per-module settings to `ModConfig` (a public field with a default
   initializer) and pass them in via `ModuleContext` if needed.
4. Register the module in `ZombieZCompanionClient.registerModules(...)`.
5. If the module emits cross-module signals, define a record implementing
   `Event` and publish on the bus.
6. If the module logs, reuse an existing `LogCategory` or add one.
7. Add user-facing strings to `lang/en_us.json`.

A module should be self-contained: deleting its file and the one
`mm.register(...)` line must not break the rest of the mod.

---

## Publishing notes (CurseForge)

Before any public release:

- `mod_version` follows semver; pre-1.0 stays on `0.x.y` or `0.x.y-alpha.N`
- `fabric.mod.json` has correct `homepage` / `sources` / `issues` / `icon`
- `LICENSE` file matches the declared license (`MIT`)
- No `TODO[DATA-NEEDED]` markers in shipped modules
- Test the JAR in a vanilla Fabric profile, not just `runClient`
- Tag the commit (`v0.1.0-alpha.1`) before uploading

---

## Coding conventions

- Package root: `io.github.keoz5.zombiezcompanion`
- Java 21, prefer `record` for data carriers
- No fields are `public` except trivial POJOs that Gson serializes
  (`ModConfig` and future per-module config classes)
- One class per file, file name matches the type
- No comments that just restate the code; comment the *why* only
- Hooks always run inside `try/catch` at the dispatch site
