package borknbeans.lightweightinventorysorting;

import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LightweightInventorySorting implements ModInitializer {
    public static final String MOD_ID = "lightweight-inventory-sorting";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        LOGGER.info("Lightweight Inventory Sorting initialized on the server!");
    }
}
