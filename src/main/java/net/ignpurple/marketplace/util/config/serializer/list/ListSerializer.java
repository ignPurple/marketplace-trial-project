package net.ignpurple.marketplace.util.config.serializer.list;

import net.ignpurple.marketplace.util.config.ConfigManager;
import net.ignpurple.marketplace.util.config.annotation.Config;
import net.ignpurple.marketplace.util.config.annotation.Section;
import net.ignpurple.marketplace.util.config.annotation.SectionName;
import net.ignpurple.marketplace.util.config.data.ConfigSerializerContext;
import net.ignpurple.marketplace.util.config.serializer.Serializer;
import org.bukkit.Keyed;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.ConfigurationSection;

import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.List;

public class ListSerializer implements Serializer<List<Object>> {

    @Override
    public void serialize(ConfigSerializerContext configSerializerContext, String path, Object defaultObject) {
        final ConfigManager configManager = configSerializerContext.getConfigManager();
        final Config configAnnotation = configSerializerContext.getConfigAnnotation();
        final Configuration configuration = configSerializerContext.getYamlConfiguration();

        final List<Object> list = (List<Object>) defaultObject;
        System.out.println("Serializing " + path + ", " + defaultObject);
        if (!this.isSerializableKey(list.get(0))) {
            configuration.set(path, list);
            return;
        }

        for (final Object object : list) {
            final String keyString = this.keyToString(object);
            final String keyPath = path + "." + keyString + ".";
            if (this.isDefaultSerializableValue(object)) {
                System.out.println(keyPath);
                configuration.set(keyPath, object);
                return;
            }

            System.out.println("Scanning object - " + object);
            configManager.scanConfig(object.getClass(), object, configAnnotation, configuration, keyPath);
        }
    }

    @Override
    public List<Object> deserialize(ConfigSerializerContext configSerializerContext, ConfigurationSection section, String path, Object defaultObject) {
        final ConfigManager configManager = configSerializerContext.getConfigManager();
        final Config configAnnotation = configSerializerContext.getConfigAnnotation();
        final Configuration configuration = configSerializerContext.getYamlConfiguration();
        final Field mapField = configSerializerContext.getField();
        final List<Object> list = new ArrayList<>();

        final ParameterizedType listType = (ParameterizedType) mapField.getGenericType();
        final Class<?> listTypeClass = (Class<?>) listType.getActualTypeArguments()[0];
        try {
            if (section == null) {
                list.addAll(configuration.getList(path));
                return list;
            }

            for (final String keys : section.getKeys(false)) {
                final Constructor<?> constructor = listTypeClass.getDeclaredConstructor();
                constructor.setAccessible(true);
                final Object object = constructor.newInstance();
                configManager.scanConfig(listTypeClass, object, configAnnotation, configuration, path + "." + keys + ".");

                list.add(object);
            }

            return list;
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException |
                 NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    private boolean isSerializableKey(Object key) {
        return !(key instanceof Serializable);
    }

    private String keyToString(Object key) {
        if (key instanceof Serializable) {
            return key.toString();
        }

        if (key instanceof Keyed) {
            return ((Keyed) key).getKey().getKey();
        }

        final Class<?> keyClass = key.getClass();
        if (keyClass.getDeclaredAnnotation(Section.class) != null) {
            for (final Field field : keyClass.getDeclaredFields()) {
                field.setAccessible(true);
                final SectionName sectionName = field.getAnnotation(SectionName.class);
                if (sectionName == null) {
                    continue;
                }

                try {
                    return field.get(key).toString();
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
            }

            throw new IllegalStateException("Class " + keyClass.getSimpleName() + " does not have a field annotated with [SectionName]");
        }

        throw new IllegalArgumentException("How did we get here?");
    }

    private boolean isDefaultSerializableValue(Object value) {
        return value instanceof Serializable;
    }
}
