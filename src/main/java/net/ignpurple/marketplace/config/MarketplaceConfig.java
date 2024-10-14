package net.ignpurple.marketplace.config;

import net.ignpurple.marketplace.config.mongo.MongoDBConfigSettings;
import net.ignpurple.marketplace.util.config.annotation.Config;
import net.ignpurple.marketplace.util.config.annotation.FieldVersion;

@Config(file = "config.yml", version = 1)
public class MarketplaceConfig {
    @FieldVersion(1)
    private final MongoDBConfigSettings mongoDBSettings = new MongoDBConfigSettings();

    public MongoDBConfigSettings getMongoDBSettings() {
        return this.mongoDBSettings;
    }
}
