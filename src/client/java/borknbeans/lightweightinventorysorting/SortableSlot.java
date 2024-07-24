package borknbeans.lightweightinventorysorting;

import borknbeans.lightweightinventorysorting.config.LightweightInventorySortingConfig;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Comparator;

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

    public ItemStack getStack() {
        return stack;
    }

    @Override
    public int compareTo(@NotNull SortableSlot o) {
        return LightweightInventorySortingConfig.sortType.compare(this.getStack(), o.getStack());
    }
}
