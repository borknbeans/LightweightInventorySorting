package borknbeans.lightweightinventorysorting.config;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class ModMenu implements ModMenuApi {

    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return this::createConfigScreen;
    }

    private Screen createConfigScreen(Screen parent) {
        ConfigBuilder builder = ConfigBuilder.create()
                .setParentScreen(parent)
                .setTitle(Component.translatable("category.lightweight-inventory-sorting.title"));
        ConfigEntryBuilder entryBuilder = builder.entryBuilder();

        ConfigCategory generalSettings = builder.getOrCreateCategory(Component.translatable("category.lightweight-inventory-sorting.general"));

        generalSettings.addEntry(entryBuilder.startTextDescription(Component.translatable("category.lightweight-inventory-sorting.sort-options"))
                .build());

        generalSettings.addEntry(entryBuilder.startEnumSelector(
                        Component.translatable("category.lightweight-inventory-sorting.sort-type"),
                        SortType.class,
                        Config.sortType
                ).setDefaultValue(SortType.INDEX)
                .setSaveConsumer(newValue -> Config.sortType = newValue)
                .setTooltip(Component.translatable("category.lightweight-inventory-sorting.sort-type-tooltip"))
                .build());

        generalSettings.addEntry(entryBuilder.startBooleanToggle(
                        Component.translatable("category.lightweight-inventory-sorting.reverse-sort"),
                        Config.reverseSort
                ).setDefaultValue(false)
                .setSaveConsumer(newValue -> Config.reverseSort = newValue)
                .setTooltip(Component.translatable("category.lightweight-inventory-sorting.reverse-sort-tooltip"))
                .build());

        generalSettings.addEntry(entryBuilder.startIntField(
                        Component.translatable("category.lightweight-inventory-sorting.sort-delay"),
                        Config.sortDelay
                ).setDefaultValue(0)
                .setSaveConsumer(newValue -> Config.sortDelay = newValue)
                .setTooltip(Component.translatable("category.lightweight-inventory-sorting.sort-delay-tooltip"))
                .build());

        generalSettings.addEntry(entryBuilder.startTextDescription(Component.translatable("category.lightweight-inventory-sorting.button-options"))
                .build());

        generalSettings.addEntry(entryBuilder.startEnumSelector(
                        Component.translatable("category.lightweight-inventory-sorting.button-size"),
                        ButtonSize.class,
                        Config.buttonSize
                ).setDefaultValue(ButtonSize.LARGE)
                .setSaveConsumer(newValue -> Config.buttonSize = newValue)
                .build());

        generalSettings.addEntry(entryBuilder.startIntField(
                        Component.translatable("category.lightweight-inventory-sorting.inventory-x"),
                        Config.xOffsetInventory
                ).setDefaultValue(0)
                .setSaveConsumer(newValue -> Config.xOffsetInventory = newValue)
                .setTooltip(Component.translatable("category.lightweight-inventory-sorting.inventory-x-tooltip"))
                .build());

        generalSettings.addEntry(entryBuilder.startIntField(
                        Component.translatable("category.lightweight-inventory-sorting.inventory-y"),
                        Config.yOffsetInventory
                ).setDefaultValue(0)
                .setSaveConsumer(newValue -> Config.yOffsetInventory = newValue)
                .setTooltip(Component.translatable("category.lightweight-inventory-sorting.inventory-y-tooltip"))
                .build());

        generalSettings.addEntry(entryBuilder.startIntField(
                        Component.translatable("category.lightweight-inventory-sorting.container-x"),
                        Config.xOffsetContainer
                ).setDefaultValue(0)
                .setSaveConsumer(newValue -> Config.xOffsetContainer = newValue)
                .setTooltip(Component.translatable("category.lightweight-inventory-sorting.container-x-tooltip"))
                .build());

        generalSettings.addEntry(entryBuilder.startIntField(
                        Component.translatable("category.lightweight-inventory-sorting.container-y"),
                        Config.yOffsetContainer
                ).setDefaultValue(0)
                .setSaveConsumer(newValue -> Config.yOffsetContainer = newValue)
                .setTooltip(Component.translatable("category.lightweight-inventory-sorting.container-y-tooltip"))
                .build());

        builder.setSavingRunnable(Config::save);

        return builder.build();
    }
}
