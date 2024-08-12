package borknbeans.lightweightinventorysorting.sorting;

import borknbeans.lightweightinventorysorting.LightweightInventorySorting;
import borknbeans.lightweightinventorysorting.config.LightweightInventorySortingConfig;
import net.minecraft.client.MinecraftClient;
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

        String insightLog = "Starting sort...";
        for (int i = startIndex; i <= endIndex; i++) {
            ItemStack stack = slots.get(i).getStack();
            if (stack.isEmpty()) { continue; }

            insightLog += "\n" + i + ": " + stack.getName().getString() + ", " + stack.getCount() + "/" + stack.getMaxCount() + ", " + stack.getItem().getName().getString();
            sortableSlots.add(new SortableSlot(i, stack));
        }
        LightweightInventorySorting.LOGGER.info(insightLog);

        sortableSlots.sort(new SortableSlotComparator());

        int syncId = client.player.currentScreenHandler.syncId;


        new Thread(() -> {
            combineLikeItems(client, syncId, slots, sortableSlots, startIndex, endIndex);

            sortableSlots.sort(new SortableSlotComparator());

            sortItems(client, syncId, slots, sortableSlots, startIndex);
        }).start();
    }

    /***
     * This goes through the sorted list and combines like items and reduces the size of the sorted list.
     *
     * @param client
     * @param syncId
     * @param sortedSlots
     */
    private static void combineLikeItems(MinecraftClient client, int syncId, DefaultedList<Slot> slots, List<SortableSlot> sortedSlots, int startIndex, int endIndex) {
        for (int i = sortedSlots.size() - 1; i >= 1; i--) {
            ItemStack stack = sortedSlots.get(i).getStack();

            if (stack.getCount() == stack.getMaxCount()) { continue; }

            int index = i - 1;

            HandHelper hand = new HandHelper();
            for (int j = index; j >= 0; j--) {
                ItemStack stackPrev = sortedSlots.get(j).getStack();

                // If we are holding something and the prev does not match OR if our hand is empty and the two checked stacks don't match
                // THEN don't combine
                if ((hand.stack != null && ItemStack.areItemsAndComponentsEqual(stackPrev, hand.stack) || (!ItemStack.areItemsAndComponentsEqual(stack, stackPrev) && !hand.exists))) {
                    if (hand.exists) { // Place item in hand back down
                        move(client, syncId, 0, sortedSlots.get(i).getIndex(), hand);
                        hand.Reset();
                    }

                    break;
                }
                if (stackPrev.getCount() == stackPrev.getMaxCount()) { continue; }

                int combinedCount = hand.exists ? hand.count + stackPrev.getCount() : stack.getCount() + stackPrev.getCount();

                if (combinedCount <= stackPrev.getMaxCount()) {
                    // Move with no remainder
                    move(client, syncId, sortedSlots.get(i).getIndex(), sortedSlots.get(j).getIndex(), hand);
                    // remove item from sortedSlots
                    sortedSlots.remove(i);
                    break;
                } else {
                    // Move with remainder
                    move(client, syncId, sortedSlots.get(i).getIndex(), sortedSlots.get(j).getIndex(), hand);
                    // Store hand item information
                    hand.exists = true;

                    ItemStack stackCopy = stackPrev.copy();
                    stackCopy.setCount(combinedCount - stackPrev.getMaxCount());

                    hand.stack = stackCopy;
                    hand.count = stackCopy.getCount(); // TODO: We can remove this count and now just use the stack count as it should be accurate
                }
            }

            if (hand.exists) {
                int emptySlot = -1;

                for (int j = startIndex; j < endIndex; j++) {
                    if (slots.get(j).getStack().isEmpty()) {
                        emptySlot = j;
                        break;
                    }
                }

                if (emptySlot != -1) {
                    move(client, syncId, 0, emptySlot, hand);
                    if (sortedSlots.get(i).getIndex() != emptySlot) {
                        sortedSlots.get(i).setIndex(emptySlot); // placing hand item here
                        hand.stack.setCount(hand.count);
                        sortedSlots.get(i).setStack(hand.stack.copy());
                    }
                    hand.Reset();
                } else {
                    System.out.println("Something went wrong combining items");
                }
            }

            try {
                Thread.sleep(LightweightInventorySortingConfig.sortDelay);
            } catch (InterruptedException e) {}
        }
    }

    private static void move(MinecraftClient client, int syncId, int source, int dest, HandHelper hand) {
        if (!hand.exists) {
            client.interactionManager.clickSlot(syncId, source, 0, SlotActionType.PICKUP, client.player);
        }

        client.interactionManager.clickSlot(syncId, dest, 0, SlotActionType.PICKUP, client.player);
    }

    private static void sortItems(MinecraftClient client, int syncId, DefaultedList<Slot> slots, List<SortableSlot> sortedSlots, int startIndex) {
        HandHelper hand = new HandHelper();

        for (int i = 0; i < sortedSlots.size(); i++) {
            if (sortedSlots.get(i).sorted) { continue; }

            sortItem(client, syncId, slots, sortedSlots, i, startIndex, hand);
        }
    }

    private static void sortItem(MinecraftClient client, int syncId, DefaultedList<Slot> slots, List<SortableSlot> sortedSlots, int index, int startIndex, HandHelper hand) {
        try {
            Thread.sleep(LightweightInventorySortingConfig.sortDelay);
        } catch (InterruptedException e) {}

        int dest = startIndex + index;

        if (dest == sortedSlots.get(index).getIndex()) {
            sortedSlots.get(index).sorted = true;
            return;
        }

        ItemStack destStack = slots.get(dest).getStack();

        move(client, syncId, sortedSlots.get(index).getIndex(), dest, hand);
        sortedSlots.get(index).sorted = true;

        if (!destStack.isEmpty()) {
            hand.exists = true;
            hand.stack = destStack;
            hand.count = destStack.getCount();

            // Get item in slot dest
            int sortedSlotListIndex = -1;
            for (int i = 0; i < sortedSlots.size(); i++) {
                if (sortedSlots.get(i).getIndex() == dest) {
                    sortedSlotListIndex = i;
                    break;
                }
            }

            if (sortedSlotListIndex == -1) {
                System.out.println("Something went wrong with sorting the items.");
                return;
            }

            sortItem(client, syncId, slots, sortedSlots, sortedSlotListIndex, startIndex, hand);
        } else {
            hand.Reset();
        }
    }
}
