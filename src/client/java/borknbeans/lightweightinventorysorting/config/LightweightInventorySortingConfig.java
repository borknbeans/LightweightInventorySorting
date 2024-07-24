package borknbeans.lightweightinventorysorting.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class LightweightInventorySortingConfig {

    private static final File CONFIG_FILE = new File(FabricLoader.getInstance().getConfigDir().toFile(), "lightweight-inventory-sorting.json");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public static SortTypes sortType = SortTypes.ALPHANUMERIC;

    public static ButtonSize buttonSize = ButtonSize.LARGE;
    public static int xOffsetInventory = 0;
    public static int yOffsetInventory = 0;
    public static int xOffsetContainer = 0;
    public static int yOffsetContainer = 0;

    public static int sortDelay = 5;

    public static void load() {
        if (CONFIG_FILE.exists()) {
            try (FileReader reader = new FileReader(CONFIG_FILE)) {
                ConfigData data = GSON.fromJson(reader, ConfigData.class);
                sortType = data.sortType;
                buttonSize = data.buttonSize;
                xOffsetInventory = data.xOffsetInventory;
                yOffsetInventory = data.yOffsetInventory;
                xOffsetContainer = data.xOffsetContainer;
                yOffsetContainer = data.yOffsetContainer;
                sortDelay = data.sortDelay;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void save() {
        try (FileWriter writer = new FileWriter(CONFIG_FILE)) {
            ConfigData data = new ConfigData();
            data.sortType = sortType;
            data.buttonSize = buttonSize;
            data.xOffsetInventory = xOffsetInventory;
            data.yOffsetInventory = yOffsetInventory;
            data.xOffsetContainer = xOffsetContainer;
            data.yOffsetContainer = yOffsetContainer;
            data.sortDelay = sortDelay;
            GSON.toJson(data, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static class ConfigData {
        SortTypes sortType;
        ButtonSize buttonSize;
        int xOffsetInventory;
        int yOffsetInventory;
        int xOffsetContainer;
        int yOffsetContainer;
        int sortDelay;
    }
}
