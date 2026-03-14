package dev.blockacademy.tipsign.screen;

import dev.blockacademy.tipsign.common.TipSignData;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;

public class TipSignReaderScreen extends Screen {

    private final TipSignData data;
    private final BlockPos pos;
    private int currentPage = 0;

    public TipSignReaderScreen(TipSignData data, BlockPos pos) {
        super(Component.literal(data.title() != null ? data.title() : TipSignData.DEFAULT_TITLE));
        this.data = data;
        this.pos = pos;
    }

    @Override
    protected void init() {
        int centerX = this.width / 2;
        int bottomY = this.height / 2 + 80;

        // Page navigation
        if (data.pages().size() > 1) {
            this.addRenderableWidget(Button.builder(Component.literal("<"), btn -> {
                if (currentPage > 0) currentPage--;
            }).bounds(centerX - 100, bottomY, 20, 20).build());

            this.addRenderableWidget(Button.builder(Component.literal(">"), btn -> {
                if (currentPage < data.pages().size() - 1) currentPage++;
            }).bounds(centerX + 80, bottomY, 20, 20).build());
        }

        // Close button
        this.addRenderableWidget(Button.builder(Component.translatable("tipsign.screen.reader.close"), btn -> {
            this.onClose();
        }).bounds(centerX - 40, bottomY + 25, 80, 20).build());
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(graphics, mouseX, mouseY, partialTick);

        int centerX = this.width / 2;
        int topY = this.height / 2 - 80;

        // Background panel
        graphics.fill(centerX - 120, topY - 10, centerX + 120, topY + 170, 0xCC3B2A1A);

        // Title
        String title = data.title() != null ? data.title() : TipSignData.DEFAULT_TITLE;
        graphics.drawCenteredString(this.font, Component.literal(title).withStyle(s -> s.withBold(true)),
            centerX, topY, 0xFFEEDDCC);

        // Page text
        if (!data.pages().isEmpty() && currentPage < data.pages().size()) {
            String pageText = data.pages().get(currentPage);
            int lineY = topY + 20;
            for (String line : pageText.split("\n")) {
                if (lineY > topY + 140) break;
                graphics.drawString(this.font, line, centerX - 110, lineY, 0xFFE8D8C8);
                lineY += 10;
            }
        }

        // Page counter
        if (data.pages().size() > 1) {
            String pageCounter = "Page " + (currentPage + 1) + " / " + data.pages().size();
            graphics.drawCenteredString(this.font, pageCounter, centerX, this.height / 2 + 80 + 3, 0xFFAA9988);
        }

        super.render(graphics, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
