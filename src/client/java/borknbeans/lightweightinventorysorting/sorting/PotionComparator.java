package borknbeans.lightweightinventorysorting.sorting;

import net.minecraft.core.component.DataComponents;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.PotionContents;

import java.util.Iterator;

/**
 * Secondary comparison for potion-like stacks (potions, splash/lingering potions, tipped arrows).
 *
 * <p>All of these share a single base item and the same name/creative-index/raw-id, so the regular
 * {@link borknbeans.lightweightinventorysorting.config.SortType} comparators treat every variant as
 * equal. This comparator breaks that tie by the potion's contents, ordering by:
 * <ol>
 *     <li>primary effect name (A-Z)</li>
 *     <li>amplifier / strength (low to high, e.g. Strength I before Strength II)</li>
 *     <li>duration (short to long)</li>
 * </ol>
 *
 * <p>The ordering is intentionally independent of {@code Config.reverseSort} so potion sub-ordering
 * stays stable regardless of the primary sort direction (matching how the count tie-break behaves).
 * To flip a field's direction, negate the corresponding {@link Integer#compare} / string compare.
 */
public final class PotionComparator {

    private PotionComparator() {
    }

    /** A stack is "potion-like" if it carries potion contents (potions, tipped arrows, etc.). */
    public static boolean isPotion(ItemStack stack) {
        return stack.has(DataComponents.POTION_CONTENTS);
    }

    /**
     * Compares two potion-like stacks. Returns 0 if either stack is not potion-like (the caller
     * should fall back to its normal tie-breaking in that case).
     */
    public static int compare(ItemStack left, ItemStack right) {
        if (!isPotion(left) || !isPotion(right)) {
            return 0;
        }

        MobEffectInstance leftEffect = primaryEffect(left);
        MobEffectInstance rightEffect = primaryEffect(right);

        // Effect-less potions (water, mundane, awkward, thick) group together, ahead of real potions.
        if (leftEffect == null && rightEffect == null) {
            return 0;
        }
        if (leftEffect == null) {
            return -1;
        }
        if (rightEffect == null) {
            return 1;
        }

        int byName = effectName(leftEffect).compareToIgnoreCase(effectName(rightEffect));
        if (byName != 0) {
            return byName;
        }

        int byStrength = Integer.compare(leftEffect.getAmplifier(), rightEffect.getAmplifier());
        if (byStrength != 0) {
            return byStrength;
        }

        return Integer.compare(leftEffect.getDuration(), rightEffect.getDuration());
    }

    /** The first effect of a potion (base potion effects come before custom effects), or null. */
    private static MobEffectInstance primaryEffect(ItemStack stack) {
        PotionContents contents = stack.get(DataComponents.POTION_CONTENTS);
        if (contents == null) {
            return null;
        }
        Iterator<MobEffectInstance> effects = contents.getAllEffects().iterator();
        return effects.hasNext() ? effects.next() : null;
    }

    private static String effectName(MobEffectInstance effect) {
        return effect.getEffect().value().getDisplayName().getString();
    }
}
