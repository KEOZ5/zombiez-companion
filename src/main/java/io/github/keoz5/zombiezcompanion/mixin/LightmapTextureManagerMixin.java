package io.github.keoz5.zombiezcompanion.mixin;

import io.github.keoz5.zombiezcompanion.modules.brightness.BrightnessOverride;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.SimpleOption;
import net.minecraft.client.render.LightmapTextureManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * Redirects the gamma read inside {@link LightmapTextureManager#update(float)}
 * to return Brightness's boosted value when the module is active.
 *
 * <p>Targeting the lightmap update specifically (rather than every
 * {@code SimpleOption.getValue()} call in the game) gives us two guarantees:
 * <ul>
 *     <li>The vanilla {@code SimpleOption} value is never read incorrectly by
 *         any other code path — the Video Settings slider still shows the
 *         user's real gamma, options.txt is never written with our value.</li>
 *     <li>The boost can exceed the vanilla {@code [0.0, 1.0]} clamp because
 *         we substitute the value after it leaves the option's validator,
 *         right before it feeds into the lerp formula that produces the
 *         lightmap texture. Values around 15 saturate every pixel to full
 *         white = true full bright (caves lit like daylight).</li>
 * </ul>
 *
 * <p>Identity check inside the redirect ensures that if Mojang ever adds a
 * second {@code SimpleOption.getValue()} call to {@code update} (for, say,
 * a future graphics option), we still only override gamma.
 */
@Mixin(LightmapTextureManager.class)
public abstract class LightmapTextureManagerMixin {

    @Redirect(
            method = "update(F)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/option/SimpleOption;getValue()Ljava/lang/Object;"
            )
    )
    private Object zombiezcompanion$boostGamma(SimpleOption<?> instance) {
        Object original = instance.getValue();
        if (!BrightnessOverride.isActive()) return original;
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc == null || mc.options == null) return original;
        if (instance == mc.options.getGamma()) {
            return BrightnessOverride.target();
        }
        return original;
    }
}
