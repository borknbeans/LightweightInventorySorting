package borknbeans.lightweightinventorysorting.sorting;

import borknbeans.lightweightinventorysorting.config.LightweightInventorySortingConfig;
import net.minecraft.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class SortableSlot implements Comparable<SortableSlot> {

    private int index;
    private ItemStack stack;

    public boolean sorted;

    public SortableSlot(int index, ItemStack stack) {
        this.index = index;
        this.stack = stack;
        sorted = false;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public ItemStack getStack() {
        return stack;
    }

    public void setStack(ItemStack stack) {
        this.stack = stack;
    }

    @Override
    public int compareTo(@NotNull SortableSlot o) {
        // Compare the names of the two stacks
        int result = LightweightInventorySortingConfig.sortType.compare(this.getStack(), o.getStack());

        if (result != 0) { // Items have different names
            return result;
        } else if (!ItemStack.areItemsAndComponentsEqual(this.getStack(), o.getStack())) { // Items have the same name, but different components or items
            return 0;
        } else { // Items have same name, are same item, and have same components
            return o.getStack().getCount() - this.getStack().getCount();
        }
    }
}
