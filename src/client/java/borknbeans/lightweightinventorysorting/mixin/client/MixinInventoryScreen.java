package borknbeans.lightweightinventorysorting.mixin.client;

import borknbeans.lightweightinventorysorting.ContainerSortButton;
import borknbeans.lightweightinventorysorting.LightweightInventorySortingClient;
import borknbeans.lightweightinventorysorting.config.LightweightInventorySortingConfig;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(InventoryScreen.class)
public abstract class MixinInventoryScreen extends HandledScreen<PlayerScreenHandler> {

    @Unique
    private ContainerSortButton inventorySortButton;


    public MixinInventoryScreen(PlayerScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
    }

    @Inject(method = "init", at = @At("RETURN"))
    private void onInit(CallbackInfo ci) {
        int size = LightweightInventorySortingConfig.buttonSize.getButtonSize();

        inventorySortButton = new ContainerSortButton(0, 0, size, size, Text.literal("S"), 9, 35, this);

        setButtonCoordinates();

        // Add button to the screen
        this.addDrawableChild(inventorySortButton);
    }

    @Inject(method = "render", at = @At("RETURN"))
    private void onRender(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        if (inventorySortButton != null) {
            setButtonCoordinates();

            inventorySortButton.render(context, mouseX, mouseY, delta);
        }
    }

    @Inject(method="keyPressed", at=@At("RETURN"))
    public void keyPressed(int keyCode, int scanCode, int modifiers, CallbackInfoReturnable<Boolean> cir) {
        if (LightweightInventorySortingClient.sortKeyBind.matchesKey(keyCode, scanCode)) {
            inventorySortButton.onClick(0f, 0f); // Simulate a click
        }
    }

    private void setButtonCoordinates() {
        inventorySortButton.setX(this.x + this.backgroundWidth - 20 + LightweightInventorySortingConfig.xOffsetInventory);
        inventorySortButton.setY(this.height / 2 - 15 + LightweightInventorySortingConfig.yOffsetInventory);
    }
}
