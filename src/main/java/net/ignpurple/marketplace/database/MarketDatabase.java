package net.ignpurple.marketplace.database;

import net.ignpurple.marketplace.entity.MarketplaceItem;
import net.ignpurple.marketplace.entity.MarketplaceTransaction;

import java.math.BigDecimal;
import java.util.UUID;

public interface MarketDatabase {
    /**
     * Get all the items in the marketplace
     */
    Iterable<MarketplaceItem> getItems();

    /**
     * Get the item from the database using its item key
     */
    MarketplaceItem getItem(UUID itemKey);

    void addTransaction(UUID owner, long timestamp, BigDecimal price, String itemName);

    Iterable<MarketplaceTransaction> getTransactions(UUID owner);

    /**
     * Close the current Database connection
     */
    void closeConnection();
}
