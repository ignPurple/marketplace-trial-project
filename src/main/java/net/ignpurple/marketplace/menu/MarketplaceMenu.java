package net.ignpurple.marketplace.menu;

import com.google.common.collect.Lists;
import net.ignpurple.marketplace.MarketplacePlugin;
import net.ignpurple.marketplace.config.MenuConfig;
import net.ignpurple.marketplace.database.MarketDatabase;
import net.ignpurple.marketplace.entity.MarketplaceItem;
import net.ignpurple.marketplace.util.Constants;
import net.ignpurple.marketplace.util.item.ItemBuilder;
import net.ignpurple.marketplace.util.menu.Menu;
import net.ignpurple.marketplace.util.menu.template.MenuTemplate;
import net.ignpurple.marketplace.util.replacer.Replacer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MarketplaceMenu extends Menu<MarketplaceItem> {
    private final MenuConfig config;
    private final MarketDatabase database;

    public MarketplaceMenu(Player player, MarketplacePlugin plugin, MenuTemplate menuTemplate) {
        super(player, menuTemplate);

        this.config = plugin.getConfigManager().getConfig(MenuConfig.class);
        this.database = plugin.getDatabase();
    }

    @Override
    public ItemStack getDefaultItem() {
        return this.config.getMarketplaceItem();
    }

    @Override
    public List<MarketplaceItem> retrieveObjects() {
        final List<MarketplaceItem> transactions = Lists.newArrayList(this.database.getItems());
        Collections.reverse(transactions);
        return transactions;
    }

    @Override
    public ItemStack createPagedItem(MarketplaceItem object, int slot, ItemStack pagedItem) {
        final ItemBuilder itemBuilder = ItemBuilder.create(pagedItem);
        final ItemStack marketplaceItemStack = object.getItemStack();
        itemBuilder.type(marketplaceItemStack.getType());
        if (marketplaceItemStack.hasItemMeta()) {
            final ItemMeta meta = marketplaceItemStack.getItemMeta();
            final ItemMeta pagedItemMeta = pagedItem.getItemMeta();

            final List<String> lore = new ArrayList<>();
            if (meta.hasLore()) {
                lore.addAll(meta.getLore());
                lore.add("");

            }

            if (pagedItemMeta.hasLore()) {
                lore.addAll(pagedItemMeta.getLore());
            }

            itemBuilder.lore(lore);
            itemBuilder.customModelData(meta.getCustomModelData());
            if (meta.hasDisplayName()) {
                itemBuilder.name(meta.getDisplayName());
            }
        }

        itemBuilder.replace(
            Replacer.create()
                .replacer(
                    "%seller",
                    Bukkit.getOfflinePlayer(object.getOwner()).getName()
                ).replacer(
                    "%cost%",
                    Constants.COST_FORMAT.format(object.getPrice())
                ).replacer(
                    "%amount%",
                    String.valueOf(marketplaceItemStack.getAmount())
                )
        );
        return itemBuilder.build();
    }
}
