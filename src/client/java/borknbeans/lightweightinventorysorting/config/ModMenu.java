package borknbeans.lightweightinventorysorting.config;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import me.shedaniel.clothconfig2.api.Requirement;
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

        ConfigCategory general = builder.getOrCreateCategory(Text.of("Sort Button Settings"));

        ConfigEntryBuilder entryBuilder = builder.entryBuilder();

        general.addEntry(entryBuilder.startEnumSelector(
                        Text.translatable("Button Size"),
                        ButtonSize.class,
                        LightweightInventorySortingConfig.buttonSize
                ).setDefaultValue(ButtonSize.LARGE)
                .setSaveConsumer(newValue -> LightweightInventorySortingConfig.buttonSize = newValue)
                .setTooltip(Text.translatable("Change the size of the sort button:\nSMALL\nMEDIUM\nLARGE"))
                .build());

        general.addEntry(entryBuilder.startIntField(
                    Text.translatable("Inventory X Offset"),
                    LightweightInventorySortingConfig.xOffsetInventory
                ).setDefaultValue(0)
                .setSaveConsumer(newValue -> LightweightInventorySortingConfig.xOffsetInventory = newValue)
                .setTooltip(Text.translatable("Move the inventory sort button on the x-axis"))
                .build());

        general.addEntry(entryBuilder.startIntField(
                        Text.translatable("Inventory Y Offset"),
                        LightweightInventorySortingConfig.yOffsetInventory
                ).setDefaultValue(0)
                .setSaveConsumer(newValue -> LightweightInventorySortingConfig.yOffsetInventory = newValue)
                .setTooltip(Text.translatable("Move the inventory sort button on the y-axis"))
                .build());

        general.addEntry(entryBuilder.startIntField(
                        Text.translatable("Container X Offset"),
                        LightweightInventorySortingConfig.xOffsetContainer
                ).setDefaultValue(0)
                .setSaveConsumer(newValue -> LightweightInventorySortingConfig.xOffsetContainer = newValue)
                .setTooltip(Text.translatable("Move the container sort button on the x-axis"))
                .build());

        general.addEntry(entryBuilder.startIntField(
                        Text.translatable("Container Y Offset"),
                        LightweightInventorySortingConfig.yOffsetContainer
                ).setDefaultValue(0)
                .setSaveConsumer(newValue -> LightweightInventorySortingConfig.yOffsetContainer = newValue)
                .setTooltip(Text.translatable("Move the container sort button on the y-axis"))
                .build());

        builder.setSavingRunnable(() -> LightweightInventorySortingConfig.save());

        return builder.build();
    }
}
