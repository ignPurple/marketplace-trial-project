package net.ignpurple.marketplace.util.menu.built;

import org.bukkit.inventory.ItemStack;

public class BuiltButton {
    private final int slot;
    private final String name;
    private final ItemStack item;

    public BuiltButton(int slot, String name, ItemStack item) {
        this.slot = slot;
        this.name = name;
        this.item = item;
    }

    public int getSlot() {
        return this.slot;
    }

    public String getName() {
        return this.name;
    }

    public ItemStack getItem() {
        return this.item;
    }
}
