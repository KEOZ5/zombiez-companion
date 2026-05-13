# create-issues.ps1
# Creates all tracked GitHub issues for ZombieZ Companion.
# Requires GitHub CLI: https://cli.github.com/  →  gh auth login
# Usage: .\create-issues.ps1

$repo = "KEOZ5/zombiez-companion"

function New-Issue($title, $body, $labels) {
    Write-Host "Creating: $title"
    gh issue create --repo $repo --title $title --body $body --label $labels
}

# ── Phase 1: Architecture & setup ─────────────────────────────────────────

New-Issue `
    "[Parser] Calibrate ScoreboardParser patterns for real ZombieZ data" `
    "## Context
ScoreboardParser uses provisional regex for zone/class/streak detection.

## How to calibrate
1. Enable debugMode (\`/zzc debug\`)
2. Join ZombieZ server
3. Filter log on \`[ZombieZ][DEBUG][Scoreboard]\`
4. Copy all \`clean=\` lines
5. Update \`ZONE_PATTERN\`, \`CLASS_PATTERN\`, \`STREAK_PATTERN\` in ScoreboardParser.java

## Expected log format
\`\`\`
[ZombieZ][DEBUG][Scoreboard] title=""
[ZombieZ][DEBUG][Scoreboard] clean="" score=
\`\`\`

## File
\`src/main/java/com/keoz5/zombiezcompanion/parser/ScoreboardParser.java\`" `
    "parser,needs-real-data,calibration"

New-Issue `
    "[Parser] Calibrate kill/loot/event detection patterns for real ZombieZ messages" `
    "## Context
ChatMessageParser and SessionTrackerModule use provisional patterns.

## How to calibrate
1. Enable debugMode (\`/zzc debug\`)
2. Join ZombieZ server
3. Filter log on \`[ZombieZ][DEBUG][Chat:Raw]\` — ALL messages are logged
4. Get kills, rewards, events in-game
5. Update patterns in ChatMessageParser.java and SessionTrackerModule.java

## Files
- \`ChatMessageParser.java\` → EVENT_PATTERNS, KILL_PATTERN
- \`SessionTrackerModule.java\` → LOOT_PATTERNS, KILL_DIRECT_PATTERN" `
    "parser,needs-real-data,calibration"

New-Issue `
    "[Parser] Calibrate BossBarParser mutation/event detection" `
    "## Context
BossBarParser uses keyword-based detection for mutation bossbars.

## How to calibrate
1. Enable debugMode (\`/zzc debug\`)
2. Join ZombieZ server
3. Filter log on \`[ZombieZ][DEBUG][Bossbar]\`
4. Let a mutation charge / activate an event with a bossbar
5. Copy the \`name=\` lines
6. Update parseMutation() and parseActiveEvent() in BossBarParser.java

## File
\`src/main/java/com/keoz5/zombiezcompanion/parser/BossBarParser.java\`" `
    "parser,needs-real-data,calibration"

New-Issue `
    "[Build] Verify HudRenderCallback API compiles with fabric-api 0.114.0+1.21.4" `
    "## Context
ZombieZCompanionClient uses:
\`\`\`java
HudRenderCallback.EVENT.register((drawContext, tickCounter) ->
    moduleRegistry.onHudRender(drawContext, tickCounter.getTickDelta(true)));
\`\`\`
This requires RenderTickCounter from net.minecraft.client.render (Yarn mapping).

## Verify
- [ ] \`./gradlew build\` completes without errors
- [ ] RenderTickCounter resolves correctly
- [ ] HUD overlay renders in game

## Fallback if build fails
Replace the lambda signature with \`(ctx, delta)\` and remove \`.getTickDelta(true)\`.

## File
\`ZombieZCompanionClient.java\`" `
    "build,hud,compatibility"

New-Issue `
    "[Keybind] Right Shift may conflict with sprint — document and consider alternatives" `
    "## Context
Default open-menu key is Right Shift (\`GLFW_KEY_RIGHT_SHIFT\`). This may conflict with sprint on some setups.

## Resolution
Rebind in Options → Controls → ZombieZ Companion.
If a better default is found after testing, update \`registerKeybind()\` in ZombieZCompanionClient.java." `
    "keybind,ux"

New-Issue `
    "[Data-collection] Collect real ZombieZ in-game data to finalize all patterns" `
    "## This is the primary field-testing issue

Follow the procedure in CLAUDE.md §'Procédure de collecte des données in-game'.

## Checklist
- [ ] Enable debugMode (\`/zzc debug\`)
- [ ] Collect Scoreboard lines (several contexts: spawn, combat, event)
- [ ] Collect Kill messages
- [ ] Collect Loot/reward messages
- [ ] Collect Bossbar names (mutation, events)
- [ ] Collect Event announcement messages (Zombie Bombe, etc.)
- [ ] Fill out the return template from CLAUDE.md and send for pattern update

## After data collection
Open issues #1, #2, #3 and update the patterns." `
    "needs-real-data,field-test,priority"

New-Issue `
    "[Enhancement] Add in-game debug overlay for real-time HUD state inspection" `
    "## Context
Currently debug info only goes to the log file.

## Proposed
When debugMode is on, show a small corner overlay displaying:
- raw parsed zone/class/streak
- mutationReady flag
- activeEvent string
- session duration

This avoids having to read the log file to verify parsing.

## Acceptance
- Only shown when debugMode = true
- Toggle with /zzc debug
- Does not affect production behavior" `
    "enhancement,debug,hud"

Write-Host ""
Write-Host "All issues created. See: https://github.com/$repo/issues"
