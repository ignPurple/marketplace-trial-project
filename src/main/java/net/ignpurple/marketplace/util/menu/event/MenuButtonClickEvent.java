package net.ignpurple.marketplace.util.menu.event;

import net.ignpurple.marketplace.util.menu.built.BuiltButton;

public class MenuButtonClickEvent {
    private final BuiltButton button;

    public MenuButtonClickEvent(BuiltButton button) {
        this.button = button;
    }

    public BuiltButton getButton() {
        return this.button;
    }
}
