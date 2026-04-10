package borknbeans.lightweightinventorysorting;

import borknbeans.lightweightinventorysorting.config.Config;
import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LightweightInventorySortingClient implements ClientModInitializer {

    private final static Map<Item, Integer> CREATIVE_INDICES = new HashMap<>();
    // Define the category for your mod's keybindings
    private static final KeyMapping.Category CATEGORY = KeyMapping.Category.register(
            Identifier.fromNamespaceAndPath("lightweight-inventory-sorting", "lightweight-inventory-sorting"));
    public static KeyMapping sortKeyBind;

    public static int getCreativeIndex(ItemStack itemStack) {
        if (CREATIVE_INDICES.isEmpty()) {
            reloadIndices();
        }
        return CREATIVE_INDICES.getOrDefault(itemStack.getItem(), 0);
    }

    public static void reloadIndices() {
        LightweightInventorySorting.LOGGER.info("Reloading item order for Lightweight Inventory Sorter");

        Minecraft client = Minecraft.getInstance();
        CreativeModeTab.ItemDisplayParameters context = new CreativeModeTab.ItemDisplayParameters(
                client.player.connection.enabledFeatures(),
                false,
                client.level.registryAccess()
        );

        CREATIVE_INDICES.clear();
        List<ItemStack> items = new ArrayList<>();

        for (CreativeModeTab group : CreativeModeTabs.allTabs()) {
            if (group.getType() != CreativeModeTab.Type.SEARCH) {
                group.buildContents(context);
                items.addAll(group.getDisplayItems());
            }
        }

        for (int i = 0; i < items.size(); i++) {
            CREATIVE_INDICES.putIfAbsent(items.get(i).getItem(), i);
        }
    }

    @Override
    public void onInitializeClient() {
        Config.load();
        registerKeyBindings();
    }

    private void registerKeyBindings() {
        sortKeyBind = new KeyMapping(
                "key.lightweight-inventory-sorting.sort",
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_R,
                CATEGORY
        );
    }
}
