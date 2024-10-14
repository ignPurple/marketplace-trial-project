package net.ignpurple.marketplace.database.mongo.codec;

import net.ignpurple.marketplace.entity.MarketplaceItem;
import net.ignpurple.marketplace.entity.MarketplaceTransaction;
import net.ignpurple.marketplace.util.item.ItemUtil;
import org.bson.BsonBinary;
import org.bson.BsonReader;
import org.bson.BsonWriter;
import org.bson.codecs.Codec;
import org.bson.codecs.DecoderContext;
import org.bson.codecs.EncoderContext;
import org.bson.types.Decimal128;
import org.bson.types.ObjectId;

import java.time.Instant;
import java.util.Date;

public class MarketplaceTransactionCodec implements Codec<MarketplaceTransaction> {

    @Override
    public MarketplaceTransaction decode(BsonReader bsonReader, DecoderContext decoderContext) {
        bsonReader.readStartDocument();
        bsonReader.readObjectId();
        final BsonBinary owner = bsonReader.readBinaryData();
        final long timestamp = bsonReader.readInt64();
        final Decimal128 price = bsonReader.readDecimal128();
        final String itemName = bsonReader.readString();
        bsonReader.readEndDocument();
        return new MarketplaceTransaction(owner.asUuid(), timestamp, price.bigDecimalValue(), itemName);
    }

    @Override
    public void encode(BsonWriter bsonWriter, MarketplaceTransaction marketplaceTransaction, EncoderContext encoderContext) {
        bsonWriter.writeStartDocument();
        bsonWriter.writeBinaryData("owner", new BsonBinary(marketplaceTransaction.getOwner()));
        bsonWriter.writeInt64("timestamp", marketplaceTransaction.getTimestamp());
        bsonWriter.writeDecimal128("price", new Decimal128(marketplaceTransaction.getPrice()));
        bsonWriter.writeString("itemName", marketplaceTransaction.getItemName());
        bsonWriter.writeEndDocument();
    }

    @Override
    public Class<MarketplaceTransaction> getEncoderClass() {
        return MarketplaceTransaction.class;
    }
}
