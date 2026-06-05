package borknbeans.lightweightinventorysorting.mixin.client;

import borknbeans.lightweightinventorysorting.LightweightInventorySortingClient;
import borknbeans.lightweightinventorysorting.config.Config;
import borknbeans.lightweightinventorysorting.sorting.SortButton;
import borknbeans.lightweightinventorysorting.sorting.Sorter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.ContainerScreen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ChestMenu;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(ContainerScreen.class)
public abstract class GenericContainerScreenMixin extends AbstractContainerScreen<ChestMenu> {

    @Unique
    private SortButton sortButton;

    public GenericContainerScreenMixin(ChestMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
    }

    @Override
    protected void init() {
        super.init();

        // Initialize button
        int x = this.leftPos + this.imageWidth - 20 + Config.xOffsetContainer;
        int y = this.topPos + 4 + Config.yOffsetContainer;
        int size = Config.buttonSize.getButtonSize();
        sortButton = new SortButton(x, y, size, size, Component.literal("S"), 0, getMenu().slots.size() - 37);

        // Add button to the screen
        this.addRenderableWidget(sortButton);
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        if (LightweightInventorySortingClient.sortKeyBind.matches(event)) {
            if (this.sortButton != null) {
                Sorter.sortContainerClientside(Minecraft.getInstance(), sortButton.getSortStartIndex(), sortButton.getSortEndIndex());
            }
            return true;
        }
        return super.keyPressed(event);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        if (LightweightInventorySortingClient.sortKeyBind.matchesMouse(event)) {
            if (this.sortButton != null) {
                Sorter.sortContainerClientside(Minecraft.getInstance(), sortButton.getSortStartIndex(), sortButton.getSortEndIndex());
            }
            return true;
        }
        return super.mouseClicked(event, doubleClick);
    }
}
