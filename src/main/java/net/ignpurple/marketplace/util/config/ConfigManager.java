package net.ignpurple.marketplace.util.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.ignpurple.marketplace.util.Constants;
import net.ignpurple.marketplace.util.config.annotation.*;
import net.ignpurple.marketplace.util.config.data.ConfigSerializerContext;
import net.ignpurple.marketplace.util.config.serializer.Serializer;
import net.ignpurple.marketplace.util.config.serializer.bukkit.ItemStackSerializer;
import net.ignpurple.marketplace.util.config.serializer.list.ListSerializer;
import net.ignpurple.marketplace.util.config.serializer.map.MapSerializer;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;

@SuppressWarnings("unchecked")
public class ConfigManager {
    private final JavaPlugin plugin;
    private final Logger logger;
    private final Map<Class<?>, Object> configurations;
    private final Map<Class<?>, Configuration> bukkitConfigurations;
    private final Map<Class<?>, Serializer<?>> serializers;

    // Used for deserialization
    public static final Gson GSON = new GsonBuilder().setLenient().create();

    public ConfigManager(JavaPlugin plugin) {
        this.plugin = plugin;
        this.logger = this.plugin.getLogger();
        this.configurations = new IdentityHashMap<>();
        this.bukkitConfigurations = new IdentityHashMap<>();
        this.serializers = new IdentityHashMap<>();

        this.serializers.put(Map.class, new MapSerializer());
        this.serializers.put(HashMap.class, new MapSerializer());
        this.serializers.put(List.class, new ListSerializer());
        this.serializers.put(ArrayList.class, new ListSerializer());
        this.serializers.put(ItemStack.class, new ItemStackSerializer());
    }

    public <T> T getConfig(Class<T> configClass) {
        return (T) this.configurations.get(configClass);
    }

    public <T> void loadConfig(Class<T> configClass, Supplier<T> configObject) {
        final T config = configObject.get();
        final Config configAnnotation = configClass.getDeclaredAnnotation(Config.class);
        if (configAnnotation == null) {
            this.logger.warning("Config " + configClass.getSimpleName() + " has no @Config annotation attached.");
            return;
        }

        final File file = new File(this.plugin.getDataFolder(), configAnnotation.file());
        if (!file.exists()) {
            file.getParentFile().mkdirs();
            try {
                file.createNewFile();
            } catch (IOException e) {
                throw new IllegalStateException("Unable to create config file: " + configAnnotation.file(), e);
            }
        }

        final YamlConfiguration yamlConfiguration = new YamlConfiguration();
        try {
            yamlConfiguration.load(file);

            this.scanConfig(configClass, config, configAnnotation, yamlConfiguration, "");

            if (!yamlConfiguration.contains("config-version")) {
                yamlConfiguration.set("config-version", configAnnotation.version());
            }

            yamlConfiguration.save(file);
        } catch (InvalidConfigurationException | IOException exception) {
            this.logger.warning("Exception when loading configuration " + configClass.getSimpleName());
            throw new RuntimeException(exception);
        }

        this.configurations.put(configClass, config);
        this.bukkitConfigurations.put(configClass, yamlConfiguration);
    }

    public <T> T deserialize(ConfigSerializerContext context, Class<T> objectClass, String path, String key) {
        final Serializer<?> existingSerializer = this.getSerializer(objectClass);
        if (existingSerializer != null) {
            return (T) existingSerializer.deserialize(context, context.getYamlConfiguration().getConfigurationSection(path), path, key);
        }

        throw new IllegalArgumentException("Could not deserialize " + objectClass.getSimpleName() + ", consider creating your own serializer.");
    }

    public void scanConfig(Class<?> configClass, Object config, Config configAnnotation, Configuration yamlConfiguration, String parentPath) {
        try {
            for (final Field field : configClass.getDeclaredFields()) {
                field.setAccessible(true);

                final Path path = field.getDeclaredAnnotation(Path.class);
                final FieldVersion fieldVersion = field.getDeclaredAnnotation(FieldVersion.class);
                final String fieldPath = String.join("-", Constants.FIELD_NAME_SEPARATOR.split(field.getName())).toLowerCase(Locale.ROOT);
                final String fullPath = (parentPath != null ? parentPath : "") + (path == null ? fieldPath : path.value());

                final Object object = field.get(config);
                if (field.getType().isAnnotationPresent(Section.class)) {
                    final Class<?> objectClass = object.getClass();
                    this.scanConfig(objectClass, object, configAnnotation, yamlConfiguration, fullPath + ".");
                    continue;
                }

                this.saveFieldToConfig(configAnnotation, yamlConfiguration, object, fullPath, field, fieldVersion == null ? configAnnotation.version() : fieldVersion.value());
                this.loadFieldFromConfig(configAnnotation, yamlConfiguration, fullPath, config, (Class<Object>) field.getType(), object, field);
            }
        } catch (IllegalAccessException exception) {
            this.logger.warning("Exception when scanning configuration " + configClass.getSimpleName());
            throw new RuntimeException(exception);
        }
    }

