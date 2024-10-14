package net.ignpurple.marketplace.util.config.serializer.map;

import net.ignpurple.marketplace.util.config.ConfigManager;
import net.ignpurple.marketplace.util.config.annotation.Config;
import net.ignpurple.marketplace.util.config.data.ConfigSerializerContext;
import net.ignpurple.marketplace.util.config.serializer.Serializer;
import org.bukkit.Keyed;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.ConfigurationSection;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.ParameterizedType;
import java.util.HashMap;
import java.util.Map;

public class MapSerializer implements Serializer<Map<Object, Object>> {

    @Override
    public void serialize(ConfigSerializerContext configSerializerContext, String path, Object defaultObject) {
        final ConfigManager configManager = configSerializerContext.getConfigManager();
        final Config configAnnotation = configSerializerContext.getConfigAnnotation();
        final Configuration configuration = configSerializerContext.getYamlConfiguration();

        final Map<Object, Object> map = (Map<Object, Object>) defaultObject;
        for (final Map.Entry<Object, Object> entry : map.entrySet()) {
            final Object key = entry.getKey();
            if (!this.isSerializableKey(key)) {
                throw new IllegalArgumentException("Cannot serialize map to " + path + " due to unserializable key " + key.getClass().getSimpleName());
            }

            final String keyString = this.keyToString(key);
            final String keyPath = path + "." + keyString + ".";
            final Object value = entry.getValue();
            if (this.isDefaultSerializableValue(value)) {
                configuration.set(keyPath, value);
                return;
            }

            configManager.scanConfig(value.getClass(), value, configAnnotation, configuration, keyPath);
        }
    }

    @Override
    public Map<Object, Object> deserialize(ConfigSerializerContext configSerializerContext, ConfigurationSection section, String path, Object defaultObject) {
        final ConfigManager configManager = configSerializerContext.getConfigManager();
        final Config configAnnotation = configSerializerContext.getConfigAnnotation();
        final Configuration configuration = configSerializerContext.getYamlConfiguration();
        final Field mapField = configSerializerContext.getField();
        final Map<Object, Object> map = new HashMap<>();

        final ParameterizedType mapType = (ParameterizedType) mapField.getGenericType();
        final Class<?> mapKey = (Class<?>) mapType.getActualTypeArguments()[0];
        final Class<?> mapValue = (Class<?>) mapType.getActualTypeArguments()[1];
        try {
            for (final String keys : section.getKeys(false)) {
                final Object valueObject = mapValue.getDeclaredConstructor().newInstance();
                configManager.scanConfig(mapValue, valueObject, configAnnotation, configuration, path + "." + keys + ".");

                map.put(configSerializerContext.deserialize(mapKey, path, keys), valueObject);
            }

            return map;
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException |
                 NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    private boolean isSerializableKey(Object key) {
        return key instanceof String || key instanceof Number || key instanceof Enum<?> || key instanceof Keyed;
    }

    private String keyToString(Object key) {
        if (key instanceof Serializable) {
            return key.toString();
        }

        if (key instanceof Keyed) {
            return ((Keyed) key).getKey().getKey();
        }

        throw new IllegalArgumentException("How did we get here?");
    }

    private boolean isDefaultSerializableValue(Object value) {
        return value instanceof Serializable;
    }
}
