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

import java.util.ArrayList;
import java.util.List;

public class Sorter {

    private static volatile boolean isSorting = false;

    public static void sortContainerClientside(Minecraft client, int sortStartIndex, int sortEndIndex) {
        if (FabricLoader.getInstance().getEnvironmentType() != EnvType.CLIENT) {
            return;
        }

        if (isSorting) return;
        isSorting = true;

        LightweightInventorySorting.LOGGER.info("Starting clientside sort");

        AbstractContainerMenu container = client.player.containerMenu;
        int syncId = container.containerId;

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

    private static void clearMouseStack(Minecraft client, int syncId, int sortStartIndex, int sortEndIndex) throws Exception {
        List<ItemStack> snapshot = getInventorySnapshot(client, sortStartIndex, sortEndIndex);

        // Clear any existing item that is on the mouse
        ItemStack mouseStack = getMouseStack(client).copy();
        if (!mouseStack.isEmpty()) {
            int emptyIndex = getEmptySlotIndex(snapshot);
            if (emptyIndex == -1) {
                throw new Exception("[Sort] No empty slot found to clear mouse stack");
            }

            ClickOperation emptySlotOperation = new ClickOperation(client, syncId, sortStartIndex + emptyIndex, ItemStack.EMPTY, mouseStack, mouseStack, ItemStack.EMPTY);
            emptySlotOperation.execute();
        }
    }

    private static void combineLikeStacks(Minecraft client, int syncId, int sortStartIndex, int sortEndIndex) throws Exception {
        List<ItemStack> snapshot = getInventorySnapshot(client, sortStartIndex, sortEndIndex);
        ItemStack mouseStack = getMouseStack(client);

        if (!mouseStack.isEmpty()) {
            throw new Exception("[CombineLikeStacks] Mouse stack is not empty");
        }

        for (int i = 0; i < snapshot.size(); i++) {
            ItemStack stackOriginal = snapshot.get(i).copy();
            mouseStack = getMouseStack(client);
            if (stackOriginal.isEmpty() || stackOriginal.getCount() == stackOriginal.getMaxStackSize()) {
                continue;
            }

            for (int j = i + 1; j < snapshot.size(); j++) {
                ItemStack otherStack = snapshot.get(j).copy();
                if (otherStack.isEmpty() || otherStack.getCount() == otherStack.getMaxStackSize()) {
                    continue;
                }

                ItemStack stack = mouseStack.isEmpty() ? stackOriginal : mouseStack;

                if (ItemStack.isSameItemSameComponents(stack, otherStack)) {
                    int maxStackSize = stack.getMaxStackSize();
                    int combinedSize = stack.getCount() + otherStack.getCount();

                    ClickOperation pickupFirstStack = new ClickOperation(client, syncId, i + sortStartIndex, stack, ItemStack.EMPTY, mouseStack, stack);

                    ItemStack expectedEndingMouseStack = combinedSize > maxStackSize ? stack.copyWithCount(combinedSize - maxStackSize) : ItemStack.EMPTY;
                    ClickOperation combineStacks = new ClickOperation(client, syncId, j + sortStartIndex, otherStack, stack.copyWithCount(Math.min(combinedSize, maxStackSize)), stack, expectedEndingMouseStack);
                    try {
                        if (mouseStack.isEmpty()) { // Don't pick up first stack if we have a stack in our hand from the previous iteration
                            pickupFirstStack.execute();
                        }
                        Thread.sleep(Config.sortDelay);
                        combineStacks.execute();
                    } catch (Exception e) {
                        throw new Exception("Failed to combine like items: " + e.getMessage());
                    }

                    mouseStack = expectedEndingMouseStack;

                    if (mouseStack.isEmpty()) { // If our hand is empty, move to the next stack
                        break;
                    }
                }
            }

            if (!mouseStack.isEmpty()) {
                ClickOperation putBackStack = new ClickOperation(client, syncId, i + sortStartIndex, ItemStack.EMPTY, mouseStack, mouseStack, ItemStack.EMPTY);

                try {
                    putBackStack.execute();
                } catch (Exception e) {
                    throw new Exception("Failed to put back item: " + e.getMessage());
                }
            }

            snapshot = getInventorySnapshot(client, sortStartIndex, sortEndIndex); // Update the inventory to reflect our changes
        }
    }

    private static void sort(Minecraft client, int syncId, int sortStartIndex, int sortEndIndex) throws Exception {
        List<ItemStack> snapshot = getInventorySnapshot(client, sortStartIndex, sortEndIndex);

        ArrayList<ItemStack> sortedStacks = new ArrayList<ItemStack>();
        for (int i = 0; i < snapshot.size(); i++) {
            ItemStack stack = snapshot.get(i).copy();
            if (stack.isEmpty()) {
                continue;
            }

            sortedStacks.add(stack);
        }

        sortedStacks.sort(new SortComparator());

        ItemStack mouseStack = getMouseStack(client);

        if (!mouseStack.isEmpty()) {
            throw new Exception("[Sort] Mouse stack is not empty");
        }

        for (int i = 0; i < sortedStacks.size(); i++) {
            ItemStack sortedStack = sortedStacks.get(i);

            int stackCurrIndex = -1;
            for (int j = i; j < snapshot.size(); j++) {
                if (ItemStack.isSameItemSameComponents(sortedStack, snapshot.get(j)) && sortedStack.getCount() == snapshot.get(j).getCount()) {
                    stackCurrIndex = j + sortStartIndex;
                    break;
                }
            }

            if (stackCurrIndex == -1) {
                throw new Exception("[Sort] Stack not found in inventory, looking for: " + sortedStack.toString());
            }

            if (stackCurrIndex == i + sortStartIndex) {
                continue;
            }

            ClickOperation pickupOperation = new ClickOperation(client, syncId, stackCurrIndex, sortedStack, ItemStack.EMPTY, ItemStack.EMPTY, sortedStack);

            ItemStack existingStack = snapshot.get(i).copy();

            // If the item that is in our desired slot is a bundle, we need to handle it differently
            if (existingStack.getItem() instanceof BundleItem) {
                ClickOperation pickupBundleOperation = new ClickOperation(client, syncId, i + sortStartIndex, existingStack, ItemStack.EMPTY, ItemStack.EMPTY, existingStack);
                ClickOperation placeBundleElsewhereOperation = new ClickOperation(client, syncId, getEmptySlotIndex(snapshot) + sortStartIndex, ItemStack.EMPTY, existingStack, existingStack, ItemStack.EMPTY);

                pickupBundleOperation.execute();
                placeBundleElsewhereOperation.execute();

                existingStack = ItemStack.EMPTY;
            }

            ClickOperation placeOperation = new ClickOperation(client, syncId, i + sortStartIndex, existingStack, sortedStack, sortedStack, existingStack);

            ClickOperation emptyHandOperation = new ClickOperation(client, syncId, stackCurrIndex, ItemStack.EMPTY, existingStack, existingStack, ItemStack.EMPTY);

            // If the item we are sorting is a bundle, we need to handle it differently
            if (sortedStack.getItem() instanceof BundleItem) {
                if (!existingStack.isEmpty()) {
                    ClickOperation pickupTargetSlotOperation = new ClickOperation(client, syncId, i + sortStartIndex, existingStack, ItemStack.EMPTY, ItemStack.EMPTY, existingStack);
                    int emptySlotIndex = getEmptySlotIndex(snapshot);

                    if (emptySlotIndex == -1) {
                        throw new Exception("[Sort] No empty slot found");
                    }

                    ClickOperation placeInEmptySlotOperation = new ClickOperation(client, syncId, sortStartIndex + emptySlotIndex, ItemStack.EMPTY, existingStack, existingStack, ItemStack.EMPTY);

                    pickupTargetSlotOperation.execute();
                    placeInEmptySlotOperation.execute();
                    Thread.sleep(Config.sortDelay);

                    existingStack = ItemStack.EMPTY;

                    // Update place operation to expect the new empty stack
                    placeOperation = new ClickOperation(client, syncId, i + sortStartIndex, existingStack, sortedStack, sortedStack, existingStack);
                }
            }

            pickupOperation.execute();
            placeOperation.execute();
            Thread.sleep(Config.sortDelay);
            if (!existingStack.isEmpty()) {
                emptyHandOperation.execute();
            }

            snapshot = getInventorySnapshot(client, sortStartIndex, sortEndIndex);
        }

        for (int i = 0; i < sortedStacks.size(); i++) {
            ItemStack expectedStack = sortedStacks.get(i).copy();
            ItemStack actualStack = snapshot.get(i).copy();

            if (!ItemStack.isSameItemSameComponents(expectedStack, actualStack)) {
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

    private static int getEmptySlotIndex(List<ItemStack> snapshot) {
        for (int i = 0; i < snapshot.size(); i++) {
            if (snapshot.get(i).isEmpty()) {
                return i;
            }
        }
        return -1;
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
