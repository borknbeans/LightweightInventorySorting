package borknbeans.lightweightinventorysorting.sorting;

import borknbeans.lightweightinventorysorting.LightweightInventorySorting;
import borknbeans.lightweightinventorysorting.config.Config;
import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.core.NonNullList;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.BundleItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.ArrayList;
import java.util.List;

public class Sorter {

    private static volatile boolean isSorting = false;

    // Returns a list of actual handler slot indices that are considered "sortable" / safe.
    // This skips saddle, armor, disabled, and other restricted slots.
    private static List<Integer> getSafeSlots(Minecraft client, int sortStartIndex, int sortEndIndex) {
        var handler = client.player.containerMenu;
        var slots = handler.slots;
        var safe = new ArrayList<Integer>();

        for (int slotIndex = sortStartIndex; slotIndex <= sortEndIndex && slotIndex < slots.size(); slotIndex++) {
            Slot slot = slots.get(slotIndex);
            if (slot == null) continue;
            if (!slot.isActive()) continue;

            // Skip slots that clearly aren't for normal inventory use
            // - Max stack size of 1 (typical for armor/saddle)
            // - Rejects a simple neutral item like dirt (means it's restricted)
            ItemStack probe = new ItemStack(Items.DIRT);
            boolean restricted = (slot.getMaxStackSize() == 1 && !slot.mayPlace(probe));

            if (restricted) continue; // saddle, armor, etc.

            // Skip disabled or read-only slots
            if (!slot.mayPickup(client.player) && !slot.hasItem()) continue;

            // If it's empty AND can't accept normal items, skip it
            if (!slot.hasItem() && !slot.mayPlace(probe)) continue;

            // Otherwise it's safe to use
            safe.add(slotIndex);
        }
        return safe;
    }

    // Parallel snapshot of stacks in those safe slots, in the same order.
    private static List<ItemStack> getSnapshotForSlots(Minecraft client, List<Integer> safeSlots) {
        var handler = client.player.containerMenu;
        var out = new ArrayList<ItemStack>(safeSlots.size());
        for (int idx : safeSlots) {
            out.add(handler.getSlot(idx).getItem().copy());
        }
        return out;
    }

    // Find the FIRST empty safe slot index (returns value is INDEX INTO safeSlots, not handler index)
    private static int getFirstEmptySafeSlotIndex(List<ItemStack> snapshotForSafeSlots) {
        for (int i = 0; i < snapshotForSafeSlots.size(); i++) {
            if (snapshotForSafeSlots.get(i).isEmpty()) {
                return i;
            }
        }
        return -1;
    }

    public static void sortContainerClientside(Minecraft client, int sortStartIndex, int sortEndIndex) {
        if (FabricLoader.getInstance().getEnvironmentType() != EnvType.CLIENT) {
            return;
        }

        if (isSorting) return;
        isSorting = true;

        LightweightInventorySorting.LOGGER.info("Starting clientside sort");

        AbstractContainerMenu container = client.player.containerMenu;
        int syncId = container.containerId;

        // Debug: Print all slots
        LightweightInventorySorting.LOGGER.info("=== ALL SLOTS BEFORE SORTING ===");
        for (int i = 0; i < container.slots.size(); i++) {
            Slot slot = container.slots.get(i);
            ItemStack item = slot.getItem();
            LightweightInventorySorting.LOGGER.info("Slot [" + i + "]: " + (item.isEmpty() ? "EMPTY" : item.getCount() + "x " + item.getItem().getName(item).getString()));
        }
        LightweightInventorySorting.LOGGER.info("Sort range: " + sortStartIndex + " to " + sortEndIndex);
        LightweightInventorySorting.LOGGER.info("=== END SLOT LIST ===");

        List<ItemStack> snapshot = getInventorySnapshot(client, sortStartIndex, sortEndIndex);
        SortSnapshotClientside snapshotEncoder = new SortSnapshotClientside(snapshot);
        LightweightInventorySorting.LOGGER.info("Encoded snapshot: " + snapshotEncoder.encode());

        // Run the sort in a new thread
        new Thread(() -> {
            try {
                clearMouseStack(client, syncId, sortStartIndex, sortEndIndex);
                combineLikeStacks(client, syncId, sortStartIndex, sortEndIndex);
                sort(client, syncId, sortStartIndex, sortEndIndex);
                LightweightInventorySorting.LOGGER.info("Clientside sort complete");
            } catch (Exception e) {
                LightweightInventorySorting.LOGGER.error(e.getMessage());
            }

            isSorting = false;
        }).start();
    }

