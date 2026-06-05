package borknbeans.lightweightinventorysorting.config;

import borknbeans.lightweightinventorysorting.LightweightInventorySortingClient;
import borknbeans.lightweightinventorysorting.sorting.PotionComparator;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.Comparator;

public enum SortType {
    INDEX(Comparator.comparingInt(LightweightInventorySortingClient::getCreativeIndex)),
    ALPHANUMERIC(Comparator.comparing(stack -> stack.getItemName().getString())),
    RAW_ID(Comparator.comparingInt(stack -> Item.getId(stack.getItem())));

    public final Comparator<ItemStack> comparator;

    SortType(Comparator<ItemStack> comparator) {
        this.comparator = comparator;
    }

    public int compare(ItemStack left, ItemStack right) {
        // Check for empty slots
        if (left.isEmpty() && !right.isEmpty()) {
            return 1;
        } else if (right.isEmpty() && !left.isEmpty()) {
            return -1;
        }

        int result = this.comparator.compare(left, right);
        result = Config.reverseSort ? -result : result;
        if (result != 0) {
            return result;
        }

        // Same base item (e.g. all potions share one item): sub-sort potion variants by their
        // effect/strength/duration, then fall back to stack count.
        int byPotion = PotionComparator.compare(left, right);
        if (byPotion != 0) {
            return byPotion;
        }

        return right.getCount() - left.getCount();
    }
}
