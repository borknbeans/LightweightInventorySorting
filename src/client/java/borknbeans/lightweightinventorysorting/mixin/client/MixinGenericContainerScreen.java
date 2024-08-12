package borknbeans.lightweightinventorysorting.mixin.client;

import borknbeans.lightweightinventorysorting.ContainerSortButton;
import borknbeans.lightweightinventorysorting.LightweightInventorySorting;
import borknbeans.lightweightinventorysorting.LightweightInventorySortingClient;
import borknbeans.lightweightinventorysorting.config.LightweightInventorySortingConfig;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.GenericContainerScreen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GenericContainerScreen.class)
public abstract class MixinGenericContainerScreen extends HandledScreen<GenericContainerScreenHandler>  {
    @Unique
    private ContainerSortButton containerSortButton;

    public MixinGenericContainerScreen(GenericContainerScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
    }

    @Override
    public void init() {
        super.init();

        // Initialize button
        int x = this.x + this.backgroundWidth - 20 + LightweightInventorySortingConfig.xOffsetContainer;
        int y = this.y + 4 + LightweightInventorySortingConfig.yOffsetContainer;
        int size = LightweightInventorySortingConfig.buttonSize.getButtonSize();
        containerSortButton = new ContainerSortButton(x, y, size, size, Text.literal("S"), 0, getScreenHandler().slots.size() - 37, this);

        // Add button to the screen
        this.addDrawableChild(containerSortButton);
    }

    @Inject(method = "render", at = @At("RETURN"))
    private void onRender(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        if (containerSortButton != null) {
            containerSortButton.render(context, mouseX, mouseY, delta);
        }
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (LightweightInventorySortingClient.sortKeyBind.matchesKey(keyCode, scanCode)) {
            containerSortButton.onClick(0f, 0f); // Simulate a click
        }

        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (LightweightInventorySortingClient.sortKeyBind.matchesMouse(button)) {
            containerSortButton.onClick(0f, 0f); // Simulate a click
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }
}
