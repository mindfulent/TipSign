package dev.blockacademy.tipsign.screen;

import dev.blockacademy.tipsign.common.TipSignData;
import dev.blockacademy.tipsign.common.TipSignDataCodec;
import dev.blockacademy.tipsign.compat.VersionAdapter;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;

public class TipSignAuthorScreen extends Screen {

    private final TipSignData originalData;
    private final BlockPos pos;

    private EditBox titleField;
    private EditBox bodyField;
    private int currentPage = 0;
    private final List<String> pages;
    private String editTitle;

    public TipSignAuthorScreen(TipSignData data, BlockPos pos) {
        super(Component.translatable("tipsign.screen.author.title"));
        this.originalData = data;
        this.pos = pos;
        this.pages = new ArrayList<>(data.pages());
        this.editTitle = data.title() != null ? data.title() : TipSignData.DEFAULT_TITLE;
    }

    @Override
    protected void init() {
        int centerX = this.width / 2;
        int topY = this.height / 2 - 90;

        // Title field
        this.titleField = new EditBox(this.font, centerX - 100, topY, 200, 18, Component.literal("Title"));
        this.titleField.setMaxLength(TipSignData.MAX_TITLE_LENGTH);
        this.titleField.setValue(editTitle);
        this.titleField.setResponder(s -> editTitle = s);
        this.addRenderableWidget(titleField);

        // Body text field
        this.bodyField = new EditBox(this.font, centerX - 100, topY + 25, 200, 18, Component.literal("Body"));
        this.bodyField.setMaxLength(1120); // 14 lines * 80 chars
        if (!pages.isEmpty() && currentPage < pages.size()) {
            this.bodyField.setValue(pages.get(currentPage));
        }
        this.bodyField.setResponder(s -> {
            if (currentPage < pages.size()) {
                pages.set(currentPage, s);
            }
        });
        this.addRenderableWidget(bodyField);

        int btnY = topY + 50;

        // Page navigation
        this.addRenderableWidget(Button.builder(Component.literal("< Prev"), btn -> {
            if (currentPage > 0) {
                currentPage--;
                bodyField.setValue(pages.get(currentPage));
            }
        }).bounds(centerX - 100, btnY, 60, 20).build());

        this.addRenderableWidget(Button.builder(Component.literal("Next >"), btn -> {
            if (currentPage < pages.size() - 1) {
                currentPage++;
                bodyField.setValue(pages.get(currentPage));
            }
        }).bounds(centerX + 40, btnY, 60, 20).build());

        // Add page
        this.addRenderableWidget(Button.builder(Component.literal("+Page"), btn -> {
            if (pages.size() < 10) { // Will use config maxPages in Phase 5
                pages.add("");
                currentPage = pages.size() - 1;
                bodyField.setValue("");
            }
        }).bounds(centerX - 25, btnY, 50, 20).build());

        // Save
        this.addRenderableWidget(Button.builder(Component.translatable("tipsign.screen.author.save"), btn -> {
            save();
        }).bounds(centerX - 100, btnY + 30, 95, 20).build());

        // Cancel
        this.addRenderableWidget(Button.builder(Component.translatable("tipsign.screen.author.cancel"), btn -> {
            this.onClose();
        }).bounds(centerX + 5, btnY + 30, 95, 20).build());
    }

    private void save() {
        TipSignData updated = new TipSignData(
            originalData.id(),
            editTitle,
            new ArrayList<>(pages),
            originalData.kofiUrl(),
            originalData.patreonUrl(),
            originalData.ownerUuid(),
            originalData.ownerUsername(),
            originalData.placedAt(),
            originalData.lastEditedAt()
        );

        // Send update packet to server via VersionAdapter
        VersionAdapter.INSTANCE.sendUpdateToServer(pos, updated);
        this.onClose();
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(graphics, mouseX, mouseY, partialTick);

        int centerX = this.width / 2;
        int topY = this.height / 2 - 90;

        // Background panel
        graphics.fill(centerX - 115, topY - 15, centerX + 115, topY + 110, 0xCC3B2A1A);

        // Labels
        graphics.drawString(this.font, "Title:", centerX - 110, topY - 10, 0xFFE8D8C8);

        // Page counter
        String pageLabel = "Page " + (currentPage + 1) + " / " + pages.size();
        graphics.drawCenteredString(this.font, pageLabel, centerX, topY + 50 + 3, 0xFFAA9988);

        super.render(graphics, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
