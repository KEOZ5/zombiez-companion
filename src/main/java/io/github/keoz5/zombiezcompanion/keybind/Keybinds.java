package io.github.keoz5.zombiezcompanion.keybind;

import io.github.keoz5.zombiezcompanion.ModInfo;
import io.github.keoz5.zombiezcompanion.ui.ConfigScreen;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

/**
 * Registers all client-side key bindings.
 *
 * <p>Default key is <b>Right Shift</b>. Rebindable in Options → Controls.
 * Conflicts: Right Shift is not bound by vanilla, but some mods use it —
 * users can move it freely.
 */
public final class Keybinds {

    private static final String CATEGORY = "key.categories." + ModInfo.MOD_ID;

    private static KeyBinding openMenu;

    private Keybinds() {}

    public static void register(Runnable onMenuPressed) {
        openMenu = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key." + ModInfo.MOD_ID + ".open_menu",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_RIGHT_SHIFT,
                CATEGORY
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (openMenu.wasPressed()) {
                if (client.currentScreen == null) onMenuPressed.run();
            }
        });
    }
}
