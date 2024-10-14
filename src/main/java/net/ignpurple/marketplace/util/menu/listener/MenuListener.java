package net.ignpurple.marketplace.util.menu.listener;

import net.ignpurple.marketplace.util.menu.Menu;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.InventoryView;

public class MenuListener implements Listener {

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        final HumanEntity player = event.getWhoClicked();
        final InventoryView openInventoryView = player.getOpenInventory();
        final Inventory topInventory = openInventoryView.getTopInventory();
        if (event.getClickedInventory() == null) {
            return;
        }

        if (event.getSlot() == -1) {
            return;
        }

        final InventoryHolder holder = topInventory.getHolder();
        if (!(holder instanceof final Menu<?> menu)) {
            return;
        }

        menu.handleClick(event);
    }
}
