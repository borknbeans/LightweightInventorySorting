package borknbeans.lightweightinventorysorting.sorting;

import net.minecraft.client.Minecraft;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.List;

public class ClickOperation {
    private final Minecraft client;
    private final int syncId;
    private final int targetSlot;
    private final ItemStack expectedStartingTargetStack;
    private final ItemStack expectedEndingTargetStack;
    private final ItemStack expectedStartingMouseStack;
    private final ItemStack expectedEndingMouseStack;

    private final List<Integer> delays = List.of(0, 5, 15); // in milliseconds

    // Custom exception to signal that a click was intentionally skipped due to unsafe conditions
    public static class SkippedUnsafeClickException extends Exception {
        public final int slot;
        public SkippedUnsafeClickException(int slot) {
            super("Skipped unsafe click on slot " + slot);
            this.slot = slot;
        }
    }

    public ClickOperation(Minecraft client, int syncId, int targetSlot, ItemStack expectedStartingTargetStack, ItemStack expectedEndingTargetStack, ItemStack expectedStartingMouseStack, ItemStack expectedEndingMouseStack) {
        this.client = client;
        this.syncId = syncId;
        this.targetSlot = targetSlot;
        this.expectedStartingTargetStack = expectedStartingTargetStack;
        this.expectedEndingTargetStack = expectedEndingTargetStack;
        this.expectedStartingMouseStack = expectedStartingMouseStack;
        this.expectedEndingMouseStack = expectedEndingMouseStack;
    }

    public void execute() throws Exception {
        if (client.player == null) {
            throw new Exception("Player is null");
        }

        ItemStack startingMouseStack = Sorter.getMouseStack(client);
        if (!ItemStack.isSameItemSameComponents(startingMouseStack, expectedStartingMouseStack)) {
            throw new Exception("[Target: " + targetSlot + "] Starting mouse stack is not what we expected: (ACTUAL)" + getItemStackString(startingMouseStack) + " != (EXPECTED)" + getItemStackString(expectedStartingMouseStack));
        }

        ItemStack startingTargetStack = Sorter.getInventoryStack(client, targetSlot);
        if (!ItemStack.isSameItemSameComponents(startingTargetStack, expectedStartingTargetStack)) {
            throw new Exception("[Target: " + targetSlot + "] Starting target stack is not what we expected: (ACTUAL)" + getItemStackString(startingTargetStack) + " != (EXPECTED)" + getItemStackString(expectedStartingTargetStack));
        }

        // Safety guard: Check if this click is safe
        if (!canSafelyClick(client, startingMouseStack)) {
            throw new SkippedUnsafeClickException(targetSlot);
        }

        click();

        Exception error = null;
        // Backoff retry - 5, 10, 15ms delay between each retry
        for (int i = 0; i < delays.size(); i++) {
            try {
                Thread.sleep(delays.get(i));
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            try {
                postClickVerification();
                return;
            } catch (Exception e) {
                error = e;
            }
        }

        if (error != null) {
            throw error;
        }
    }

    private void click() {
        if (client.player == null) {
            return;
        }
        client.gameMode.handleContainerInput(syncId, targetSlot, 0, ContainerInput.PICKUP, client.player);
    }

    private void postClickVerification() throws Exception {
        ItemStack endingMouseStack = Sorter.getMouseStack(client);
        if (!ItemStack.isSameItemSameComponents(endingMouseStack, expectedEndingMouseStack)) {
            throw new Exception("[Target: " + targetSlot + "] Ending mouse stack is not what we expected: (ACTUAL)" + getItemStackString(endingMouseStack) + " != (EXPECTED)" + getItemStackString(expectedEndingMouseStack));
        }

        ItemStack targetStack = Sorter.getInventoryStack(client, targetSlot);
        if (!ItemStack.isSameItemSameComponents(targetStack, expectedEndingTargetStack)) {
            throw new Exception("[Target: " + targetSlot + "] Ending target stack is not what we expected: (ACTUAL)" + getItemStackString(targetStack) + " != (EXPECTED)" + getItemStackString(expectedEndingTargetStack));
        }
    }

    private String getItemStackString(ItemStack stack) {
        return String.format("%dx %s", stack.getCount(), stack.getItem().getName(stack).getString());
    }

    // Safety check: Verify slot is valid and can accept the item in hand
    private boolean canSafelyClick(Minecraft client, ItemStack mouseStack) {
        if (client.player == null) return false;

        var handler = client.player.containerMenu;
        if (targetSlot < 0 || targetSlot >= handler.slots.size()) return false;

        var slot = handler.getSlot(targetSlot);
        if (slot == null || !slot.isActive()) return false;

        // Equipment / non-insertable slots will often reject generic items
        // Check both directions: whether we can take items FROM this slot OR insert INTO it
        if (!slot.mayPickup(client.player) && !slot.hasItem()) return false;

        // If holding something, make sure this slot can accept it
        if (!mouseStack.isEmpty() && !slot.mayPlace(mouseStack)) return false;

        // If this slot has special restrictions (e.g. saddle/armor), skip it
        if (slot.getMaxStackSize() <= 0) return false;

        return true;
    }
}
