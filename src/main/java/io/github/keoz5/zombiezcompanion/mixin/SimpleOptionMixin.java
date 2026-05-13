package io.github.keoz5.zombiezcompanion.mixin;

import io.github.keoz5.zombiezcompanion.modules.brightness.BrightnessOverride;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.SimpleOption;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Read-only override of the gamma option's effective value.
 *
 * <p>This mixin fires for every {@link SimpleOption#getValue()} call — there
 * is no narrower hook in vanilla. The body short-circuits as soon as
 * Brightness is inactive (a single volatile read) so the per-frame overhead
 * is negligible. The identity check {@code this == options.getGamma()} makes
 * sure we never touch any other option (FOV, render distance, etc.).
 *
 * <p>The vanilla stored gamma value is never modified — the mixin only
 * substitutes the value returned to callers while the module is on.
 */
@Mixin(SimpleOption.class)
public abstract class SimpleOptionMixin {

    @Inject(method = "getValue", at = @At("HEAD"), cancellable = true)
    private void zombiezcompanion$applyBrightnessOverride(CallbackInfoReturnable<Object> cir) {
        if (!BrightnessOverride.isActive()) return;
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc == null || mc.options == null) return;
        // Identity-check against the live gamma option instance.
        if ((Object) this == mc.options.getGamma()) {
            cir.setReturnValue(BrightnessOverride.target());
        }
    }
}
