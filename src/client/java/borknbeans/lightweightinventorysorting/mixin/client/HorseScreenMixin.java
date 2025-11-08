package borknbeans.lightweightinventorysorting.mixin.client;

import borknbeans.lightweightinventorysorting.LightweightInventorySortingClient;
import borknbeans.lightweightinventorysorting.config.Config;
import borknbeans.lightweightinventorysorting.sorting.SortButton;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.screen.ingame.HorseScreen;
import net.minecraft.client.input.KeyInput;
import net.minecraft.client.input.MouseInput;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.HorseScreenHandler;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HorseScreen.class)
public abstract class HorseScreenMixin extends HandledScreen<HorseScreenHandler> {
    @Unique
    private SortButton sortButton;

    public HorseScreenMixin(HorseScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
    }

    @Override
    public void init() {
        super.init();

        // Initialize button
        int x = this.x + this.backgroundWidth - 20 + Config.xOffsetContainer;
        int y = this.y + 4 + Config.yOffsetContainer;
        int size = Config.buttonSize.getButtonSize();
        sortButton = new SortButton(x, y, size, size, Text.literal("S"), 0, getScreenHandler().slots.size() - 37);

        // Add button to the screen
        this.addDrawableChild(sortButton);
    }

    @Inject(method = "render", at = @At("RETURN"))
    private void onRender(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        if (sortButton != null) {
            sortButton.render(context, mouseX, mouseY, delta);
        }
    }

    @Override
    public boolean keyPressed(KeyInput keyInput) {
        if (LightweightInventorySortingClient.sortKeyBind.matchesKey(keyInput)) {
            sortButton.onPress(new MouseInput(0,0));
            return true;
        }

        return super.keyPressed(keyInput);
    }

    @Override
    public boolean mouseClicked(Click click, boolean doubleClick) {
        if (LightweightInventorySortingClient.sortKeyBind.matchesMouse(click)) {
            sortButton.simulateClick(click); // Simulate a click
            return true;
        }

        return super.mouseClicked(click, doubleClick);
    }
}
