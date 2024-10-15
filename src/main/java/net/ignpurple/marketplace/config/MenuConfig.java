package net.ignpurple.marketplace.config;

import net.ignpurple.marketplace.util.config.annotation.Config;
import net.ignpurple.marketplace.util.config.annotation.FieldVersion;
import net.ignpurple.marketplace.util.item.ItemBuilder;
import net.ignpurple.marketplace.util.menu.template.MenuButton;
import net.ignpurple.marketplace.util.menu.template.MenuTemplate;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

@Config(file = "menus.yml", version = 1)
public class MenuConfig {
    @FieldVersion(1)
    private final MenuTemplate transactionsMenu = MenuTemplate.builder()
        .title("&6&lTransactions")
        .design(
            "$$$$$$$$$",
            "$_______$",
            "$_______$",
            "$_______$",
            "$_______$",
            "<$$$$$$$>"
        ).button(
            MenuButton.builder()
                .name("background")
                .identifier("$")
                .item(ItemBuilder.create(Material.ORANGE_STAINED_GLASS_PANE).name("&7").build())
                .build()
        ).button(
            MenuButton.builder()
                .name("previous-page")
                .identifier("<")
                .item(ItemBuilder.create(Material.ARROW).name("&7Previous Page").build())
                .build()
        ).button(
            MenuButton.builder()
                .name("next-page")
                .identifier(">")
                .item(ItemBuilder.create(Material.ARROW).name("&7Next Page").build())
                .build()
        ).build();

    @FieldVersion(1)
    private final MenuTemplate marketplaceMenu = MenuTemplate.builder()
        .title("&4&lMarketplace")
        .design(
            "$$$$$$$$$",
            "$_______$",
            "$_______$",
            "$_______$",
            "$_______$",
            "<$$$$$$$>"
        ).button(
            MenuButton.builder()
                .name("background")
                .identifier("$")
                .item(ItemBuilder.create(Material.RED_STAINED_GLASS_PANE).name("&7").build())
                .build()
        ).button(
            MenuButton.builder()
                .name("previous-page")
                .identifier("<")
                .item(ItemBuilder.create(Material.ARROW).name("&7Previous Page").build())
                .build()
        ).button(
            MenuButton.builder()
                .name("next-page")
                .identifier(">")
                .item(ItemBuilder.create(Material.ARROW).name("&7Next Page").build())
                .build()
        ).build();

    @FieldVersion(1)
    private ItemStack transactionItem = ItemBuilder.create(Material.GOLD_INGOT)
        .name("&6&lTransaction")
        .lore(
            "&7&o( %time% ago )",
            "",
            "&eCost: &2$&a%cost%",
            "&eItem: &f%item_name%"
        ).build();

    @FieldVersion(1)
    private ItemStack marketplaceItem = ItemBuilder.create(Material.STONE)
        .name("&f%item_name%")
        .lore(
            "&eCost: &2$&a%cost%",
            "&eAmount: &6%amount%x",
            "&eSeller: &f%seller%"
        ).build();

    public MenuTemplate getTransactionsMenu() {
        return this.transactionsMenu;
    }

    public MenuTemplate getMarketplaceMenu() {
        return this.marketplaceMenu;
    }

    public ItemStack getTransactionItem() {
        return this.transactionItem;
    }

    public ItemStack getMarketplaceItem() {
        return this.marketplaceItem;
    }
}
