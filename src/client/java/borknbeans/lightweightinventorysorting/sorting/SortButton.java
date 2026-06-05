package borknbeans.lightweightinventorysorting.sorting;

import borknbeans.lightweightinventorysorting.LightweightInventorySorting;
import borknbeans.lightweightinventorysorting.config.Config;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

public class SortButton extends AbstractWidget {

    private final Identifier buttonTexture;
    private final Identifier buttonHoverTexture;
    private final int sortStartIndex;
    private final int sortEndIndex;

    public SortButton(int x, int y, int width, int height, Component message, int startIndex, int endIndex) {
        super(x, y, width, height, message);
        this.sortStartIndex = startIndex;
        this.sortEndIndex = endIndex;
        this.buttonTexture = Config.buttonSize.getButtonTexture();
        this.buttonHoverTexture = Config.buttonSize.getButtonHoverTexture();
    }

    public int getSortStartIndex() {
        return sortStartIndex;
    }

    public int getSortEndIndex() {
        return sortEndIndex;
    }

    @Override
    protected void extractWidgetRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        Identifier texture = this.isHovered() ? buttonHoverTexture : buttonTexture;
        graphics.blitSprite(RenderPipelines.GUI_TEXTURED, texture, this.getX(), this.getY(), this.getWidth(), this.getHeight());
    }

    @Override
    public void onClick(MouseButtonEvent event, boolean doubleClick) {
        Minecraft client = Minecraft.getInstance();
        if (client.player != null) {
            Sorter.sortContainerClientside(client, sortStartIndex, sortEndIndex);
            // TODO: handle server-side sorting if enabled
        } else {
            LightweightInventorySorting.LOGGER.error("Player is not available.");
        }
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput output) {
        output.add(NarratedElementType.TITLE, this.createNarrationMessage());
        if (this.active) {
            if (this.isFocused()) {
                output.add(NarratedElementType.USAGE, Component.translatable("narration.button.usage.focused"));
            } else {
                output.add(NarratedElementType.USAGE, Component.translatable("narration.button.usage.hovered"));
            }
        }
    }
}
