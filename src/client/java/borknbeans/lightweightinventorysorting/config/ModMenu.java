package borknbeans.lightweightinventorysorting.config;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import me.shedaniel.clothconfig2.gui.entries.TextListEntry;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

public class ModMenu implements ModMenuApi {

    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return parent -> createConfigScreen(parent);
    }

    private Screen createConfigScreen(Screen parent) {
        ConfigBuilder builder = ConfigBuilder.create()
                .setParentScreen(parent)
                .setTitle(Text.of("Lightweight Inventory Sorting"));

        ConfigEntryBuilder entryBuilder = builder.entryBuilder();

        ConfigCategory generalSettings = builder.getOrCreateCategory(Text.of("General Settings"));

        generalSettings.addEntry(entryBuilder.startTextDescription(Text.of("Sorting Options"))
                .build());

        generalSettings.addEntry(entryBuilder.startEnumSelector(
                        Text.translatable("Sorting Type"),
                        SortTypes.class,
                        LightweightInventorySortingConfig.sortType
                ).setDefaultValue(SortTypes.ALPHANUMERIC)
                .setSaveConsumer(newValue -> LightweightInventorySortingConfig.sortType = newValue)
                .setTooltip(Text.translatable("Change the sorting method used:\nALPHANUMERIC\nREVERSE_ALPHANUMERIC"))
                .build());

        generalSettings.addEntry(entryBuilder.startIntField(
                        Text.translatable("Sorting Delay"),
                        LightweightInventorySortingConfig.sortDelay
                ).setDefaultValue(5)
                .setSaveConsumer(newValue -> LightweightInventorySortingConfig.sortDelay = newValue)
                .setTooltip(Text.translatable("Increase delay between sorting steps (Useful for multiplayer servers)"))
                .build());

        generalSettings.addEntry(entryBuilder.startTextDescription(Text.of("Button Options"))
                .build());

        generalSettings.addEntry(entryBuilder.startEnumSelector(
                        Text.translatable("Button Size"),
                        ButtonSize.class,
                        LightweightInventorySortingConfig.buttonSize
                ).setDefaultValue(ButtonSize.LARGE)
                .setSaveConsumer(newValue -> LightweightInventorySortingConfig.buttonSize = newValue)
                .setTooltip(Text.translatable("Change the size of the sort button:\nSMALL\nMEDIUM\nLARGE"))
                .build());

        generalSettings.addEntry(entryBuilder.startIntField(
                    Text.translatable("Inventory X Offset"),
                    LightweightInventorySortingConfig.xOffsetInventory
                ).setDefaultValue(0)
                .setSaveConsumer(newValue -> LightweightInventorySortingConfig.xOffsetInventory = newValue)
                .setTooltip(Text.translatable("Move the inventory sort button on the x-axis\nPOSITIVE: Right\nNEGATIVE: Left"))
                .build());

        generalSettings.addEntry(entryBuilder.startIntField(
                        Text.translatable("Inventory Y Offset"),
                        LightweightInventorySortingConfig.yOffsetInventory
                ).setDefaultValue(0)
                .setSaveConsumer(newValue -> LightweightInventorySortingConfig.yOffsetInventory = newValue)
                .setTooltip(Text.translatable("Move the inventory sort button on the y-axis\nPOSITIVE: Down\nNEGATIVE: Up"))
                .build());

        generalSettings.addEntry(entryBuilder.startIntField(
                        Text.translatable("Container X Offset"),
                        LightweightInventorySortingConfig.xOffsetContainer
                ).setDefaultValue(0)
                .setSaveConsumer(newValue -> LightweightInventorySortingConfig.xOffsetContainer = newValue)
                .setTooltip(Text.translatable("Move the container sort button on the x-axis\nPOSITIVE: Right\nNEGATIVE: Left"))
                .build());

        generalSettings.addEntry(entryBuilder.startIntField(
                        Text.translatable("Container Y Offset"),
                        LightweightInventorySortingConfig.yOffsetContainer
                ).setDefaultValue(0)
                .setSaveConsumer(newValue -> LightweightInventorySortingConfig.yOffsetContainer = newValue)
                .setTooltip(Text.translatable("Move the container sort button on the y-axis\nPOSITIVE: Down\nNEGATIVE: Up"))
                .build());

        builder.setSavingRunnable(() -> LightweightInventorySortingConfig.save());

        return builder.build();
    }
}
