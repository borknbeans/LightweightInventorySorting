package borknbeans.lightweightinventorysorting.config;

import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;

public enum SortTypes {
    ALPHANUMERIC,
    REVERSE_ALPHANUMERIC;

    public boolean compare(ItemStack left, ItemStack right) {
        switch (this) {
            case ALPHANUMERIC:
                return alphanumeric(left, right);
            case REVERSE_ALPHANUMERIC:
                return reverseAlphanumeric(left, right);
        }

        return false;
    }

    private boolean alphanumeric(ItemStack left, ItemStack right) {
        if (left.isEmpty() && !right.isEmpty()) {
            return true;
        } else if (right.isEmpty() && !left.isEmpty()) {
            return false;
        }

        return (left.getName().getString().compareTo(right.getName().getString())) > 0;
    }

    private boolean reverseAlphanumeric(ItemStack left, ItemStack right) {
        if (left.isEmpty() && !right.isEmpty()) {
            return true;
        } else if (right.isEmpty() && !left.isEmpty()) {
            return false;
        }

        return (left.getName().getString().compareTo(right.getName().getString())) < 0;
    }
}
