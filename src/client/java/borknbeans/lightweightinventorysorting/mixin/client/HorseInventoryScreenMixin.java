package borknbeans.lightweightinventorysorting.mixin.client;

import borknbeans.lightweightinventorysorting.LightweightInventorySortingClient;
import borknbeans.lightweightinventorysorting.config.Config;
import borknbeans.lightweightinventorysorting.sorting.SortButton;
import borknbeans.lightweightinventorysorting.sorting.Sorter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.HorseInventoryScreen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.HorseInventoryMenu;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(HorseInventoryScreen.class)
public abstract class HorseInventoryScreenMixin extends AbstractContainerScreen<HorseInventoryMenu> {

    @Unique
    private SortButton sortButton;

    public HorseInventoryScreenMixin(HorseInventoryMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
    }

    @Override
    protected void init() {
        super.init();

        int x = this.leftPos + this.imageWidth - 20 + Config.xOffsetContainer;
        int y = this.topPos + 4 + Config.yOffsetContainer;
        int size = Config.buttonSize.getButtonSize();
        sortButton = new SortButton(x, y, size, size, Component.literal("S"), 0, getMenu().slots.size() - 37);
        this.addRenderableWidget(sortButton);
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
}
