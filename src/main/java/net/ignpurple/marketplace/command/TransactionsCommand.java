package net.ignpurple.marketplace.command;

import net.ignpurple.marketplace.MarketplacePlugin;
import net.ignpurple.marketplace.config.MenuConfig;
import net.ignpurple.marketplace.menu.MarketplaceMenu;
import net.ignpurple.marketplace.menu.TransactionsMenu;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class TransactionsCommand implements CommandExecutor {
    private final MarketplacePlugin plugin;

    public TransactionsCommand(MarketplacePlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String label, String[] args) {
        if (!commandSender.hasPermission("marketplace.history")) {
            commandSender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&cYou do not have permission for this command!"));
            return true;
        }

        if (!(commandSender instanceof Player player)) {
            commandSender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&cYou need to be a player to execute this command!"));
            return true;
        }

        new TransactionsMenu(player, this.plugin, this.plugin.getConfigManager().getConfig(MenuConfig.class).getTransactionsMenu()).open();
        return true;
    }
}
