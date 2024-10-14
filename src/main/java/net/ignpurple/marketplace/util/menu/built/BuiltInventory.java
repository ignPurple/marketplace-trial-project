package net.ignpurple.marketplace.util.menu.built;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class BuiltInventory {
    private final Inventory inventory;
    private final Map<Integer, BuiltButton> buttons;

    public BuiltInventory(Inventory inventory) {
        this.inventory = inventory;
        this.buttons = new ConcurrentHashMap<>();
    }

    public Inventory getInventory() {
        return this.inventory;
    }
    public ItemStack getItem(int index) {
        final BuiltButton button = this.buttons.get(index);
        return button == null ? null : button.getItem();
    }

    public BuiltButton getButton(int slot) {
        return this.buttons.get(slot);
    }

    public List<BuiltButton> getButtonsByName(String name) {
        final List<BuiltButton> buttons = new ArrayList<>();
        for (final BuiltButton button : this.buttons.values()) {
            if (!button.getName().equalsIgnoreCase(name)) {
                continue;
            }

            buttons.add(button);
        }

        return buttons;
    }

    public void addButton(BuiltButton button) {
        this.buttons.put(button.getSlot(), button);
    }

    public void clearButtons() {
        this.buttons.clear();
    }

}
