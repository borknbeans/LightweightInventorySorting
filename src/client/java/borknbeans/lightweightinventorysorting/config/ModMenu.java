package borknbeans.lightweightinventorysorting.config;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
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
                .setTitle(Text.translatable("category.lightweight-inventory-sorting.title"));
        ConfigEntryBuilder entryBuilder = builder.entryBuilder();

        ConfigCategory generalSettings = builder.getOrCreateCategory(Text.translatable("category.lightweight-inventory-sorting.general"));

        generalSettings.addEntry(entryBuilder.startTextDescription(Text.translatable("category.lightweight-inventory-sorting.sort-options"))
                .build());

        generalSettings.addEntry(entryBuilder.startEnumSelector(
                        Text.translatable("category.lightweight-inventory-sorting.sort-type"),
                        SortTypes.class,
                        LightweightInventorySortingConfig.sortType
                ).setDefaultValue(SortTypes.ALPHANUMERIC)
                .setSaveConsumer(newValue -> LightweightInventorySortingConfig.sortType = newValue)
                .setTooltip(Text.translatable("category.lightweight-inventory-sorting.sort-type-tooltip"))
                .build());

        generalSettings.addEntry(entryBuilder.startIntField(
                        Text.translatable("category.lightweight-inventory-sorting.sort-delay"),
                        LightweightInventorySortingConfig.sortDelay
                ).setDefaultValue(0)
                .setSaveConsumer(newValue -> LightweightInventorySortingConfig.sortDelay = newValue)
                .setTooltip(Text.translatable("category.lightweight-inventory-sorting.sort-delay-tooltip"))
                .build());

        generalSettings.addEntry(entryBuilder.startTextDescription(Text.translatable("category.lightweight-inventory-sorting.button-options"))
                .build());

        generalSettings.addEntry(entryBuilder.startEnumSelector(
                        Text.translatable("category.lightweight-inventory-sorting.button-size"),
                        ButtonSize.class,
                        LightweightInventorySortingConfig.buttonSize
                ).setDefaultValue(ButtonSize.LARGE)
                .setSaveConsumer(newValue -> LightweightInventorySortingConfig.buttonSize = newValue)
                .build());

        generalSettings.addEntry(entryBuilder.startIntField(
                    Text.translatable("category.lightweight-inventory-sorting.inventory-x"),
                    LightweightInventorySortingConfig.xOffsetInventory
                ).setDefaultValue(0)
                .setSaveConsumer(newValue -> LightweightInventorySortingConfig.xOffsetInventory = newValue)
                .setTooltip(Text.translatable("category.lightweight-inventory-sorting.inventory-x-tooltip"))
                .build());

        generalSettings.addEntry(entryBuilder.startIntField(
                        Text.translatable("category.lightweight-inventory-sorting.inventory-y"),
                        LightweightInventorySortingConfig.yOffsetInventory
                ).setDefaultValue(0)
                .setSaveConsumer(newValue -> LightweightInventorySortingConfig.yOffsetInventory = newValue)
                .setTooltip(Text.translatable("category.lightweight-inventory-sorting.inventory-y-tooltip"))
                .build());

        generalSettings.addEntry(entryBuilder.startIntField(
                        Text.translatable("category.lightweight-inventory-sorting.container-x"),
                        LightweightInventorySortingConfig.xOffsetContainer
                ).setDefaultValue(0)
                .setSaveConsumer(newValue -> LightweightInventorySortingConfig.xOffsetContainer = newValue)
                .setTooltip(Text.translatable("category.lightweight-inventory-sorting.container-x-tooltip"))
                .build());

        generalSettings.addEntry(entryBuilder.startIntField(
                        Text.translatable("category.lightweight-inventory-sorting.container-y"),
                        LightweightInventorySortingConfig.yOffsetContainer
                ).setDefaultValue(0)
                .setSaveConsumer(newValue -> LightweightInventorySortingConfig.yOffsetContainer = newValue)
                .setTooltip(Text.translatable("category.lightweight-inventory-sorting.container-y-tooltip"))
                .build());

        builder.setSavingRunnable(() -> LightweightInventorySortingConfig.save());

        return builder.build();
    }
}
