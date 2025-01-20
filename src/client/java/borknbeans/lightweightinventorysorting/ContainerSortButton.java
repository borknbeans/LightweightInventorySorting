package borknbeans.lightweightinventorysorting;

import borknbeans.lightweightinventorysorting.config.LightweightInventorySortingConfig;
import borknbeans.lightweightinventorysorting.sorting.SortingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class ContainerSortButton extends ClickableWidget {

    private Identifier buttonTexture;
    private Identifier buttonHoverTexture;

    private int startIndex, endIndex;
    private HandledScreen<?> screen;

    public ContainerSortButton(int x, int y, int width, int height, Text message, int startIndex, int endIndex, HandledScreen<?> screen) {
        super(x, y, width, height, message);

        this.startIndex = startIndex;
        this.endIndex = endIndex;

        buttonTexture = LightweightInventorySortingConfig.buttonSize.getButtonTexture();
        buttonHoverTexture = LightweightInventorySortingConfig.buttonSize.getButtonHoverTexture();

        this.screen = screen;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        if (this.isHovered()) {
            context.drawTexture(buttonHoverTexture, this.getX(), this.getY(), 1, 1, this.getWidth(), this.getHeight());
        } else {
            context.drawTexture(buttonTexture, this.getX(), this.getY(), 1, 1, this.getWidth(), this.getHeight());
        }
    }

    @Override
    protected void appendClickableNarrations(NarrationMessageBuilder builder) {
        // Narration message if needed
    }

    @Override
    protected void renderButton(DrawContext context, int mouseX, int mouseY, float delta) {
        // Render button if needed
    }

    @Override
    public void onClick(double mouseX, double mouseY) {
        MinecraftClient client = MinecraftClient.getInstance();

        if (client.player != null) {
            SortingHelper.sortInventory(client, startIndex, endIndex);
        } else {
            System.out.println("Player is not available.");
        }
    }

}
