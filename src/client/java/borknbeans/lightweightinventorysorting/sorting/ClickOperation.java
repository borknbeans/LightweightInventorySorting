package borknbeans.lightweightinventorysorting.sorting;

import java.util.List;

import net.minecraft.client.MinecraftClient;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.SlotActionType;

public class ClickOperation {
    private final MinecraftClient client;
    private final int syncId;
    private final int targetSlot;
    private final ItemStack expectedStartingTargetStack;
    private final ItemStack expectedEndingTargetStack;
    private final ItemStack expectedStartingMouseStack;
    private final ItemStack expectedEndingMouseStack;

    private final List<Integer> delays = List.of(0, 5, 15); // in milliseconds

    public ClickOperation(MinecraftClient client, int syncId, int targetSlot, ItemStack expectedStartingTargetStack, ItemStack expectedEndingTargetStack, ItemStack expectedStartingMouseStack, ItemStack expectedEndingMouseStack) {
        this.client = client;
        this.syncId = syncId;
        this.targetSlot = targetSlot;
        this.expectedStartingTargetStack = expectedStartingTargetStack;
        this.expectedEndingTargetStack = expectedEndingTargetStack;
        this.expectedStartingMouseStack = expectedStartingMouseStack;
        this.expectedEndingMouseStack = expectedEndingMouseStack;
    }

    // Custom exception to signal that a click was intentionally skipped
    public static class SkippedUnsafeClickException extends Exception {
        public final int slot;
        public SkippedUnsafeClickException(int slot) {
            super("Skipped unsafe click on slot " + slot);
            this.slot = slot;
        }
    }

    public void execute() throws Exception {
        if (client.player == null) {
            throw new Exception("Player is null");
        }

        ItemStack startingMouseStack = Sorter.getMouseStack(client);
        if (!ItemStack.areItemsAndComponentsEqual(startingMouseStack, expectedStartingMouseStack)) {
            throw new Exception("[Target: " + targetSlot + "] Starting mouse stack is not what we expected: (ACTUAL)" + getItemStackString(startingMouseStack) + " != (EXPECTED)" + getItemStackString(expectedStartingMouseStack));
        }

        ItemStack startingTargetStack = Sorter.getInventoryStack(client, targetSlot);
        if (!ItemStack.areItemsAndComponentsEqual(startingTargetStack, expectedStartingTargetStack)) {
            throw new Exception("[Target: " + targetSlot + "] Starting target stack is not what we expected: (ACTUAL)" + getItemStackString(startingTargetStack) + " != (EXPECTED)" + getItemStackString(expectedStartingTargetStack));
        }

        // Safety guard:
        if (!canSafelyClick(startingMouseStack)) {
            throw new SkippedUnsafeClickException(targetSlot);
        }


        click();

        Exception error = null;
        // Backoff retry - 5, 10, 15ms delay between each retry
        for (int i = 0; i < delays.size(); i++) {
            try {
                Thread.sleep(delays.get(i));
            } catch (InterruptedException e) {}
    
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

        client.interactionManager.clickSlot(syncId, targetSlot, 0, SlotActionType.PICKUP, client.player);
    }

    private void postClickVerification() throws Exception{
        var endingMouseStack = Sorter.getMouseStack(client);
        if (!ItemStack.areItemsAndComponentsEqual(endingMouseStack, expectedEndingMouseStack)) {
            throw new Exception("[Target: " + targetSlot + "] Ending mouse stack is not what we expected: (ACTUAL)" + getItemStackString(endingMouseStack) + " != (EXPECTED)" + getItemStackString(expectedEndingMouseStack));
        }

        var targetStack = Sorter.getInventoryStack(client, targetSlot);
        if (!ItemStack.areItemsAndComponentsEqual(targetStack, expectedEndingTargetStack)) {
            throw new Exception("[Target: " + targetSlot + "] Ending target stack is not what we expected: (ACTUAL)" + getItemStackString(targetStack) + " != (EXPECTED)" + getItemStackString(expectedEndingTargetStack));
        }
    }

    private String getItemStackString(ItemStack stack) {
        return String.format("%dx %s", stack.getCount(), stack.getItem().getName().getString());
    }

    private boolean canSafelyClick(ItemStack mouseStack) {
        if (client.player == null) return false;

        var handler = client.player.currentScreenHandler;
        if (targetSlot < 0 || targetSlot >= handler.slots.size()) return false;

        var slot = handler.getSlot(targetSlot);
        if (slot == null || !slot.isEnabled()) return false;

        // Equipment / non-insertable slots will often reject generic items
        // Check both directions: whether we can place *into* this slot OR pick up from it.
        if (!slot.canTakeItems(client.player) && !slot.hasStack()) return false;

        // If holding something, make sure this slot can accept it.
        if (!mouseStack.isEmpty() && !slot.canInsert(mouseStack)) return false;

        // If this slot has special restrictions (e.g. saddle/armor), skip it.
        if (slot.getMaxItemCount() <= 0) return false;

        return true;
    }

}