    // Clear any existing item that is on the mouse cursor
    private static void clearMouseStack(Minecraft client, int syncId, int sortStartIndex, int sortEndIndex) throws Exception {
        var mouseStack = getMouseStack(client).copy();
        if (mouseStack.isEmpty()) return;

        var handler = client.player.containerMenu;
        var slots = handler.slots;

        // Build the list of valid destination slots
        var safeSlots = getSafeSlots(client, sortStartIndex, sortEndIndex);

        // Try to find a safe empty slot that can accept what's on the mouse
        int chosenSafeListIndex = -1;
        for (int k = 0; k < safeSlots.size(); k++) {
            int slotIdx = safeSlots.get(k);
            var slot = slots.get(slotIdx);
            if (!slot.isActive()) continue;
            if (!slot.getItem().isEmpty()) continue;
            if (!slot.mayPlace(mouseStack)) continue;
            if (slot.getMaxStackSize() <= 0) continue;

            chosenSafeListIndex = k;
            break;
        }

        if (chosenSafeListIndex == -1) {
            throw new Exception("[Sort] No safe empty slot found to clear mouse stack");
        }

        int handlerSlotIndex = safeSlots.get(chosenSafeListIndex);

        var op = new ClickOperation(
                client,
                syncId,
                handlerSlotIndex,
                ItemStack.EMPTY,
                mouseStack,
                mouseStack,
                ItemStack.EMPTY
        );

        try {
            op.execute();
        } catch (ClickOperation.SkippedUnsafeClickException e) {
            // Extremely defensive fallback. At this point we're already in "we tried".
            LightweightInventorySorting.LOGGER.warn("[clearMouseStack] Gave up clearing mouse; skipped slot {}", e.slot);
        }
    }

    // Combine like stacks together (works with safe slots only)
    private static void combineLikeStacks(Minecraft client, int syncId, int sortStartIndex, int sortEndIndex) throws Exception {
        var mouseStack = getMouseStack(client);
        if (!mouseStack.isEmpty()) {
            throw new Exception("[CombineLikeStacks] Mouse stack is not empty");
        }

        // We'll loop until we walk all safe slots
        var safeSlots = getSafeSlots(client, sortStartIndex, sortEndIndex);
        var snapshot = getSnapshotForSlots(client, safeSlots);

        for (int i = 0; i < snapshot.size(); i++) {
            var stackOriginal = snapshot.get(i).copy();
            mouseStack = getMouseStack(client);

            if (stackOriginal.isEmpty() || stackOriginal.getCount() == stackOriginal.getMaxStackSize()) {
                continue;
            }

            for (int j = i + 1; j < snapshot.size(); j++) {
                var otherStack = snapshot.get(j).copy();
                if (otherStack.isEmpty() || otherStack.getCount() == otherStack.getMaxStackSize()) {
                    continue;
                }

                var stack = mouseStack.isEmpty() ? stackOriginal : mouseStack;

                if (ItemStack.isSameItemSameComponents(stack, otherStack)) {
                    int maxStackSize = stack.getMaxStackSize();
                    int combinedSize = stack.getCount() + otherStack.getCount();

                    int handlerSlotI = safeSlots.get(i);
                    int handlerSlotJ = safeSlots.get(j);

                    var pickupFirstStack = new ClickOperation(
                            client,
                            syncId,
                            handlerSlotI,
                            stack,
                            ItemStack.EMPTY,
                            mouseStack,
                            stack
                    );

                    var expectedEndingMouseStack =
                            combinedSize > maxStackSize
                                    ? stack.copyWithCount(combinedSize - maxStackSize)
                                    : ItemStack.EMPTY;

                    var combineStacks = new ClickOperation(
                            client,
                            syncId,
                            handlerSlotJ,
                            otherStack,
                            stack.copyWithCount(Math.min(combinedSize, maxStackSize)),
                            stack,
                            expectedEndingMouseStack
                    );

                    try {
                        if (mouseStack.isEmpty()) {
                            pickupFirstStack.execute();
                        }
                        Thread.sleep(Config.sortDelay);
                        combineStacks.execute();
                    } catch (ClickOperation.SkippedUnsafeClickException e) {
                        LightweightInventorySorting.LOGGER.warn("[CombineLikeStacks] Skipped unsafe slot {} — clearing mouse + resync", e.slot);

                        try {
                            clearMouseStack(client, syncId, sortStartIndex, sortEndIndex);
                        } catch (Exception clearErr) {
                            LightweightInventorySorting.LOGGER.warn("[CombineLikeStacks] After skip, couldn't clear mouse: {}", clearErr.getMessage());
                        }

                        // refresh snapshot and continue main loop
                        safeSlots = getSafeSlots(client, sortStartIndex, sortEndIndex);
                        snapshot = getSnapshotForSlots(client, safeSlots);
                        continue;
                    } catch (Exception e) {
                        throw new Exception("Failed to combine like items: " + e.getMessage());
                    }

                    mouseStack = expectedEndingMouseStack;

                    if (mouseStack.isEmpty()) {
                        break; // done merging this base stack
                    }
                }
            }

            // try to put leftover mouse stack back into slot i
            mouseStack = getMouseStack(client);
            if (!mouseStack.isEmpty()) {
                int handlerSlotI = safeSlots.get(i);

                var putBackStack = new ClickOperation(
                        client,
                        syncId,
                        handlerSlotI,
                        ItemStack.EMPTY,
                        mouseStack,
                        mouseStack,
                        ItemStack.EMPTY
                );

                try {
                    putBackStack.execute();
                } catch (ClickOperation.SkippedUnsafeClickException e) {
                    LightweightInventorySorting.LOGGER.warn("[CombineLikeStacks] Put-back skipped on slot {} — clearing mouse + resync", e.slot);

                    try {
                        clearMouseStack(client, syncId, sortStartIndex, sortEndIndex);
                    } catch (Exception clearErr) {
                        LightweightInventorySorting.LOGGER.warn("[CombineLikeStacks] After put-back skip, couldn't clear mouse: {}", clearErr.getMessage());
                    }
                } catch (Exception e) {
                    throw new Exception("Failed to put back item: " + e.getMessage());
                }
            }

            // refresh snapshot after each outer iteration
            safeSlots = getSafeSlots(client, sortStartIndex, sortEndIndex);
            snapshot = getSnapshotForSlots(client, safeSlots);
        }
    }

