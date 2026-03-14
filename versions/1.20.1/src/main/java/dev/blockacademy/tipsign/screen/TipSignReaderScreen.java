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
    private static final int PANEL_HEIGHT = 220;
    private static final int BG_COLOR = 0xEE3B2A1A;
    private static final int BORDER_COLOR = 0xFF2A1A0A;
    private static final int TITLE_COLOR = 0xFFEEDDCC;
    private static final int TEXT_COLOR = 0xFFE8D8C8;
    private static final int LINK_COLOR = 0xFF6699FF;
    private static final int PAGE_COLOR = 0xFFAA9988;

    // Ko-fi / Patreon button colors
    private static final int KOFI_COLOR = 0xFFFF5E5B;
    private static final int PATREON_COLOR = 0xFFFF424D;

    private final TipSignData data;
    private final BlockPos pos;
    private int currentPage = 0;

    private int panelLeft, panelTop;

    public TipSignReaderScreen(TipSignData data, BlockPos pos) {
        super(Component.literal(data.title() != null ? data.title() : TipSignData.DEFAULT_TITLE));
        this.data = data;
        this.pos = pos;
    }

    @Override
    protected void init() {
        panelLeft = (this.width - PANEL_WIDTH) / 2;
        panelTop = (this.height - PANEL_HEIGHT) / 2;

        int bottomY = panelTop + PANEL_HEIGHT - 28;
        int btnWidth = 80;

        // Page navigation arrows
        if (data.pages().size() > 1) {
            this.addRenderableWidget(Button.builder(Component.literal("\u25C0"), btn -> {
                if (currentPage > 0) currentPage--;
            }).bounds(panelLeft + 10, bottomY, 24, 20).build());

            this.addRenderableWidget(Button.builder(Component.literal("\u25B6"), btn -> {
                if (currentPage < data.pages().size() - 1) currentPage++;
            }).bounds(panelLeft + PANEL_WIDTH - 34, bottomY, 24, 20).build());
        }

        // Supporter buttons
        int supporterY = bottomY - 24;
        int supporterX = panelLeft + PANEL_WIDTH / 2;

        boolean hasKofi = data.kofiUrl() != null && !data.kofiUrl().isBlank();
        boolean hasPatreon = data.patreonUrl() != null && !data.patreonUrl().isBlank();

        if (hasKofi && hasPatreon) {
            addSupporterButton(supporterX - btnWidth - 4, supporterY, btnWidth,
                "tipsign.screen.reader.kofi", KOFI_COLOR, data.kofiUrl());
            addSupporterButton(supporterX + 4, supporterY, btnWidth,
                "tipsign.screen.reader.patreon", PATREON_COLOR, data.patreonUrl());
        } else if (hasKofi) {
            addSupporterButton(supporterX - btnWidth / 2, supporterY, btnWidth,
                "tipsign.screen.reader.kofi", KOFI_COLOR, data.kofiUrl());
        } else if (hasPatreon) {
            addSupporterButton(supporterX - btnWidth / 2, supporterY, btnWidth,
                "tipsign.screen.reader.patreon", PATREON_COLOR, data.patreonUrl());
        }

        // Close button
        this.addRenderableWidget(Button.builder(Component.translatable("tipsign.screen.reader.close"), btn -> {
            this.onClose();
        }).bounds(panelLeft + PANEL_WIDTH / 2 - 30, panelTop + PANEL_HEIGHT - 5, 60, 20).build());
    }

    private void addSupporterButton(int x, int y, int width, String translationKey, int color, String url) {
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
        // 1.20.1: renderBackground takes only GuiGraphics (no mouse/tick params)
        this.renderBackground(graphics);

        // Panel background with border
        graphics.fill(panelLeft - 2, panelTop - 2, panelLeft + PANEL_WIDTH + 2, panelTop + PANEL_HEIGHT + 18, BORDER_COLOR);
        graphics.fill(panelLeft, panelTop, panelLeft + PANEL_WIDTH, panelTop + PANEL_HEIGHT + 16, BG_COLOR);

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
            int maxLineY = panelTop + PANEL_HEIGHT - 60;

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
                panelLeft + PANEL_WIDTH / 2, panelTop + PANEL_HEIGHT - 28 + 5, PAGE_COLOR);
        }

        super.render(graphics, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
