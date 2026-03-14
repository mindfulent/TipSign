package dev.blockacademy.tipsign.screen;

import dev.blockacademy.tipsign.common.LinkParser;
import dev.blockacademy.tipsign.common.TipSignConfig;
import dev.blockacademy.tipsign.common.TipSignData;
import net.minecraft.Util;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.ConfirmLinkScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;

import java.util.List;

public class TipSignReaderScreen extends Screen {

    private static final int PANEL_WIDTH = 300;
    private static final int MIN_PANEL_HEIGHT = 80;
    private static final int MAX_PANEL_HEIGHT = 300;
    private static final int TITLE_COLOR = 0xFFEEDDCC;
    private static final int TEXT_COLOR = 0xFFE8D8C8;
    private static final int PAGE_COLOR = 0xFFAA9988;

    private final TipSignData data;
    private final BlockPos pos;
    private int currentPage = 0;

    private int panelLeft, panelTop;
    private int panelHeight;

    public TipSignReaderScreen(TipSignData data, BlockPos pos) {
        super(Component.literal(data.title() != null ? data.title() : TipSignData.DEFAULT_TITLE));
        this.data = data;
        this.pos = pos;
    }

    private int computePanelHeight() {
        // Fixed header: 8px top padding + 9px title + 1px separator + 10px gap = 28px
        int height = 28;

        // Text lines: find tallest page so height doesn't jump on page flip
        int maxLines = 0;
        for (String page : data.pages()) {
            String stripped = LinkParser.stripLinks(page);
            int lines = stripped.isEmpty() ? 0 : stripped.split("\n", -1).length;
            maxLines = Math.max(maxLines, lines);
        }
        height += maxLines * 11;

        // Supporter buttons row
        boolean hasKofi = data.kofiUrl() != null && !data.kofiUrl().isBlank();
        boolean hasPatreon = data.patreonUrl() != null && !data.patreonUrl().isBlank();
        if (hasKofi || hasPatreon) {
            height += 24;
        }

        // Page navigation row
        if (data.pages().size() > 1) {
            height += 24;
        }

        // Close button + bottom padding
        height += 25;

        return Math.max(MIN_PANEL_HEIGHT, Math.min(height, MAX_PANEL_HEIGHT));
    }

    @Override
    protected void init() {
        panelHeight = computePanelHeight();
        panelLeft = (this.width - PANEL_WIDTH) / 2;
        panelTop = (this.height - panelHeight) / 2;

        int btnWidth = 120;
        boolean hasKofi = data.kofiUrl() != null && !data.kofiUrl().isBlank();
        boolean hasPatreon = data.patreonUrl() != null && !data.patreonUrl().isBlank();

        // Layout bottom-up from panel bottom
        int y = panelTop + panelHeight - 22; // Close button

        this.addRenderableWidget(Button.builder(Component.translatable("tipsign.screen.reader.close"), btn -> {
            this.onClose();
        }).bounds(panelLeft + PANEL_WIDTH / 2 - 30, y, 60, 20).build());

        // Page navigation arrows
        if (data.pages().size() > 1) {
            y -= 24;
            this.addRenderableWidget(Button.builder(Component.literal("\u25C0"), btn -> {
                if (currentPage > 0) currentPage--;
            }).bounds(panelLeft + 10, y, 24, 20).build());

            this.addRenderableWidget(Button.builder(Component.literal("\u25B6"), btn -> {
                if (currentPage < data.pages().size() - 1) currentPage++;
            }).bounds(panelLeft + PANEL_WIDTH - 34, y, 24, 20).build());
        }

        // Supporter buttons
        if (hasKofi || hasPatreon) {
            y -= 24;
            int supporterX = panelLeft + PANEL_WIDTH / 2;

            if (hasKofi && hasPatreon) {
                addSupporterButton(supporterX - btnWidth - 4, y, btnWidth,
                    "tipsign.screen.reader.kofi", data.kofiUrl());
                addSupporterButton(supporterX + 4, y, btnWidth,
                    "tipsign.screen.reader.patreon", data.patreonUrl());
            } else if (hasKofi) {
                addSupporterButton(supporterX - btnWidth / 2, y, btnWidth,
                    "tipsign.screen.reader.kofi", data.kofiUrl());
            } else {
                addSupporterButton(supporterX - btnWidth / 2, y, btnWidth,
                    "tipsign.screen.reader.patreon", data.patreonUrl());
            }
        }
    }

    private void addSupporterButton(int x, int y, int width, String translationKey, String url) {
        Button btn = Button.builder(Component.translatable(translationKey), b -> {
            openUrlWithConfirmation(url);
        }).bounds(x, y, width, 20).build();
        this.addRenderableWidget(btn);
    }

    private void openUrlWithConfirmation(String url) {
        TipSignConfig config = TipSignConfig.get();
        if (config.requireConfirmBeforeBrowserOpen()) {
            this.minecraft.setScreen(new ConfirmLinkScreen(confirmed -> {
                if (confirmed) {
                    Util.getPlatform().openUri(url);
                }
                this.minecraft.setScreen(this);
            }, url, true));
        } else {
            Util.getPlatform().openUri(url);
        }
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(graphics, mouseX, mouseY, partialTick);

        int bgColor = TipSignData.bgColor(data.bgColorIndex());
        int borderColor = TipSignData.borderColor(data.bgColorIndex());

        // Panel background with border
        graphics.fill(panelLeft - 2, panelTop - 2, panelLeft + PANEL_WIDTH + 2, panelTop + panelHeight + 2, borderColor);
        graphics.fill(panelLeft, panelTop, panelLeft + PANEL_WIDTH, panelTop + panelHeight, bgColor);

        // Title (bold, centered)
        String title = data.title() != null ? data.title() : TipSignData.DEFAULT_TITLE;
        graphics.drawCenteredString(this.font,
            Component.literal(title).withStyle(Style.EMPTY.withBold(true)),
            panelLeft + PANEL_WIDTH / 2, panelTop + 8, TITLE_COLOR);

        // Separator line
        graphics.fill(panelLeft + 10, panelTop + 22, panelLeft + PANEL_WIDTH - 10, panelTop + 23, 0x44FFFFFF);

        // Page text
        if (!data.pages().isEmpty() && currentPage < data.pages().size()) {
            String pageText = data.pages().get(currentPage);
            int lineY = panelTop + 28;
            int maxLineY = panelTop + panelHeight - 60;

            // Parse inline links
            TipSignConfig config = TipSignConfig.get();
            List<LinkParser.ParsedLink> links = config.allowInlineLinks()
                ? LinkParser.extractLinks(pageText) : List.of();

            // Render plain text with links highlighted
            String strippedText = LinkParser.stripLinks(pageText);
            for (String line : strippedText.split("\n")) {
                if (lineY > maxLineY) break;
                graphics.drawString(this.font, line, panelLeft + 12, lineY, TEXT_COLOR);
                lineY += 11;
            }
        }

        // Page counter
        if (data.pages().size() > 1) {
            String pageCounter = "Page " + (currentPage + 1) + " / " + data.pages().size();
            graphics.drawCenteredString(this.font, pageCounter,
                panelLeft + PANEL_WIDTH / 2, panelTop + panelHeight - 46 + 5, PAGE_COLOR);
        }

        super.render(graphics, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
