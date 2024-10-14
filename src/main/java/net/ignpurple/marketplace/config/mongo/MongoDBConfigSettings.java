package net.ignpurple.marketplace.config.mongo;

import net.ignpurple.marketplace.util.config.annotation.FieldVersion;
import net.ignpurple.marketplace.util.config.annotation.Section;

@Section
public class MongoDBConfigSettings {
    @FieldVersion(1)
    private boolean useMongoDB = true;

    @FieldVersion(1)
    private String connection = "mongodb";

    @FieldVersion(1)
    private String host = "127.0.0.1";

    @FieldVersion(1)
    private int port = 27017;

    @FieldVersion(1)
    private boolean useAuth = false;

    @FieldVersion(1)
    private String username = "admin";

    @FieldVersion(1)
    private String password = "";

    @FieldVersion(1)
    private String database = "marketplace";

    public boolean useMongoDB() {
        return this.useMongoDB;
    }

    public String getConnection() {
        return this.connection;
    }

    public String getHost() {
        return this.host;
    }

    public int getPort() {
        return this.port;
    }

    public boolean useAuth() {
        return this.useAuth;
    }

    public String getUsername() {
        return this.username;
    }

    public String getPassword() {
        return this.password;
    }

    public String getDatabase() {
        return this.database;
    }
}
