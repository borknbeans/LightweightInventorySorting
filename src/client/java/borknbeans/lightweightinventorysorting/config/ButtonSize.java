package borknbeans.lightweightinventorysorting.config;

import borknbeans.lightweightinventorysorting.LightweightInventorySorting;
import net.minecraft.util.Identifier;

public enum ButtonSize {
    SMALL,
    MEDIUM,
    LARGE;

    public int getButtonSize() {
        switch (this) {
            case SMALL:
                return 6;
            case MEDIUM:
                return 9;
            case LARGE:
                return 12;
        }

        return 0;
    }

    public Identifier getButtonTexture() {
        String fileName = "sort_button_large";
        switch (this) {
            case SMALL:
                fileName = "sort_button_small";
                break;
            case MEDIUM:
                fileName = "sort_button_medium";
                break;
            case LARGE:
                fileName = "sort_button_large";
                break;
        }

        return Identifier.of(LightweightInventorySorting.MOD_ID, fileName);
    }

    public Identifier getButtonHoverTexture() {
        String fileName = "sort_button_large_hover";
        switch (this) {
            case SMALL:
                fileName = "sort_button_small_hover";
                break;
            case MEDIUM:
                fileName = "sort_button_medium_hover";
                break;
            case LARGE:
                fileName = "sort_button_large_hover";
                break;
        }

        return Identifier.of(LightweightInventorySorting.MOD_ID, fileName);
    }

}
