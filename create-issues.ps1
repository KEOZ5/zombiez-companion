# create-issues.ps1
# Run this script after installing GitHub CLI (https://cli.github.com/) and running `gh auth login`
# Usage: .\create-issues.ps1

$repo = "KEOZ5/zombiez-companion"

function New-Issue($title, $body, $labels) {
    gh issue create --repo $repo --title $title --body $body --label $labels
}

Write-Host "Creating GitHub issues for ZombieZ Companion..."

New-Issue `
    "[Parser] Calibrate ScoreboardParser patterns for real ZombieZ data" `
    "## Context
The ScoreboardParser currently uses provisional regex patterns for zone, class, and streak detection.

## What to do
1. Enable \`debugMode\` in the in-game config screen (or set \`debugMode: true\` in config.json)
2. Join the ZombieZ server
3. Open \`.minecraft/logs/latest.log\` and search for \`[ScoreboardParser]\`
4. Note the exact \`clean=\` values printed for each sidebar line
5. Update the patterns in \`ScoreboardParser.java\` (ZONE_PATTERN, CLASS_PATTERN, STREAK_PATTERN)

## Files
\`src/main/java/com/keoz5/zombiezcompanion/parser/ScoreboardParser.java\`" `
    "parser,needs-real-data"

New-Issue `
    "[Parser] Calibrate loot/kill detection patterns for real ZombieZ messages" `
    "## Context
\`SessionTrackerModule\` and \`ChatMessageParser\` contain provisional regex patterns for detecting kills and loot.

## What to do
1. Enable \`debugMode\` — all chat messages are logged with \`[SessionTracker] Chat:\`
2. Play on ZombieZ and get a kill, pick up loot, complete an event
3. Copy the exact chat messages from the log
4. Update:
   - \`LOOT_PATTERNS\` list in \`SessionTrackerModule.java\`
   - \`KILL_DIRECT_PATTERN\` in \`SessionTrackerModule.java\`
   - \`KILL_PATTERN\` in \`ChatMessageParser.java\`
   - \`EVENT_PATTERNS\` in \`ChatMessageParser.java\`

## Files
- \`src/main/java/com/keoz5/zombiezcompanion/modules/tracker/SessionTrackerModule.java\`
- \`src/main/java/com/keoz5/zombiezcompanion/parser/ChatMessageParser.java\`" `
    "parser,needs-real-data"

New-Issue `
    "[HUD] Verify HudRenderCallback API with installed Fabric version" `
    "## Context
The mod uses \`HudRenderCallback.EVENT\` with signature \`(DrawContext, RenderTickCounter)\`.
This is correct for Fabric API >= 0.100 (our target: 0.114.0+1.21.4).

## What to verify at build time
- \`RenderTickCounter\` resolves at \`net.minecraft.client.render.RenderTickCounter\`
- \`tickCounter.getTickDelta(true)\` compiles without error
- If using a different fabric-api version, update the signature accordingly

## Fallback
If Fabric API < 0.100, replace:
\`\`\`java
HudRenderCallback.EVENT.register((drawContext, tickCounter) ->
    moduleRegistry.onHudRender(drawContext, tickCounter.getTickDelta(true)));
\`\`\`
with:
\`\`\`java
HudRenderCallback.EVENT.register((drawContext, delta) ->
    moduleRegistry.onHudRender(drawContext, delta));
\`\`\`

## Files
\`src/main/java/com/keoz5/zombiezcompanion/ZombieZCompanionClient.java\`" `
    "build,hud,compatibility"

New-Issue `
    "[Keybind] Default menu key set to Right Shift — may conflict with sprint" `
    "## Context
The default keybind to open the ZZC menu was changed from Y to Right Shift (\`GLFW_KEY_RIGHT_SHIFT\`).

## Potential conflict
Some Minecraft control setups use Right Shift for sprinting or other actions.
Users can rebind it in **Options → Controls → ZombieZ Companion**.

## To change the default
Edit \`ZombieZCompanionClient.registerKeybind()\`:
\`\`\`java
GLFW.GLFW_KEY_RIGHT_SHIFT   // current default
\`\`\`
Replace with any GLFW key constant, e.g. \`GLFW.GLFW_KEY_HOME\`.

## Files
\`src/main/java/com/keoz5/zombiezcompanion/ZombieZCompanionClient.java\`" `
    "keybind,ux"

New-Issue `
    "[Debug] Add in-game debug overlay showing raw scoreboard + HUD state" `
    "## Context
Currently debug info is printed to the log file only.

## Proposed improvement
When \`debugMode\` is enabled, optionally show a small in-game overlay in the top-left corner
displaying the raw parsed values (zone, class, streak, mutation state) so they can be verified
without opening the log file.

## Acceptance criteria
- Toggled by \`debugMode\` flag (already in \`ModConfig\`)
- Shows current \`HudState\` field values
- Does not appear in production (debugMode defaults to false)" `
    "enhancement,debug,hud"

Write-Host "Done. Check https://github.com/$repo/issues"
