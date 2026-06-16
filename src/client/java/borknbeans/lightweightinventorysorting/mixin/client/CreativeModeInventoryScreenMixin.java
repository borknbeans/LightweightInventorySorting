package borknbeans.lightweightinventorysorting.mixin.client;

import borknbeans.lightweightinventorysorting.LightweightInventorySortingClient;
import borknbeans.lightweightinventorysorting.config.Config;
import borknbeans.lightweightinventorysorting.sorting.SortButton;
import borknbeans.lightweightinventorysorting.sorting.Sorter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.CreativeModeTab;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CreativeModeInventoryScreen.class)
public abstract class CreativeModeInventoryScreenMixin extends AbstractContainerScreen<AbstractContainerMenu> {

    @Unique
    private SortButton sortButton;

    public CreativeModeInventoryScreenMixin(AbstractContainerMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
    }

    @Inject(method = "init", at = @At("TAIL"))
    private void addSortButton(CallbackInfo ci) {
        // Only add sort button for the inventory tab
        updateSortButton();
    }

    @Inject(method = "selectTab", at = @At("TAIL"))
    private void onTabSelected(CallbackInfo ci) {
        // Update button visibility when tab changes
        updateSortButton();
    }

    @Unique
    private void updateSortButton() {
        if (isInventoryTab()) {
            if (sortButton == null) {
                int x = this.leftPos + this.imageWidth - 20 + Config.xOffsetContainer;
                int y = this.topPos + 4 + Config.yOffsetContainer;
                int size = Config.buttonSize.getButtonSize();
                // Creative grid is 3x9 (27 slots)
                // Slots 9-35: Creative grid (3 rows × 9 columns)
                // Avoids armor slots (5-8) and hotbar items (36-44)
                sortButton = new SortButton(x, y, size, size, Component.literal("S"), 9, 35);
                this.addRenderableWidget(sortButton);
            }
            // Show button
            if (sortButton != null) {
                sortButton.visible = true;
            }
        } else {
            // Hide button when not on inventory tab
            if (sortButton != null) {
                sortButton.visible = false;
            }
        }
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        // Only allow sorting when on inventory tab
        if (isInventoryTab() && LightweightInventorySortingClient.sortKeyBind.matches(event)) {
            if (sortButton != null) {
                Sorter.sortContainerClientside(Minecraft.getInstance(), sortButton.getSortStartIndex(), sortButton.getSortEndIndex());
            }
            return true;
        }
        return super.keyPressed(event);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        // Only allow sorting when on inventory tab
        if (isInventoryTab() && LightweightInventorySortingClient.sortKeyBind.matchesMouse(event)) {
            if (sortButton != null) {
                Sorter.sortContainerClientside(Minecraft.getInstance(), sortButton.getSortStartIndex(), sortButton.getSortEndIndex());
            }
            return true;
        }
        return super.mouseClicked(event, doubleClick);
    }

    @Unique
    private boolean isInventoryTab() {
        try {
            // Access the static selectedTab field via reflection
            var field = CreativeModeInventoryScreen.class.getDeclaredField("selectedTab");
            field.setAccessible(true);
            CreativeModeTab selectedTab = (CreativeModeTab) field.get(null);
            return selectedTab != null && selectedTab.getType() == CreativeModeTab.Type.INVENTORY;
        } catch (Exception e) {
            // Default to false if we can't access the field
            return false;
        }
    }
}
