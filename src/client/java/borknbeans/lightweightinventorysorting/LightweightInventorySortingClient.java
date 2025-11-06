package borknbeans.lightweightinventorysorting;

import borknbeans.lightweightinventorysorting.config.Config;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemGroups;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;

import java.util.*;

public class LightweightInventorySortingClient implements ClientModInitializer {

	private final static Map<Item, Integer> CREATIVE_INDICES = new HashMap<>();

	public static KeyBinding sortKeyBind;

	@Override
	public void onInitializeClient() {
		Config.load();
		registerKeyBindings();
	}

	private void registerKeyBindings() {
        KeyBinding.Category titleCategory = KeyBinding.Category.create(Identifier.of("category.lightweight-inventory-sorting.title"));
		sortKeyBind = KeyBindingHelper.registerKeyBinding(new KeyBinding(
			"key.lightweight-inventory-sorting.sort",
			InputUtil.Type.KEYSYM,
			GLFW.GLFW_KEY_R,
			titleCategory
		));
	}

	public static int getCreativeIndex(ItemStack itemStack) {
		if (CREATIVE_INDICES.isEmpty()) {
			reloadIndices();
		}
		return CREATIVE_INDICES.getOrDefault(itemStack.getItem(), 0);
	}

	/**
	 * Saves the creative inventory index of all registered items.
	 * This should run after all items have been registered.
	 */
	public static void reloadIndices() {
		LightweightInventorySorting.LOGGER.info("Reloading item order for Lightweight Inventory Sorter");

		// For updating the groups (thanks EMI)
		MinecraftClient client = MinecraftClient.getInstance();
		ItemGroup.DisplayContext context = new ItemGroup.DisplayContext(client.player.networkHandler.getEnabledFeatures(), false, client.world.getRegistryManager());

		CREATIVE_INDICES.clear();

        List<ItemStack> items = new ArrayList<>();

		for (ItemGroup group : ItemGroups.getGroups()) {
			if (group.getType() != ItemGroup.Type.SEARCH) {
				group.updateEntries(context);
				items.addAll(group.getSearchTabStacks());
			}
		}

		for (int i = 0; i < items.size(); i++) {
			CREATIVE_INDICES.putIfAbsent(items.get(i).getItem(), i);
		}
    }
}