    public <T> Serializer<? extends T> getSerializer(Class<T> type) {
        if (this.serializers.containsKey(type)) {
            return (Serializer<? extends T>) this.serializers.get(type);
        }

        Class<?> currentType = type;
        while (currentType != null && (currentType != Object.class)) {
            for (final Class<?> interfaces : currentType.getInterfaces()) {
                if (!this.serializers.containsKey(interfaces)) {
                    continue;
                }

                return (Serializer<? extends T>) this.serializers.get(interfaces);
            }

            currentType = currentType.getSuperclass();
            if (!this.serializers.containsKey(currentType)) {
                continue;
            }

            return (Serializer<? extends T>) this.serializers.get(currentType);
        }

        return null;
    }

    public void registerSerializer(Class<?> objectClass, Serializer<?> serializer) {
        this.serializers.put(objectClass, serializer);
    }

    private void saveFieldToConfig(Config configAnnotation, Configuration yamlConfiguration, Object object, String path, Field field, int fieldVersion) {
        if (fieldVersion <= configAnnotation.version() && yamlConfiguration.contains(path)) {
            return;
        }

        final Serializer<?> existingSerializer = this.getSerializer(object.getClass());
        if (existingSerializer != null) {
            existingSerializer.serialize(
                new ConfigSerializerContext(
                    this,
                    configAnnotation,
                    yamlConfiguration,
                    field
                ),
                path,
                object
            );
            return;
        }

        final Serializable serializableAnnotation = field.getDeclaredAnnotation(Serializable.class);
        if (serializableAnnotation != null) {
            try {
                final Class<? extends Serializer<?>> serializerClass = serializableAnnotation.value();
                final Serializer<?> serializer = serializerClass.getDeclaredConstructor().newInstance();

                serializer.serialize(
                    new ConfigSerializerContext(
                        this,
                        configAnnotation,
                        yamlConfiguration,
                        field
                    ),
                    path,
                    object
                );
            } catch (InstantiationException | IllegalAccessException | NoSuchMethodException |
                     InvocationTargetException e) {
                throw new RuntimeException(e);
            }
            return;
        }
        yamlConfiguration.set(path, object);
    }

    private void loadFieldFromConfig(Config configAnnotation, Configuration yamlConfiguration, String path, Object
        parent, Class<Object> defaultObjectClass, Object defaultObject, Field field) {
        final Object object = yamlConfiguration.get(path, defaultObject);
        if (Modifier.isFinal(field.getModifiers())) {
            this.logger.warning("Could not set field " + field.getName() + " in class " + path.getClass().getSimpleName() + " due to \"final\" modifier.");
            this.logger.warning("Please remove the final modifier to allow loading to work correctly for this method.");
            return;
        }

        try {
            if (defaultObject instanceof Number) {
                field.set(parent, GSON.getAdapter(defaultObjectClass).fromJson(object.toString()));
                return;
            }

            final Serializer<?> existingSerializer = this.getSerializer(defaultObjectClass);
            if (existingSerializer != null) {
                field.set(parent, existingSerializer.deserialize(
                    new ConfigSerializerContext(
                        this,
                        configAnnotation,
                        yamlConfiguration,
                        field
                    ),
                    yamlConfiguration.getConfigurationSection(path),
                    path,
                    defaultObject
                ));
                return;
            }

            final Serializable serializableAnnotation = field.getDeclaredAnnotation(Serializable.class);
            if (serializableAnnotation != null) {
                final Class<? extends Serializer<?>> serializerClass = serializableAnnotation.value();
                final Serializer<?> serializer = serializerClass.getDeclaredConstructor().newInstance();

                field.set(parent, serializer.deserialize(
                    new ConfigSerializerContext(
                        this,
                        configAnnotation,
                        yamlConfiguration,
                        field
                    ),
                    yamlConfiguration.getConfigurationSection(path),
                    path,
                    defaultObject
                ));

                this.registerSerializer(defaultObjectClass, serializer);
                return;
            }

            field.set(parent, object);
        } catch (IllegalAccessException | IOException | InstantiationException | NoSuchMethodException |
                 InvocationTargetException exception) {
            this.logger.log(Level.WARNING, "Error when loading field from config");
            throw new RuntimeException(exception);

        }
    }
}
