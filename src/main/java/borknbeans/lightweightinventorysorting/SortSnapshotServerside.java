package borknbeans.lightweightinventorysorting;

import com.google.gson.*;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import net.minecraft.world.item.ItemStack;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Base64;
import java.util.List;
import java.util.zip.GZIPInputStream;

public class SortSnapshotServerside {
    public List<ItemStack> inventory;

    public SortSnapshotServerside decode(String compressedData) {
        try {
            byte[] decoded = Base64.getDecoder().decode(compressedData);
            ByteArrayInputStream bais = new ByteArrayInputStream(decoded);
            GZIPInputStream gzipIn = new GZIPInputStream(bais);

            byte[] buffer = new byte[1024];
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            int len;
            while ((len = gzipIn.read(buffer)) > 0) {
                baos.write(buffer, 0, len);
            }
            gzipIn.close();

            String json = baos.toString();
            Gson gson = new GsonBuilder()
                    .registerTypeAdapter(ItemStack.class, new ItemStackDeserializer())
                    .create();

            return gson.fromJson(json, SortSnapshotServerside.class);
        } catch (IOException e) {
            throw new RuntimeException("Failed to decompress inventory data", e);
        }
    }

    private static class ItemStackDeserializer implements JsonDeserializer<ItemStack> {
        @Override
        public ItemStack deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            JsonObject obj = json.getAsJsonObject();

            if (obj.has("empty") && obj.get("empty").getAsBoolean()) {
                return ItemStack.EMPTY;
            }

            DataResult<ItemStack> result = ItemStack.CODEC.parse(JsonOps.INSTANCE, obj);

            if (result.error().isPresent()) {
                throw new JsonParseException("Failed to deserialize ItemStack: " + result.error().get().message());
            }

            return result.result().orElse(ItemStack.EMPTY);
        }
    }
}
