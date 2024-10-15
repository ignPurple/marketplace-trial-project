package net.ignpurple.marketplace;

import net.ignpurple.marketplace.command.MarketplaceCommand;
import net.ignpurple.marketplace.command.TransactionsCommand;
import net.ignpurple.marketplace.config.MarketplaceConfig;
import net.ignpurple.marketplace.config.MenuConfig;
import net.ignpurple.marketplace.config.mongo.MongoDBConfigSettings;
import net.ignpurple.marketplace.database.MarketDatabase;
import net.ignpurple.marketplace.database.mongo.MongoMarketDatabase;
import net.ignpurple.marketplace.entity.MarketplaceTransaction;
import net.ignpurple.marketplace.menu.TransactionsMenu;
import net.ignpurple.marketplace.util.config.ConfigManager;
import net.ignpurple.marketplace.util.menu.Menu;
import net.ignpurple.marketplace.util.menu.listener.MenuListener;
import net.ignpurple.marketplace.util.menu.template.MenuTemplate;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.plugin.java.JavaPlugin;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class MarketplacePlugin extends JavaPlugin implements Listener {
    private ConfigManager configManager;
    private MarketDatabase database;

    @Override
    public void onEnable() {
        this.initConfig();
        this.initDatabase();
        this.initCommands();
        this.initListeners();

    }

    public ConfigManager getConfigManager() {
        return this.configManager;
    }

    public MarketDatabase getDatabase() {
        return this.database;
    }

    private void initConfig() {
        this.configManager = new ConfigManager(this);
        this.configManager.loadConfig(MenuConfig.class, MenuConfig::new);
        this.configManager.loadConfig(MarketplaceConfig.class, MarketplaceConfig::new);
    }

    private void initDatabase() {
        final MarketplaceConfig config = this.configManager.getConfig(MarketplaceConfig.class);
        final MongoDBConfigSettings mongoDBSettings = config.getMongoDBSettings();
        if (mongoDBSettings.useMongoDB()) {
            this.database = new MongoMarketDatabase(mongoDBSettings);
            return;
        }

        throw new IllegalStateException("No database selected for Marketplace, disabling plugin.");
    }

    private void initCommands() {
        this.getServer().getPluginCommand("marketplace").setExecutor(new MarketplaceCommand(this));
        this.getServer().getPluginCommand("transactions").setExecutor(new TransactionsCommand(this));
    }

    private void initListeners() {
        this.getServer().getPluginManager().registerEvents(new MenuListener(), this);
    }
}