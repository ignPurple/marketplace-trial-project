package net.ignpurple.marketplace.menu;

import com.google.common.collect.Lists;
import net.ignpurple.marketplace.MarketplacePlugin;
import net.ignpurple.marketplace.config.MenuConfig;
import net.ignpurple.marketplace.database.MarketDatabase;
import net.ignpurple.marketplace.entity.MarketplaceTransaction;
import net.ignpurple.marketplace.util.Constants;
import net.ignpurple.marketplace.util.item.ItemBuilder;
import net.ignpurple.marketplace.util.menu.Menu;
import net.ignpurple.marketplace.util.menu.template.MenuTemplate;
import net.ignpurple.marketplace.util.replacer.Replacer;
import net.ignpurple.marketplace.util.time.Timer;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Collections;
import java.util.List;

public class TransactionsMenu extends Menu<MarketplaceTransaction> {
    private final MenuConfig config;
    private final MarketDatabase database;

    public TransactionsMenu(Player player, MarketplacePlugin plugin, MenuTemplate menuTemplate) {
        super(player, menuTemplate);

        this.config = plugin.getConfigManager().getConfig(MenuConfig.class);
        this.database = plugin.getDatabase();
    }

    @Override
    public ItemStack getDefaultItem() {
        return this.config.getTransactionItem();
    }

    @Override
    public List<MarketplaceTransaction> retrieveObjects() {
        final List<MarketplaceTransaction> transactions = Lists.newArrayList(this.database.getTransactions(this.player.getUniqueId()));
        Collections.reverse(transactions);
        return transactions;
    }

    @Override
    public ItemStack createPagedItem(MarketplaceTransaction object, int slot, ItemStack pagedItem) {
        final ItemBuilder itemBuilder = ItemBuilder.create(pagedItem);
        itemBuilder.replace(
            Replacer.create()
                .replacer(
                    "%time%",
                    new Timer.Builder()
                        .timeSince(object.getTimestamp())
                        .format(Timer.Format.SHORT)
                        .build()
                        .build()
                ).replacer(
                    "%cost%",
                    Constants.COST_FORMAT.format(object.getPrice())
                ).replacer(
                    "%item_name%",
                    ChatColor.translateAlternateColorCodes('&', object.getItemName())
                )
        );
        return itemBuilder.build();
    }
}
