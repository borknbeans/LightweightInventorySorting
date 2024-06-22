package borknbeans.lightweightinventorysorting;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.DefaultedList;

public class ContainerSortButton extends ClickableWidget {

    private static final Identifier BUTTON_TEXTURE = Identifier.of(LightweightInventorySorting.MOD_ID,"sort_button");
    private static final Identifier BUTTON_HOVER_TEXTURE = Identifier.of(LightweightInventorySorting.MOD_ID,"sort_button_hover");

    private int startIndex, endIndex;
    private HandledScreen<?> screen;

    public ContainerSortButton(int x, int y, int width, int height, Text message, int startIndex, int endIndex, HandledScreen<?> screen) {
        super(x, y, width, height, message);

        this.startIndex = startIndex;
        this.endIndex = endIndex;

        this.screen = screen;
    }

    @Override
    protected void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
        // Draw the texture (fill with width and height of button)
        context.drawGuiTexture(BUTTON_TEXTURE, this.getX(), this.getY(), this.getWidth(), this.getHeight());

        // Optional: Draw hover effect or additional text
        if (this.isHovered()) {
            context.drawGuiTexture(BUTTON_HOVER_TEXTURE, this.getX(), this.getY(), this.getWidth(), this.getHeight());
        }
    }

    @Override
    protected void appendClickableNarrations(NarrationMessageBuilder builder) {
        // Narration message if needed
    }

    @Override
    public void onClick(double mouseX, double mouseY) {
        MinecraftClient client = MinecraftClient.getInstance();

        if (client.player != null) {
            sortInventory();
        } else {
            System.out.println("Player is not available.");
        }
    }

    private void sortInventory() {
        for (int i = startIndex; i <= endIndex; i++) {
            // Sort items into alphabetical order
            bubbleSort();
            // Combine like items
            collapseItems();
        }
    }

    private void bubbleSort() {
        MinecraftClient client = MinecraftClient.getInstance();
        int syncId = client.player.currentScreenHandler.syncId;

        DefaultedList<Slot> slots = screen.getScreenHandler().slots;
        for (int i = startIndex; i <= endIndex; i++) {
            for (int j = startIndex; j <= endIndex - 1; j++) {
                if (compare(slots.get(j).getStack(), slots.get(j + 1).getStack()) > 0) { // Swap items
                    client.interactionManager.clickSlot(syncId, j, 0, SlotActionType.PICKUP, client.player);
                    client.interactionManager.clickSlot(syncId, j + 1, 0, SlotActionType.PICKUP, client.player);
                    client.interactionManager.clickSlot(syncId, j, 0, SlotActionType.PICKUP, client.player);
                }
            }
        }
    }

    private int compare(ItemStack left, ItemStack right) {
        if (left.isEmpty() && !right.isEmpty()) {
            return 1;
        } else if (right.isEmpty() && !left.isEmpty()) {
            return -1;
        }

        return left.getName().getString().compareTo(right.getName().getString());
    }

    private void collapseItems() {
        MinecraftClient client = MinecraftClient.getInstance();
        int syncId = client.player.currentScreenHandler.syncId;

        DefaultedList<Slot> slots = screen.getScreenHandler().slots;
        for (int i = endIndex; i > startIndex; i--) {
            ItemStack right = slots.get(i).getStack();
            ItemStack left = slots.get(i - 1).getStack();

            if (right.isOf(left.getItem())) { // Same Type of item
                client.interactionManager.clickSlot(syncId, i, 0, SlotActionType.PICKUP, client.player);
                client.interactionManager.clickSlot(syncId, i - 1, 0, SlotActionType.PICKUP, client.player);
                client.interactionManager.clickSlot(syncId, i, 0, SlotActionType.PICKUP, client.player);
            }
        }
    }
}