    // Main sorting pass (works with safe slots only)
    private static void sort(Minecraft client, int syncId, int sortStartIndex, int sortEndIndex) throws Exception {
        // Build current view
        var safeSlots = getSafeSlots(client, sortStartIndex, sortEndIndex);
        var snapshot = getSnapshotForSlots(client, safeSlots);

        // Build list of all non-empty stacks from those safe slots
        var sortedStacks = new ArrayList<ItemStack>();
        for (ItemStack st : snapshot) {
            if (!st.isEmpty()) {
                sortedStacks.add(st.copy());
            }
        }

        // Sort them using the comparator
        sortedStacks.sort(new SortComparator());

        var mouseStack = getMouseStack(client);
        if (!mouseStack.isEmpty()) {
            throw new Exception("[Sort] Mouse stack is not empty");
        }

        // For each desired sorted position i...
        for (int i = 0; i < sortedStacks.size(); i++) {
            var sortedStack = sortedStacks.get(i);

            // Find where that exact stack currently lives (match item+components+count)
            int foundJ = -1;
            for (int j = i; j < snapshot.size(); j++) {
                ItemStack snapStack = snapshot.get(j);
                if (ItemStack.isSameItemSameComponents(sortedStack, snapStack)
                        && sortedStack.getCount() == snapStack.getCount()) {
                    foundJ = j;
                    break;
                }
            }

            if (foundJ == -1) {
                throw new Exception("[Sort] Stack not found in inventory, looking for: " + sortedStack.toString());
            }

            // If it's already where we want it, skip
            if (foundJ == i) {
                continue;
            }

            // Handler (real) slot indices for where it is and where it should go
            int fromHandlerSlot = safeSlots.get(foundJ);
            int toHandlerSlot   = safeSlots.get(i);

            var existingStack = snapshot.get(i).copy(); // what's currently sitting in target

            // Handle bundle blocking target slot
            if (existingStack.getItem() instanceof BundleItem) {
                // move that bundle out first
                int emptyIndexInSafe = getFirstEmptySafeSlotIndex(snapshot);
                if (emptyIndexInSafe == -1) {
                    throw new Exception("[Sort] No empty slot found to park bundle");
                }

                int targetHandlerSlot = toHandlerSlot;
                int emptyHandlerSlot  = safeSlots.get(emptyIndexInSafe);

                var pickupBundleOperation = new ClickOperation(
                        client,
                        syncId,
                        targetHandlerSlot,
                        existingStack,
                        ItemStack.EMPTY,
                        ItemStack.EMPTY,
                        existingStack
                );

                var placeBundleElsewhereOperation = new ClickOperation(
                        client,
                        syncId,
                        emptyHandlerSlot,
                        ItemStack.EMPTY,
                        existingStack,
                        existingStack,
                        ItemStack.EMPTY
                );

                try {
                    pickupBundleOperation.execute();
                    placeBundleElsewhereOperation.execute();
                } catch (ClickOperation.SkippedUnsafeClickException e) {
                    LightweightInventorySorting.LOGGER.warn("[Sort] Bundle relocation skipped on slot {} — clearing mouse + resync", e.slot);
                    try {
                        clearMouseStack(client, syncId, sortStartIndex, sortEndIndex);
                    } catch (Exception clearErr) {
                        LightweightInventorySorting.LOGGER.warn("[Sort] After bundle skip, couldn't clear mouse: {}", clearErr.getMessage());
                    }

                    // resync snapshot/safeSlots then continue outer loop
                    safeSlots = getSafeSlots(client, sortStartIndex, sortEndIndex);
                    snapshot = getSnapshotForSlots(client, safeSlots);
                    continue;
                }

                existingStack = ItemStack.EMPTY;
            }

            // Now build the normal operations
            var pickupOperation = new ClickOperation(
                    client,
                    syncId,
                    fromHandlerSlot,
                    sortedStack,
                    ItemStack.EMPTY,
                    ItemStack.EMPTY,
                    sortedStack
            );

            var placeOperation = new ClickOperation(
                    client,
                    syncId,
                    toHandlerSlot,
                    existingStack,
                    sortedStack,
                    sortedStack,
                    existingStack
            );

            var emptyHandOperation = new ClickOperation(
                    client,
                    syncId,
                    fromHandlerSlot,
                    ItemStack.EMPTY,
                    existingStack,
                    existingStack,
                    ItemStack.EMPTY
            );

            // Special handling if the item we are moving is a bundle and target not empty
            if (sortedStack.getItem() instanceof BundleItem && !existingStack.isEmpty()) {
                int emptyIndexInSafe = getFirstEmptySafeSlotIndex(snapshot);
                if (emptyIndexInSafe == -1) {
                    throw new Exception("[Sort] No empty slot found to park blocking stack before bundle");
                }

                int targetHandlerSlot = toHandlerSlot;
                int emptyHandlerSlot  = safeSlots.get(emptyIndexInSafe);

                var pickupTargetSlotOperation = new ClickOperation(
                        client,
                        syncId,
                        targetHandlerSlot,
                        existingStack,
                        ItemStack.EMPTY,
                        ItemStack.EMPTY,
                        existingStack
                );

                var placeInEmptySlotOperation = new ClickOperation(
                        client,
                        syncId,
                        emptyHandlerSlot,
                        ItemStack.EMPTY,
                        existingStack,
                        existingStack,
                        ItemStack.EMPTY
                );

                try {
                    pickupTargetSlotOperation.execute();
                    placeInEmptySlotOperation.execute();
                    Thread.sleep(Config.sortDelay);
                } catch (ClickOperation.SkippedUnsafeClickException e) {
                    LightweightInventorySorting.LOGGER.warn("[Sort] Bundle target clearing skipped on slot {} — clearing mouse + resync", e.slot);
                    try {
                        clearMouseStack(client, syncId, sortStartIndex, sortEndIndex);
                    } catch (Exception clearErr) {
                        LightweightInventorySorting.LOGGER.warn("[Sort] After bundle target skip, couldn't clear mouse: {}", clearErr.getMessage());
                    }

                    safeSlots = getSafeSlots(client, sortStartIndex, sortEndIndex);
                    snapshot = getSnapshotForSlots(client, safeSlots);
                    continue;
                }

                // After moving that blocking stack out, `existingStack` at target is now empty
                existingStack = ItemStack.EMPTY;
                // Recompute placeOperation with now-empty target
                placeOperation = new ClickOperation(
                        client,
                        syncId,
                        toHandlerSlot,
                        existingStack,
                        sortedStack,
                        sortedStack,
                        existingStack
                );
            }

            // Execute pickup/place/return-hand with safety+resync
            try {
                pickupOperation.execute();
            } catch (ClickOperation.SkippedUnsafeClickException e) {
                LightweightInventorySorting.LOGGER.warn("[Sort] Skipped unsafe slot {} on pickup — clearing mouse + resync", e.slot);
                try {
                    clearMouseStack(client, syncId, sortStartIndex, sortEndIndex);
                } catch (Exception clearErr) {
                    LightweightInventorySorting.LOGGER.warn("[Sort] After pickup skip, couldn't clear mouse: {}", clearErr.getMessage());
                }
                safeSlots = getSafeSlots(client, sortStartIndex, sortEndIndex);
                snapshot = getSnapshotForSlots(client, safeSlots);
                continue;
            }

            try {
                placeOperation.execute();
            } catch (ClickOperation.SkippedUnsafeClickException e) {
                LightweightInventorySorting.LOGGER.warn("[Sort] Skipped unsafe slot {} on place — clearing mouse + resync", e.slot);
                try {
                    clearMouseStack(client, syncId, sortStartIndex, sortEndIndex);
                } catch (Exception clearErr) {
                    LightweightInventorySorting.LOGGER.warn("[Sort] After place skip, couldn't clear mouse: {}", clearErr.getMessage());
                }
                safeSlots = getSafeSlots(client, sortStartIndex, sortEndIndex);
                snapshot = getSnapshotForSlots(client, safeSlots);
                continue;
            }

            Thread.sleep(Config.sortDelay);

            if (!existingStack.isEmpty()) {
                try {
                    emptyHandOperation.execute();
                } catch (ClickOperation.SkippedUnsafeClickException e) {
                    LightweightInventorySorting.LOGGER.warn("[Sort] Skipped unsafe slot {} on empty hand — clearing mouse + resync", e.slot);
                    try {
                        clearMouseStack(client, syncId, sortStartIndex, sortEndIndex);
                    } catch (Exception clearErr) {
                        LightweightInventorySorting.LOGGER.warn("[Sort] After empty-hand skip, couldn't clear mouse: {}", clearErr.getMessage());
                    }
                }
            }

            // resync after each outer loop iteration
            safeSlots = getSafeSlots(client, sortStartIndex, sortEndIndex);
            snapshot = getSnapshotForSlots(client, safeSlots);
        }

        // final validation: do we match the sorted order at the front?
        safeSlots = getSafeSlots(client, sortStartIndex, sortEndIndex);
        snapshot = getSnapshotForSlots(client, safeSlots);

        for (int i = 0; i < sortedStacks.size(); i++) {
            var expectedStack = sortedStacks.get(i).copy();
            var actualStack = snapshot.get(i).copy();

            if (!ItemStack.isSameItemSameComponents(expectedStack, actualStack)
                    || expectedStack.getCount() != actualStack.getCount()) {
                throw new Exception("[Sort] Stack not in correct position");
            }
        }
    }

    /*
     * Player Inventory Slots
     * 0 : crafting result
     * 1-4: crafting input
     * 5-8: armor
     * 9-35: main inventory
     * 36-44: hotbar
     * 45: offhand
     */
    private static List<ItemStack> getInventorySnapshot(Minecraft client, int sortStartIndex, int sortEndIndex) {
        NonNullList<Slot> slots = client.player.containerMenu.slots;

        List<ItemStack> snapshot = new ArrayList<>();
        for (int i = 0; i < slots.size(); i++) {
            if (i < sortStartIndex || i > sortEndIndex) {
                continue;
            }

            snapshot.add(slots.get(i).getItem());
        }

        return snapshot;
    }



    public static ItemStack getInventoryStack(Minecraft client, int index) {
        if (client.player == null) {
            return ItemStack.EMPTY;
        }

        return client.player.containerMenu.getSlot(index).getItem();
    }

    public static ItemStack getMouseStack(Minecraft client) {
        if (client.player == null) {
            return ItemStack.EMPTY;
        }

        return client.player.containerMenu.getCarried();
    }
}
