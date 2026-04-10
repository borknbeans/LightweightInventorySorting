package borknbeans.lightweightinventorysorting.config;

import borknbeans.lightweightinventorysorting.LightweightInventorySorting;
import net.minecraft.resources.Identifier;

public enum ButtonSize {
    SMALL,
    MEDIUM,
    LARGE;

    public int getButtonSize() {
        return switch (this) {
            case SMALL -> 6;
            case MEDIUM -> 9;
            case LARGE -> 12;
        };
    }

    public Identifier getButtonTexture() {
        String fileName = switch (this) {
            case SMALL -> "sort_button_small";
            case MEDIUM -> "sort_button_medium";
            case LARGE -> "sort_button_large";
        };

        return Identifier.fromNamespaceAndPath(LightweightInventorySorting.MOD_ID, fileName);
    }

    public Identifier getButtonHoverTexture() {
        String fileName = switch (this) {
            case SMALL -> "sort_button_small_hover";
            case MEDIUM -> "sort_button_medium_hover";
            case LARGE -> "sort_button_large_hover";
        };

        return Identifier.fromNamespaceAndPath(LightweightInventorySorting.MOD_ID, fileName);
    }
}
