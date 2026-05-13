package com.keoz5.zombiezcompanion.ui.widgets;

import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.function.Consumer;

/**
 * A button that toggles ON/OFF and fires a callback with the new state.
 */
public final class ToggleButtonWidget extends ButtonWidget {

    private boolean toggled;
    private final Consumer<Boolean> onToggle;

    public ToggleButtonWidget(int x, int y, int width, int height,
                              boolean initialState, Consumer<Boolean> onToggle) {
        super(x, y, width, height,
                buildLabel(initialState),
                btn -> {},                     // handled in onPress()
                DEFAULT_NARRATION_SUPPLIER);
        this.toggled  = initialState;
        this.onToggle = onToggle;
    }

    @Override
    public void onPress() {
        toggled = !toggled;
        setMessage(buildLabel(toggled));
        onToggle.accept(toggled);
    }

    public boolean isToggled() { return toggled; }

    public void setToggled(boolean state) {
        toggled = state;
        setMessage(buildLabel(state));
    }

    private static Text buildLabel(boolean on) {
        return on
                ? Text.literal("ON").formatted(Formatting.GREEN)
                : Text.literal("OFF").formatted(Formatting.RED);
    }
}
