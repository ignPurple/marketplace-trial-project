package net.ignpurple.marketplace.entity;

import java.math.BigDecimal;
import java.util.UUID;

public class MarketplaceTransaction {
    private UUID owner;
    private long timestamp;
    private BigDecimal price;
    private String itemName;

    public MarketplaceTransaction(UUID owner, long timestamp, BigDecimal price, String itemName) {
        this.owner = owner;
        this.timestamp = timestamp;
        this.price = price;
        this.itemName = itemName;
    }

    public UUID getOwner() {
        return this.owner;
    }

    public long getTimestamp() {
        return this.timestamp;
    }

    public BigDecimal getPrice() {
        return this.price;
    }

    public String getItemName() {
        return this.itemName;
    }
}
