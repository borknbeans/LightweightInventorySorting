package borknbeans.lightweightinventorysorting.mixin.client;

import borknbeans.lightweightinventorysorting.ContainerSortButton;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.client.gui.screen.recipebook.RecipeBookWidget;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InventoryScreen.class)
public abstract class MixinInventoryScreen extends HandledScreen<PlayerScreenHandler> {
    @Shadow @Final private RecipeBookWidget recipeBook;
    @Unique
    private ContainerSortButton inventorySortButton;

    public MixinInventoryScreen(PlayerScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
    }

    @Inject(method = "init", at = @At("RETURN"))
    private void onInit(CallbackInfo ci) {
        int width = 12;
        int height = 12;

        inventorySortButton = new ContainerSortButton(0, 0, width, height, Text.literal("S"), 9, 35, this);

        // Add button to the screen
        this.addDrawableChild(inventorySortButton);
    }

    @Inject(method = "render", at = @At("RETURN"))
    private void onRender(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        if (inventorySortButton != null) {
            inventorySortButton.setX(this.x + this.backgroundWidth - 20);
            inventorySortButton.setY(this.height / 2 - 15);

            inventorySortButton.render(context, mouseX, mouseY, delta);
        }
    }
}
