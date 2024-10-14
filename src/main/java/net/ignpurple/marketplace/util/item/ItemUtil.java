package net.ignpurple.marketplace.util.item;

import org.bukkit.inventory.ItemStack;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;
import org.yaml.snakeyaml.external.biz.base64Coder.Base64Coder;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class ItemUtil {

    public static String serializeItemToBase64(ItemStack itemStack) {
        try (final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
             final BukkitObjectOutputStream dataOutput = new BukkitObjectOutputStream(outputStream)
        ) {
            dataOutput.writeObject(itemStack);

            return Base64Coder.encodeLines(outputStream.toByteArray());
        } catch (IOException e) {
            throw new IllegalStateException("Unable to serialize item", e);
        }
    }

    public static ItemStack deserializeItemFromBase64(String base64) {
        try (final ByteArrayInputStream outputStream = new ByteArrayInputStream(Base64Coder.decode(base64));
             final BukkitObjectInputStream dataOutput = new BukkitObjectInputStream(outputStream)
        ) {
            return (ItemStack) dataOutput.readObject();
        } catch (IOException | ClassNotFoundException e) {
            throw new IllegalStateException("Unable to deserialize item", e);
        }
    }
}
