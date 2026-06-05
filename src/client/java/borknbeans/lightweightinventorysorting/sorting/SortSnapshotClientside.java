package borknbeans.lightweightinventorysorting.sorting;

import com.google.gson.*;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.ItemStack;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Base64;
import java.util.List;
import java.util.zip.GZIPOutputStream;

public class SortSnapshotClientside {
    private final List<ItemStack> inventory;

    public SortSnapshotClientside(List<ItemStack> inventory) {
        this.inventory = inventory;
    }

    public String encode() {
        Gson gson = new GsonBuilder()
                .registerTypeAdapter(ItemStack.class, new ItemStackSerializer())
                .create();
        String json = gson.toJson(this);

        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            GZIPOutputStream gzipOut = new GZIPOutputStream(baos);
            gzipOut.write(json.getBytes());
            gzipOut.close();

            return Base64.getEncoder().encodeToString(baos.toByteArray());
        } catch (IOException e) {
            throw new RuntimeException("Failed to compress inventory data", e);
        }
    }

    private static class ItemStackSerializer implements JsonSerializer<ItemStack> {
        @Override
        public JsonElement serialize(ItemStack src, Type typeOfSrc, JsonSerializationContext context) {
            JsonObject json = new JsonObject();
            if (src.isEmpty()) {
                json.addProperty("empty", true);
                return json;
            }
            json.addProperty("id", BuiltInRegistries.ITEM.getKey(src.getItem()).toString());
            json.addProperty("count", src.getCount());
            return json;
        }
    }
}
