package borknbeans.lightweightinventorysorting.sorting;

import com.mojang.blaze3d.pipeline.RenderPipeline;

import borknbeans.lightweightinventorysorting.LightweightInventorySorting;
import borknbeans.lightweightinventorysorting.config.Config;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class SortButton extends ClickableWidget {

    private Identifier buttonTexture;
    private Identifier buttonHoverTexture;

    private int sortStartIndex, sortEndIndex;

    public SortButton(int x, int y, int width, int height, Text message, int startIndex, int endIndex) {
        super(x, y, width, height, message);

        this.sortStartIndex = startIndex;
        this.sortEndIndex = endIndex;

        buttonTexture = Config.buttonSize.getButtonTexture();
        buttonHoverTexture = Config.buttonSize.getButtonHoverTexture();
    }

    @Override
    protected void appendClickableNarrations(NarrationMessageBuilder builder) {
        // Narration message if needed
    }

    @Override
    protected void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
        if (this.isHovered()) {
            context.drawGuiTexture(RenderPipelines.GUI_TEXTURED, buttonHoverTexture, this.getX(), this.getY(), this.getWidth(), this.getHeight());
        } else {
            context.drawGuiTexture(RenderPipelines.GUI_TEXTURED, buttonTexture, this.getX(), this.getY(), this.getWidth(), this.getHeight());
        }
    }

    @Override
    public void onClick(Click click, boolean doubled) {
        MinecraftClient client = MinecraftClient.getInstance();

        if (client.player != null) {
            Sorter.sortContainerClientside(client, sortStartIndex, sortEndIndex);
            // TODO: handle server-side sorting if enabled
        } else {
            LightweightInventorySorting.LOGGER.error("Player is not available.");
        }
    }
    
}
