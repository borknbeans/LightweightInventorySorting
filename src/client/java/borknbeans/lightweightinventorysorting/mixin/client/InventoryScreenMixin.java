package borknbeans.lightweightinventorysorting.mixin.client;

import borknbeans.lightweightinventorysorting.LightweightInventorySortingClient;
import borknbeans.lightweightinventorysorting.config.Config;
import borknbeans.lightweightinventorysorting.sorting.SortButton;
import borknbeans.lightweightinventorysorting.sorting.Sorter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.InventoryMenu;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InventoryScreen.class)
public abstract class InventoryScreenMixin extends AbstractContainerScreen<InventoryMenu> {

    @Unique
    private SortButton sortButton;

    public InventoryScreenMixin(InventoryMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
    }

    @Inject(method = "init", at = @At("RETURN"))
    private void onInit(CallbackInfo ci) {
        int size = Config.buttonSize.getButtonSize();
        sortButton = new SortButton(0, 0, size, size, Component.literal("S"), 9, 35);
        setButtonCoordinates();
        this.addRenderableWidget(sortButton);
    }

    // Toggling the recipe book shifts leftPos without re-running init(), so
    // reposition the sort button whenever the recipe book is opened/closed.
    @Inject(method = "onRecipeBookButtonClick", at = @At("RETURN"))
    private void onRecipeBookToggle(CallbackInfo ci) {
        if (sortButton != null) {
            setButtonCoordinates();
        }
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        if (LightweightInventorySortingClient.sortKeyBind.matches(event)) {
            if (sortButton != null) {
                Sorter.sortContainerClientside(Minecraft.getInstance(), sortButton.getSortStartIndex(), sortButton.getSortEndIndex());
            }
            return true;
        }
        return super.keyPressed(event);
    }

    private void setButtonCoordinates() {
        sortButton.setX(this.leftPos + this.imageWidth - 20 + Config.xOffsetInventory);
        sortButton.setY(this.height / 2 - 15 + Config.yOffsetInventory);
    }
}
