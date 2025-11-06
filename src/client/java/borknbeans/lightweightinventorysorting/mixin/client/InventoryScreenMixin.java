package borknbeans.lightweightinventorysorting.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import borknbeans.lightweightinventorysorting.LightweightInventorySortingClient;
import borknbeans.lightweightinventorysorting.config.Config;
import borknbeans.lightweightinventorysorting.sorting.SortButton;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.client.input.KeyInput;
import net.minecraft.client.input.MouseInput;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.text.Text;

@Mixin(InventoryScreen.class)
public abstract class InventoryScreenMixin extends HandledScreen<PlayerScreenHandler> {

    @Unique
    private SortButton sortButton;

    public InventoryScreenMixin(PlayerScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
    }

    @Inject(method = "init", at = @At("RETURN"))
    private void onInit(CallbackInfo ci) {
        int size = Config.buttonSize.getButtonSize();
        sortButton = new SortButton(0, 0, size, size, Text.literal("S"), 9, 35);
        setButtonCoordinates();

        this.addDrawableChild(sortButton);
    }

    @Inject(method = "render", at = @At("RETURN"))
    private void onRender(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        if (sortButton != null) {
            setButtonCoordinates();
            sortButton.render(context, mouseX, mouseY, delta);
        }
    }

    // Updated: uses KeyInput instead of (int, int, int)
    @Override
    public boolean keyPressed(KeyInput keyInput) {
        if (LightweightInventorySortingClient.sortKeyBind.matchesKey(keyInput)) {
            sortButton.onPress(new MouseInput(0,0)); // Simulate a click
            return true;
        }
        return super.keyPressed(keyInput);
    }

    // Updated: uses Click + boolean doubleClick
    @Override
    public boolean mouseClicked(Click click, boolean doubleClick) {
        if (LightweightInventorySortingClient.sortKeyBind.matchesMouse(click)) {
            sortButton.simulateClick(click); // Simulate a click
            return true;
        }

        return super.mouseClicked(click, doubleClick);
    }
    
    @Unique
    private void setButtonCoordinates() {
        sortButton.setX(this.x + this.backgroundWidth - 20 + Config.xOffsetInventory);
        sortButton.setY(this.height / 2 - 15 + Config.yOffsetInventory);
    }
}
