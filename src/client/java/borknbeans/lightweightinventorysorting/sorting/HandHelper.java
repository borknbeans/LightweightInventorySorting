package borknbeans.lightweightinventorysorting.sorting;

import net.minecraft.item.ItemStack;

public class HandHelper {
    public boolean exists;
    public ItemStack stack;
    public int count;

    public HandHelper() {
        Reset();
    }

    public void Reset() {
        exists = false;
        stack = null;
        count = 0;
    }
}
