package borknbeans.lightweightinventorysorting.sorting;

import borknbeans.lightweightinventorysorting.config.Config;
import net.minecraft.world.item.ItemStack;

import java.util.Comparator;

public class SortComparator implements Comparator<ItemStack> {
    @Override
    public int compare(ItemStack o1, ItemStack o2) {
        return Config.sortType.compare(o1, o2);
    }
}
