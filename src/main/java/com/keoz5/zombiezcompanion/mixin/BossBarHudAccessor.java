package com.keoz5.zombiezcompanion.mixin;

import net.minecraft.client.gui.hud.BossBarHud;
import net.minecraft.client.network.ClientBossBar;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;
import java.util.UUID;

/**
 * Exposes the private bossBars field from BossBarHud so parsers can read active bars.
 * This is read-only: we never modify the map.
 */
@Mixin(BossBarHud.class)
public interface BossBarHudAccessor {

    @Accessor("bossBars")
    Map<UUID, ClientBossBar> getBossBars();
}
