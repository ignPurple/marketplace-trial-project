package net.ignpurple.marketplace.entity;

import org.bukkit.inventory.ItemStack;

import java.math.BigDecimal;
import java.util.UUID;

public class MarketplaceItem {
    private final UUID itemKey;
    private final UUID owner;
    private final ItemStack itemStack;
    private final BigDecimal price;

    public MarketplaceItem(UUID itemKey, UUID owner, ItemStack itemStack, BigDecimal price) {
        this.itemKey = itemKey;
        this.owner = owner;
        this.itemStack = itemStack;
        this.price = price;
    }

    public UUID getItemKey() {
        return this.itemKey;
    }

    public UUID getOwner() {
        return this.owner;
    }

    public ItemStack getItemStack() {
        return this.itemStack;
    }

    public BigDecimal getPrice() {
        return this.price;
    }
}
