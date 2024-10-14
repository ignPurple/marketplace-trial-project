package net.ignpurple.marketplace.util.config.serializer.bukkit;

import net.ignpurple.marketplace.util.config.ConfigManager;
import net.ignpurple.marketplace.util.config.annotation.Config;
import net.ignpurple.marketplace.util.config.data.ConfigSerializerContext;
import net.ignpurple.marketplace.util.config.serializer.Serializer;
import net.ignpurple.marketplace.util.item.ItemBuilder;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.ParameterizedType;
import java.util.*;

public class ItemStackSerializer implements Serializer<ItemStack> {

    @Override
    public void serialize(ConfigSerializerContext configSerializerContext, String path, Object defaultObject) {
        final ConfigManager configManager = configSerializerContext.getConfigManager();
        final Config configAnnotation = configSerializerContext.getConfigAnnotation();
        final Configuration configuration = configSerializerContext.getYamlConfiguration();

        final ItemStack item = (ItemStack) defaultObject;
        configuration.set(path + ".material", item.getType().name());
        configuration.set(path + ".amount", item.getAmount());
        if (!item.hasItemMeta()) {
            return;
        }

        final ItemMeta meta = item.getItemMeta();
        if (meta.hasCustomModelData()) {
            configuration.set(path + ".model-data", meta.getCustomModelData());
        }

        if (meta.hasDisplayName()) {
            configuration.set(path + ".display-name", meta.getDisplayName().replace(ChatColor.COLOR_CHAR, '&'));
        }

        if (meta.hasLore()) {
            final List<String> replacedColors = new ArrayList<>();
            for (final String lore : meta.getLore()) {
                replacedColors.add(lore.replace(ChatColor.COLOR_CHAR, '&'));
            }

            configuration.set(path + ".lore", replacedColors);
        }
    }

    @Override
    public ItemStack deserialize(ConfigSerializerContext configSerializerContext, ConfigurationSection section, String path, Object defaultObject) {
        final ConfigManager configManager = configSerializerContext.getConfigManager();
        final Config configAnnotation = configSerializerContext.getConfigAnnotation();
        final Configuration configuration = configSerializerContext.getYamlConfiguration();
        final Field mapField = configSerializerContext.getField();
        final ItemBuilder item = ItemBuilder.create(Material.getMaterial(configuration.getString(path + ".material").toUpperCase(Locale.ROOT)));
        if (configuration.contains(path + ".amount")) {
            item.amount(configuration.getInt(path + ".amount"));
        }

        if (configuration.contains(path + ".model-data")) {
            item.customModelData(configuration.getInt(path + ".model-data"));
        }

        if (configuration.contains(path + ".display-name")) {
            item.name(configuration.getString(path + ".display-name"));
        }

        if (configuration.contains(path + ".lore")) {
            item.lore(configuration.getStringList(path + ".lore"));
        }

        return item.build();
    }
}
