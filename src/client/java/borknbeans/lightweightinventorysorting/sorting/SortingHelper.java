package borknbeans.lightweightinventorysorting.sorting;

import borknbeans.lightweightinventorysorting.SortableSlot;
import borknbeans.lightweightinventorysorting.SortableSlotComparator;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.collection.DefaultedList;

import java.util.ArrayList;
import java.util.List;

public class SortingHelper {

    public static void sortInventory(MinecraftClient client, int startIndex, int endIndex) {
        if (client.player == null) return;

        DefaultedList<Slot> slots = client.player.currentScreenHandler.slots;
        List<SortableSlot> sortableSlots = new ArrayList<>();

        for (int i = startIndex; i <= endIndex; i++) {
            ItemStack stack = slots.get(i).getStack();
            if (stack.isEmpty()) { continue; }

            sortableSlots.add(new SortableSlot(i, stack));
        }

        sortableSlots.sort(new SortableSlotComparator());

        int syncId = client.player.currentScreenHandler.syncId;

        // 1. Combine like items?
        // 2. Move into desired slot

        // OR

        // 1. Move into desired slot
        // 2. Combine like items

        combineLikeItems(client, syncId, sortableSlots);
    }

    /***
     * This goes through the sorted list and combines like items and reduces the size of the sorted list.
     *
     * @param client
     * @param syncId
     * @param sortedSlots
     */
    private static void combineLikeItems(MinecraftClient client, int syncId, List<SortableSlot> sortedSlots) {
        for (int i = sortedSlots.size() - 1; i >= 1; i--) {
            ItemStack stack = sortedSlots.get(i).getStack();

            if (stack.getCount() == stack.getMaxCount()) { continue; }

            int index = i - 1;
            boolean inHand = false;
            Item itemInHand = null;
            int handCount = 0;
            for (int j = index; j >= 0; j--) {
                ItemStack stackPrev = sortedSlots.get(j).getStack();

                if ((itemInHand != null && !stackPrev.isOf(itemInHand) || (!stack.isOf(stackPrev.getItem()) && !inHand))) {
                    if (inHand) {
                        move(client, syncId, 0, sortedSlots.get(i).getIndex(), inHand);
                        inHand = false;
                    }

                    break;
                }
                if (stackPrev.getCount() == stackPrev.getMaxCount()) { continue; }

                int combinedCount = inHand ? handCount + stackPrev.getCount() : stack.getCount() + stackPrev.getCount();

                if (combinedCount <= stackPrev.getMaxCount()) {
                    // Move with no remainder
                    move(client, syncId, sortedSlots.get(i).getIndex(), sortedSlots.get(j).getIndex(), inHand);
                    // remove item from sortedSlots
                    sortedSlots.remove(i);
                    break;
                } else {
                    // Move with remainder
                    move(client, syncId, sortedSlots.get(i).getIndex(), sortedSlots.get(j).getIndex(), inHand);
                    // Store hand item information
                    inHand = true;
                    itemInHand = stackPrev.getItem();
                    handCount = combinedCount - stackPrev.getMaxCount();
                }
            }
        }
    }

    private static void move(MinecraftClient client, int syncId, int source, int dest, boolean inHand) {
        if (!inHand) {
            client.interactionManager.clickSlot(syncId, source, 0, SlotActionType.PICKUP, client.player);
        }

        client.interactionManager.clickSlot(syncId, dest, 0, SlotActionType.PICKUP, client.player);
    }
}
