package net.ignpurple.marketplace.util.config.serializer;

import net.ignpurple.marketplace.util.config.data.ConfigSerializerContext;
import org.bukkit.configuration.ConfigurationSection;

public interface Serializer<T> {
    default void serialize(ConfigSerializerContext configSerializerContext, String path, Object defaultObject) {}

    default T deserialize(ConfigSerializerContext configSerializerContext, ConfigurationSection section, String key, Object defaultObject) {
        return (T) defaultObject;
    }
}
