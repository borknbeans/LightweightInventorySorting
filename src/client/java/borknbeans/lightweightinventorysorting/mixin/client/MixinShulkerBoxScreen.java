package borknbeans.lightweightinventorysorting.mixin.client;

import borknbeans.lightweightinventorysorting.ContainerSortButton;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.screen.ingame.ShulkerBoxScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.ShulkerBoxScreenHandler;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ShulkerBoxScreen.class)
public abstract class MixinShulkerBoxScreen extends HandledScreen<ShulkerBoxScreenHandler> {
    @Unique
    private ContainerSortButton containerSortButton;

    public MixinShulkerBoxScreen(ShulkerBoxScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
    }

    @Override
    public void init() {
        super.init();

        // Initialize button
        int x = this.x + this.backgroundWidth - 20;
        int y = this.y + 4;
        int width = 12;
        int height = 12;
        containerSortButton = new ContainerSortButton(x, y, width, height, Text.literal("S"), 0, getScreenHandler().slots.size() - 37, this);

        // Add button to the screen
        this.addDrawableChild(containerSortButton);
    }

    @Inject(method = "render", at = @At("RETURN"))
    private void onRender(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        if (containerSortButton != null) {
            containerSortButton.render(context, mouseX, mouseY, delta);
        }
    }
}
