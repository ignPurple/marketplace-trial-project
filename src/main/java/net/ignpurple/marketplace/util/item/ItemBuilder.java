package net.ignpurple.marketplace.util.item;

import com.google.common.collect.Lists;
import net.ignpurple.marketplace.util.replacer.Replacer;
import org.bukkit.*;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.*;
import java.util.function.Consumer;

public class ItemBuilder {
    private static final ItemFlag[] ALL_FLAGS = ItemFlag.values();
    private ItemStack itemStack;

    public ItemBuilder(ItemStack itemStack) {
        this.itemStack = itemStack;
    }

    public static ItemBuilder create() {
        return ItemBuilder.create(Material.DIRT);
    }

    public static ItemBuilder create(Material material) {
        return ItemBuilder.create(new ItemStack(material));
    }

    public static ItemBuilder create(Material material, int data) {
        return ItemBuilder.create(new ItemStack(material, 1, (short) data));
    }

    public static ItemBuilder create(ItemStack itemStack) {
        return new ItemBuilder(itemStack.clone());
    }

    public ItemBuilder type(Material material) {
        return this.transform(itemStack -> itemStack.setType(material));
    }

    public ItemBuilder data(int data) {
        return this.transform(itemStack -> itemStack.setDurability((short) data));
    }

    public ItemBuilder transform(Consumer<ItemStack> itemStack) {
        itemStack.accept(this.itemStack);
        return this;
    }

    public ItemBuilder transformMeta(Consumer<ItemMeta> meta) {
        final ItemMeta itemMeta = this.itemStack.getItemMeta();
        if (itemMeta != null) {
            meta.accept(itemMeta);
            this.itemStack.setItemMeta(itemMeta);
        }

        return this;
    }

    public ItemBuilder lore(String... lines) {
        return this.lore(Arrays.asList(lines));
    }

    public ItemBuilder lore(Iterable<String> lines) {
        return this.transformMeta(meta -> {
            final List<String> lore = Lists.newArrayList();
            for (final String line : lines) {
                lore.add(ChatColor.translateAlternateColorCodes('&', line));
            }

            meta.setLore(lore);
        });
    }

    public ItemBuilder clearLore() {
        return this.transformMeta(meta -> meta.setLore(Lists.newArrayList()));
    }

    public ItemBuilder amount(int amount) {
        return this.transform(itemStack -> itemStack.setAmount(amount));
    }

    public ItemBuilder amount(int amount, boolean bypassMaxStackSize) {
        return this.amount(amount);
    }

    public ItemBuilder customModelData(int customModelData) {
        return this.transformMeta((meta) -> meta.setCustomModelData(customModelData));
    }

    public ItemBuilder enchant(Enchantment enchantment) {
        return this.transform(itemStack -> itemStack.addUnsafeEnchantment(enchantment, 1));
    }

    public ItemBuilder clearEnchantments() {
        return this.transform(itemStack -> itemStack.getEnchantments().keySet().forEach(itemStack::removeEnchantment));
    }

    public ItemBuilder hideAttributes() {
        return this.flag(ItemBuilder.ALL_FLAGS);
    }

    public ItemBuilder flag(ItemFlag... flags) {
        return this.transformMeta(meta -> meta.addItemFlags(flags));
    }

    public ItemBuilder showAttributes() {
        return this.unflag(ItemBuilder.ALL_FLAGS);
    }

    public ItemBuilder unflag(ItemFlag... flags) {
        return this.transformMeta(meta -> meta.removeItemFlags(flags));
    }

    public ItemBuilder glow() {
        this.enchant(Enchantment.DURABILITY, 0);
        return this.flag(ItemFlag.HIDE_ENCHANTS);
    }

    public ItemBuilder enchant(Enchantment enchantment, int level) {
        return this.transform(itemStack -> itemStack.addUnsafeEnchantment(enchantment, level));
    }

    public ItemBuilder color(Color color) {
        return this.transform(itemStack -> {
            final ItemMeta meta = itemStack.getItemMeta();

            if (meta instanceof LeatherArmorMeta armorMeta) {
                armorMeta.setColor(color);
                itemStack.setItemMeta(armorMeta);
            }
        });
    }

    public ItemBuilder head(OfflinePlayer player) {
        return this.transformSkullMeta((meta) -> meta.setOwner(player.getName()));
    }

    public ItemBuilder transformSkullMeta(Consumer<SkullMeta> skullMeta) {
        this.type(Material.PLAYER_HEAD);

        final SkullMeta meta = (SkullMeta) this.itemStack.getItemMeta();
        skullMeta.accept(meta);
        this.itemStack.setItemMeta(meta);
        return this;
    }

    public ItemBuilder name(String name) {
        return this.transformMeta((meta) -> {
            meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', name));
        });
    }

    public ItemStack build() {
        return this.itemStack;
    }

    public ItemBuilder replace(Replacer replacer) {
        final ItemMeta itemMeta = this.itemStack.getItemMeta();
        if (itemMeta == null) {
            return this;
        }

        boolean changed = false;

        final boolean hasDisplayName = itemMeta.hasDisplayName();
        final boolean hasLore = itemMeta.hasLore();
        List<String> lore = itemMeta.getLore() != null ? itemMeta.getLore() : new ArrayList<>();
        String displayName = itemMeta.getDisplayName();
        for (final Map.Entry<String, String> replace : replacer.entrySet()) {
            final String key = replace.getKey();
            final String value = replace.getValue();
            if (hasDisplayName) {
                displayName = displayName.replace(key, value);
            }

            if (hasLore) {
                final List<String> replaced = new ArrayList<>();
                for (final String loreLine : lore) {
                    replaced.add(loreLine.replace(key, value));
                }
                lore = replaced;
            }
        }

        if (hasDisplayName) {
            itemMeta.setDisplayName(displayName);
            changed = true;
        }

        if (hasLore) {
            itemMeta.setLore(lore);
            changed = true;
        }

        if (changed) {
            this.itemStack.setItemMeta(itemMeta);
        }

        return this;
    }
}
