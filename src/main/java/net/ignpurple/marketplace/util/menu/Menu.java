package net.ignpurple.marketplace.util.menu;

import net.ignpurple.marketplace.MarketplacePlugin;
import net.ignpurple.marketplace.util.menu.built.BuiltButton;
import net.ignpurple.marketplace.util.menu.built.BuiltInventory;
import net.ignpurple.marketplace.util.menu.event.MenuButtonClickEvent;
import net.ignpurple.marketplace.util.menu.state.MenuAction;
import net.ignpurple.marketplace.util.menu.state.SwitchEnum;
import net.ignpurple.marketplace.util.menu.template.MenuButton;
import net.ignpurple.marketplace.util.menu.template.MenuTemplate;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Consumer;

public abstract class Menu<T> implements InventoryHolder {
    protected final Player player;
    private final MenuTemplate menuTemplate;
    private final List<T> requestedObjects;
    private final Map<Integer, T> slotToObject;
    private final Map<String, Consumer<MenuButtonClickEvent>> clickHandlers;
    private BuiltInventory builtInventory;
    private MenuAction currentAction = null;
    private SwitchEnum switchAction = null;
    private int page;
    private int pagesAvailable;

    public Menu(Player player, MenuTemplate menuTemplate) {
        this.player = player;
        this.menuTemplate = menuTemplate;
        this.requestedObjects = new ArrayList<>();
        this.slotToObject = new HashMap<>();
        this.clickHandlers = new HashMap<>();
        this.page = 1;

        this.paginatorClicks();
    }

    @Override
    public Inventory getInventory() {
        this.buildPaged();
        return this.builtInventory.getInventory();
    }

    public void onClick(String buttonIdentifier, Consumer<MenuButtonClickEvent> clickHandler) {
        this.clickHandlers.put(buttonIdentifier, clickHandler);
    }

    public void handleClick(InventoryClickEvent event) {
        event.setCancelled(true);
        if (event.getCurrentItem() == null) {
            return;
        }

        final BuiltButton button = this.builtInventory.getButton(event.getRawSlot());
        if (button == null) {
            return;
        }

        final Consumer<MenuButtonClickEvent> clickHandler = this.clickHandlers.get(button.getName());
        if (clickHandler == null) {
            return;
        }

        clickHandler.accept(new MenuButtonClickEvent(button));
    }

    private void paginatorClicks() {
        this.onClick("next-page", event -> {
            if (this.switchAction != null) {
                return;
            }

            if (this.page >= this.pagesAvailable) {
                return;
            }

            this.page += 1;
            this.switchAction = SwitchEnum.NEXT;

            this.executeAction(MenuAction.REFRESH, () -> this.switchAction = null);
        });

        this.onClick("previous-page", event -> {
            if (this.switchAction != null) {
                return;
            }

            if (this.page <= 1) {
                return;
            }

            this.page -= 1;
            this.switchAction = SwitchEnum.PREVIOUS;

            this.executeAction(MenuAction.REFRESH, () -> this.switchAction = null);
        });
    }

    private void buildInventory() {
        if (this.builtInventory == null) {
            final Inventory inventory = Bukkit.createInventory(this, 54, ChatColor.translateAlternateColorCodes('&', this.menuTemplate.getTitle()));
            this.builtInventory = new BuiltInventory(inventory);
        }

        this.builtInventory.clearButtons();

        final List<String> design = this.menuTemplate.getMenuDesign();
        for (int row = 0; row < design.size(); row++) {
            final String rowLayout = design.get(row);
            int slot = row * 9;

            for (int column = 0; column < rowLayout.length(); column++) {
                final String symbol = rowLayout.charAt(column) + "";
                if (symbol.trim().isEmpty()) {
                    continue;
                }

                final MenuButton menuButton = this.menuTemplate.getButtonBySymbol(symbol);
                if (menuButton == null) {
                    slot++;
                    continue;
                }

                final BuiltButton builtButton = this.createButton(slot, menuButton);
                this.applyBuiltButton(builtButton);
                slot++;
            }
        }
    }

