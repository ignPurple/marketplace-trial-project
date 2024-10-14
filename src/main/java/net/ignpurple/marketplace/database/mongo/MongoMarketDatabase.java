package net.ignpurple.marketplace.database.mongo;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.MongoCredential;
import com.mongodb.client.*;
import com.mongodb.client.model.Filters;
import net.ignpurple.marketplace.config.mongo.MongoDBConfigSettings;
import net.ignpurple.marketplace.database.MarketDatabase;
import net.ignpurple.marketplace.database.mongo.codec.MarketplaceItemCodec;
import net.ignpurple.marketplace.database.mongo.codec.MarketplaceTransactionCodec;
import net.ignpurple.marketplace.entity.MarketplaceItem;
import net.ignpurple.marketplace.entity.MarketplaceTransaction;
import org.bson.UuidRepresentation;
import org.bson.codecs.configuration.CodecRegistries;
import org.bson.codecs.configuration.CodecRegistry;

import java.math.BigDecimal;
import java.util.UUID;

public class MongoMarketDatabase implements MarketDatabase {
    private final MongoClient mongoClient;
    private final MongoDatabase database;

    public MongoMarketDatabase(MongoDBConfigSettings mongoDBSettings) {
        final CodecRegistry customCodecs = CodecRegistries.fromCodecs(new MarketplaceItemCodec(), new MarketplaceTransactionCodec());
        final CodecRegistry codecs = CodecRegistries.fromRegistries(MongoClientSettings.getDefaultCodecRegistry(), customCodecs);
        final MongoClientSettings.Builder settingsBuilder =
            MongoClientSettings.builder()
                .applyConnectionString(new ConnectionString(mongoDBSettings.getConnection() + "://" + mongoDBSettings.getHost() + ":" + mongoDBSettings.getPort()))
                .uuidRepresentation(UuidRepresentation.STANDARD)
                .codecRegistry(codecs);
        if (mongoDBSettings.useAuth()) {
            settingsBuilder.credential(MongoCredential.createCredential(mongoDBSettings.getUsername(), mongoDBSettings.getDatabase(), mongoDBSettings.getPassword().toCharArray()));
        }

        this.mongoClient = MongoClients.create(
            settingsBuilder.build()
        );
        this.database = this.mongoClient.getDatabase(mongoDBSettings.getDatabase());
    }

    @Override
    public FindIterable<MarketplaceItem> getItems() {
        final MongoCollection<MarketplaceItem> documents = this.database.getCollection("items", MarketplaceItem.class);
        return documents.find();
    }

    @Override
    public MarketplaceItem getItem(UUID itemKey) {
        final MongoCollection<MarketplaceItem> documents = this.database.getCollection("items", MarketplaceItem.class);
        return documents.find(Filters.eq("itemKey", itemKey)).first();
    }

    @Override
    public void addTransaction(UUID owner, long timestamp, BigDecimal price, String itemName) {
        final MongoCollection<MarketplaceTransaction> documents = this.database.getCollection("transactions", MarketplaceTransaction.class);
        documents.insertOne(new MarketplaceTransaction(owner, timestamp, price, itemName));
    }

    @Override
    public FindIterable<MarketplaceTransaction> getTransactions(UUID owner) {
        final MongoCollection<MarketplaceTransaction> documents = this.database.getCollection("transactions", MarketplaceTransaction.class);
        return documents.find();
    }

    @Override
    public void closeConnection() {
        this.mongoClient.close();
    }
}
