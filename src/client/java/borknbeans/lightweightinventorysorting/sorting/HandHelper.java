package borknbeans.lightweightinventorysorting.sorting;

import net.minecraft.item.ItemStack;

public class HandHelper {
    public boolean exists;
    public ItemStack stack;
    public int count;

    public HandHelper() {
        reset();
    }

    public void reset() {
        exists = false;
        stack = null;
        count = 0;
    }
}