    private void buildPaged() {
        this.requestedObjects.clear();
        this.requestedObjects.addAll(this.retrieveObjects());

        this.buildInventory();

        final List<Integer> fillableSlots = this.getFillableSlots();
        this.pagesAvailable = this.getPages(fillableSlots.size());
        for (int index = 0; index < fillableSlots.size(); index++) {
            final int slot = fillableSlots.get(index);
            final int objectIndex = index + (fillableSlots.size() * (this.page - 1));
            if (objectIndex >= this.requestedObjects.size()) {
                this.builtInventory.getInventory().setItem(slot, null);
                continue;
            }

            final T object = this.requestedObjects.get(objectIndex);

            this.slotToObject.put(slot, object);

            final ItemStack pagedItem = this.createPagedItem(object, slot, this.getDefaultItem().clone());
            this.applyBuiltButton(new BuiltButton(slot, "paged-item", pagedItem));
        }

        this.setPagedButton("next-page");
        this.setPagedButton("previous-page");
    }

    public void setPagedButton(String template) {
        final List<BuiltButton> buttons = this.builtInventory.getButtonsByName(template);
        for (final BuiltButton button : buttons) {
            this.applyBuiltButton(new BuiltButton(button.getSlot(), button.getName(), button.getItem().clone()));
        }
    }

    public void executeAction(MenuAction action, Runnable callback) {
        if (this.currentAction != null) {
            return;
        }

        final Runnable finalCallback = () -> {
            this.currentAction = null;
            if (callback != null) {
                callback.run();
            }
        };

        this.currentAction = action;
        switch (action) {
            case OPEN -> this.open(finalCallback);
            case REFRESH -> this.refresh(finalCallback);
        }
    }

    public void open() {
        this.executeAction(MenuAction.OPEN, null);
    }

    public void refresh() {
        this.executeAction(MenuAction.REFRESH, null);
    }

    public void refresh(Runnable callback) {
        Bukkit.getScheduler().runTaskAsynchronously(MarketplacePlugin.getPlugin(MarketplacePlugin.class), this::getInventory);
        if (callback == null) {
            return;
        }

        callback.run();
    }

    public void open(Runnable callback) {
        Bukkit.getScheduler().runTaskAsynchronously(MarketplacePlugin.getPlugin(MarketplacePlugin.class), () -> {
            final Inventory inventory = this.getInventory();
            Bukkit.getScheduler().runTask(MarketplacePlugin.getPlugin(MarketplacePlugin.class), () -> {
                this.player.openInventory(inventory);
                if (callback == null) {
                    return;
                }

                callback.run();
            });
        });
    }

    public void applyBuiltButton(BuiltButton builtButton) {
        this.builtInventory.addButton(builtButton);
        this.builtInventory.getInventory().setItem(builtButton.getSlot(), builtButton.getItem());
    }

    public abstract ItemStack getDefaultItem();

    public abstract List<T> retrieveObjects();

    public abstract ItemStack createPagedItem(T object, int slot, ItemStack pagedItem);

    private BuiltButton createButton(int slot, MenuButton button) {
        final ItemStack itemStack = button.getItem().clone();
        return new BuiltButton(slot, button.getName(), itemStack);
    }


    private int getPages(int fillableSlots) {
        if (this.requestedObjects.isEmpty()) {
            return 0;
        }

        return (int) Math.ceil((float) this.requestedObjects.size() / fillableSlots);
    }

    private List<Integer> getFillableSlots() {
        if (this.builtInventory == null) {
            return new LinkedList<>();
        }

        final List<Integer> emptySlots = new LinkedList<>();
        for (int slot = 0; slot < this.builtInventory.getInventory().getSize(); slot++) {
            final BuiltButton button = this.builtInventory.getButton(slot);
            if (button != null && !button.getName().equalsIgnoreCase("paged-item")) {
                continue;
            }

            emptySlots.add(slot);
        }

        return emptySlots;
    }
}
