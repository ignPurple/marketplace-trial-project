package net.ignpurple.marketplace.database.mongo.codec;

import net.ignpurple.marketplace.entity.MarketplaceItem;
import net.ignpurple.marketplace.util.item.ItemUtil;
import org.bson.BsonBinary;
import org.bson.BsonReader;
import org.bson.BsonWriter;
import org.bson.codecs.*;
import org.bson.types.Decimal128;
import org.bson.types.ObjectId;

import java.util.UUID;

public class MarketplaceItemCodec implements Codec<MarketplaceItem> {
    @Override
    public MarketplaceItem decode(BsonReader bsonReader, DecoderContext decoderContext) {
        bsonReader.readStartDocument();
        bsonReader.readObjectId();
        final BsonBinary bsonBinary = bsonReader.readBinaryData();
        final BsonBinary owner = bsonReader.readBinaryData();
        final String itemBase64 = bsonReader.readString();
        final Decimal128 price = bsonReader.readDecimal128();
        bsonReader.readEndDocument();
        return new MarketplaceItem(bsonBinary.asUuid(), owner.asUuid(), ItemUtil.deserializeItemFromBase64(itemBase64), price.bigDecimalValue());
    }

    @Override
    public void encode(BsonWriter bsonWriter, MarketplaceItem marketplaceItem, EncoderContext encoderContext) {
        bsonWriter.writeStartDocument();
        bsonWriter.writeObjectId(ObjectId.get());
        bsonWriter.writeBinaryData("itemKey", new BsonBinary(marketplaceItem.getItemKey()));
        bsonWriter.writeBinaryData("owner", new BsonBinary(marketplaceItem.getOwner()));
        bsonWriter.writeString("item", ItemUtil.serializeItemToBase64(marketplaceItem.getItemStack()));
        bsonWriter.writeDecimal128("price", new Decimal128(marketplaceItem.getPrice()));
        bsonWriter.writeEndDocument();
    }

    @Override
    public Class<MarketplaceItem> getEncoderClass() {
        return MarketplaceItem.class;
    }
}
