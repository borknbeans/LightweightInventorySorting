package borknbeans.lightweightinventorysorting;

import borknbeans.lightweightinventorysorting.config.LightweightInventorySortingConfig;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

public class LightweightInventorySortingClient implements ClientModInitializer {

	public static KeyBinding sortKeyBind;

	@Override
	public void onInitializeClient() {
		LightweightInventorySortingConfig.load();

		registerKeyBindings();
	}

	private void registerKeyBindings() {
		sortKeyBind = KeyBindingHelper.registerKeyBinding(new KeyBinding(
				"key.lightweight-inventory-sorting.sort",
				InputUtil.Type.KEYSYM,
				GLFW.GLFW_KEY_R,
				"category.lightweight-inventory-sorting.title"
		));
	}
}