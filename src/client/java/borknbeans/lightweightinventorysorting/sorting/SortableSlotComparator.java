package borknbeans.lightweightinventorysorting.sorting;

import java.util.Comparator;

public class SortableSlotComparator implements Comparator<SortableSlot> {

    @Override
    public int compare(SortableSlot o1, SortableSlot o2) {
        return o1.compareTo(o2);
    }
}