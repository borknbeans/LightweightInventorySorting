package borknbeans.lightweightinventorysorting.sorting;

import borknbeans.lightweightinventorysorting.LightweightInventorySorting;
import borknbeans.lightweightinventorysorting.config.LightweightInventorySortingConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.BundleItem;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.collection.DefaultedList;

import java.util.ArrayList;
import java.util.List;

public class SortingHelper {

    public static void sortInventory(MinecraftClient client, int startIndex, int endIndex) {
        if (client.player == null) return;

        int syncId = client.player.currentScreenHandler.syncId;

        DefaultedList<Slot> slots = client.player.currentScreenHandler.slots;
        List<SortableSlot> sortableSlots = new ArrayList<>();

        LightweightInventorySorting.LOGGER.info("Collecting sort details...");
        for (int i = startIndex; i <= endIndex; i++) {
            ItemStack stack = slots.get(i).getStack();
            if (stack.isEmpty() || ignoredItem(stack)) { continue; }

            LightweightInventorySorting.LOGGER.info(i + ": " + stack.getName().getString() + ", " + stack.getCount() + "/" + stack.getMaxCount() + ", " + stack.getItem().getName().getString());
            sortableSlots.add(new SortableSlot(i, stack));
        }

        ItemStack mouseStack = stackAttachedToMouse(client);
        if (mouseStack != null) {
            LightweightInventorySorting.LOGGER.info("MOUSE: " + mouseStack.getName().getString() + ", " + mouseStack.getCount() + "/" + mouseStack.getMaxCount() + ", " + mouseStack.getItem().getName().getString());
            int index = findEmptySlotIndex(slots, startIndex, endIndex);

            if (index == -1) {
                LightweightInventorySorting.LOGGER.info("An error occurred while attempting to sort items in slots with an item already selected!");
                return;
            }

            move(client, syncId, -1, index, new HandHelper());
            sortableSlots.add(new SortableSlot(index, mouseStack));
        }

        LightweightInventorySorting.LOGGER.info("Sorting starting now...");

        sortableSlots.sort(SortableSlot::compareTo);

        new Thread(() -> {
            combineLikeItems(client, syncId, slots, sortableSlots, startIndex, endIndex);

            sortableSlots.sort(SortableSlot::compareTo);

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

            if (stack.getCount() == stack.getMaxCount() || ignoredItem(stack)) { continue; }

            int index = i - 1;

            HandHelper hand = new HandHelper();
            for (int j = index; j >= 0; j--) {
                ItemStack stackPrev = sortedSlots.get(j).getStack();

                // If we are holding something and the prev does not match OR if our hand is empty and the two checked stacks don't match OR the stack should get ignored
                // THEN don't combine
                if (hand.stack != null && !ItemStack.areItemsAndComponentsEqual(stackPrev, hand.stack) || !ItemStack.areItemsAndComponentsEqual(stack, stackPrev) && !hand.exists || ignoredItem(stackPrev)) {
                    if (hand.exists) { // Place item in hand back down
                        move(client, syncId, 0, sortedSlots.get(i).getIndex(), hand);
                        sortedSlots.get(i).setStack(hand.stack.copy());

                        hand.reset();
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

                    if (hand.exists) {
                        hand.reset();
                    }

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
                int emptySlot = findEmptySlotIndex(slots, startIndex, endIndex);

                if (emptySlot != -1) {
                    move(client, syncId, 0, emptySlot, hand);
                    if (sortedSlots.get(i).getIndex() != emptySlot) {
                        sortedSlots.get(i).setIndex(emptySlot); // placing hand item here
                        hand.stack.setCount(hand.count);
                        sortedSlots.get(i).setStack(hand.stack.copy());
                    }
                    hand.reset();
                } else {
                    LightweightInventorySorting.LOGGER.error("Something went wrong combining items");
                }
            }

            try {
                Thread.sleep(LightweightInventorySortingConfig.sortDelay);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
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

        List<Integer> ignoredIndices = slots.stream().filter(slot -> ignoredItem(slot.getStack())).map(slot -> slot.id).toList();

        for (int i = 0; i < sortedSlots.size(); i++) {
            SortableSlot slot = sortedSlots.get(i);
            if (slot.sorted || ignoredItem(slot.getStack())) { continue; }

            sortItem(client, syncId, slots, sortedSlots, i, startIndex, hand, ignoredIndices);
        }
    }

    private static void sortItem(MinecraftClient client, int syncId, DefaultedList<Slot> slots, List<SortableSlot> sortedSlots, int index, int startIndex, HandHelper hand, List<Integer> ignoredIndices) {
        try {
            Thread.sleep(LightweightInventorySortingConfig.sortDelay);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        int dest = getShiftedDest(startIndex + index, ignoredIndices);

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
                LightweightInventorySorting.LOGGER.error("Something went wrong with sorting the items.");
                return;
            }

            sortItem(client, syncId, slots, sortedSlots, sortedSlotListIndex, startIndex, hand, ignoredIndices);
        } else {
            hand.reset();
        }
    }

    /**
     * Returns a shifted destination, skipping ignored slots
     */
    private static int getShiftedDest(int dest, List<Integer> ignoredIndices) {
        int result = dest;
        for (int index : ignoredIndices) {
            if (index <= result) result++;
        }
        return result;
    }

    private static ItemStack stackAttachedToMouse(MinecraftClient client) {
        if (client.player == null) {
            return null;
        }

        ItemStack cursorStack = client.player.currentScreenHandler.getCursorStack();
        return cursorStack.isEmpty() ? null : cursorStack;
    }

    private static int findEmptySlotIndex(DefaultedList<Slot> slots, int startIndex, int endIndex) {
        for (int i = startIndex; i <= endIndex; i++) {
            ItemStack stack = slots.get(i).getStack();
            if (stack.isEmpty()) { return i; }
        }

        return -1;
    }

    private static boolean ignoredItem(ItemStack stack) {
        return stack.getItem() instanceof BundleItem;
    }
}
