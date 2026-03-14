package dev.blockacademy.tipsign.screen;

import dev.blockacademy.tipsign.common.TipSignConfig;
import dev.blockacademy.tipsign.common.TipSignData;
import dev.blockacademy.tipsign.common.UrlValidator;
import dev.blockacademy.tipsign.compat.VersionAdapter;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;

import java.util.ArrayList;
import java.util.List;

public class TipSignAuthorScreen extends Screen {

    private static final int PANEL_WIDTH = 320;
    private static final int PANEL_HEIGHT = 290;
    private static final int LABEL_COLOR = 0xFFE8D8C8;
    private static final int ERROR_COLOR = 0xFFFF6666;

    private final TipSignData originalData;
    private final BlockPos pos;

    private EditBox titleField;
    private EditBox bodyField;
    private EditBox kofiField;
    private EditBox patreonField;
    private int currentPage = 0;
    private final List<String> pages;
    private String editTitle;
    private String editKofi;
    private String editPatreon;
    private int editBgColorIndex;
    private String validationError = null;
    private boolean deleteConfirmPending = false;

    private int panelLeft, panelTop;

    public TipSignAuthorScreen(TipSignData data, BlockPos pos) {
        super(Component.translatable("tipsign.screen.author.title"));
        this.originalData = data;
        this.pos = pos;
        this.pages = new ArrayList<>(data.pages());
        this.editTitle = data.title() != null ? data.title() : TipSignData.DEFAULT_TITLE;
        this.editKofi = data.kofiUrl() != null ? data.kofiUrl() : "";
        this.editPatreon = data.patreonUrl() != null ? data.patreonUrl() : "";
        this.editBgColorIndex = data.bgColorIndex();
    }

    @Override
    protected void init() {
        panelLeft = (this.width - PANEL_WIDTH) / 2;
        panelTop = (this.height - PANEL_HEIGHT) / 2;

        int fieldLeft = panelLeft + 70;
        int fieldWidth = PANEL_WIDTH - 80;
        int y = panelTop + 12;

        // Title field
        this.titleField = new EditBox(this.font, fieldLeft, y, fieldWidth, 16, Component.literal("Title"));
        this.titleField.setMaxLength(TipSignData.MAX_TITLE_LENGTH);
        this.titleField.setValue(editTitle);
        this.titleField.setResponder(s -> editTitle = s);
        this.addRenderableWidget(titleField);
        y += 24;

        // Body text area (3 lines tall for easier editing)
        int bodyHeight = 40;
        this.bodyField = new EditBox(this.font, panelLeft + 10, y, PANEL_WIDTH - 20, bodyHeight, Component.literal("Body"));
        this.bodyField.setMaxLength(1120);
        if (!pages.isEmpty() && currentPage < pages.size()) {
            this.bodyField.setValue(pages.get(currentPage));
        }
        this.bodyField.setResponder(s -> {
            if (currentPage < pages.size()) {
                pages.set(currentPage, s);
            }
        });
        this.addRenderableWidget(bodyField);
        y += bodyHeight + 4;

        // Page navigation
        int navY = y;
        this.addRenderableWidget(Button.builder(Component.literal("\u25C0 Prev"), btn -> {
            if (currentPage > 0) {
                currentPage--;
                bodyField.setValue(pages.get(currentPage));
            }
        }).bounds(panelLeft + 10, navY, 65, 20).build());

        this.addRenderableWidget(Button.builder(Component.literal("Next \u25B6"), btn -> {
            if (currentPage < pages.size() - 1) {
                currentPage++;
                bodyField.setValue(pages.get(currentPage));
            }
        }).bounds(panelLeft + PANEL_WIDTH - 75, navY, 65, 20).build());

        // Add/Delete page
        TipSignConfig config = TipSignConfig.get();
        this.addRenderableWidget(Button.builder(Component.literal("+ Page"), btn -> {
            if (pages.size() < config.maxPages()) {
                pages.add("");
                currentPage = pages.size() - 1;
                bodyField.setValue("");
            }
        }).bounds(panelLeft + PANEL_WIDTH / 2 - 50, navY, 48, 20).build());

        this.addRenderableWidget(Button.builder(Component.literal("- Page"), btn -> {
            if (pages.size() > 1) {
                pages.remove(currentPage);
                if (currentPage >= pages.size()) currentPage = pages.size() - 1;
                bodyField.setValue(pages.get(currentPage));
            }
        }).bounds(panelLeft + PANEL_WIDTH / 2 + 2, navY, 48, 20).build());

        y += 28;

        // Ko-fi URL field
        this.kofiField = new EditBox(this.font, fieldLeft, y, fieldWidth, 16, Component.literal("Ko-fi"));
        this.kofiField.setMaxLength(128);
        this.kofiField.setValue(editKofi);
        this.kofiField.setResponder(s -> editKofi = s);
        this.addRenderableWidget(kofiField);
        y += 22;

        // Patreon URL field
        this.patreonField = new EditBox(this.font, fieldLeft, y, fieldWidth, 16, Component.literal("Patreon"));
        this.patreonField.setMaxLength(128);
        this.patreonField.setValue(editPatreon);
        this.patreonField.setResponder(s -> editPatreon = s);
        this.addRenderableWidget(patreonField);
        y += 24;

        // Background color cycle button
        this.addRenderableWidget(Button.builder(
            Component.literal("Theme: " + TipSignData.BG_PRESET_NAMES[editBgColorIndex]),
            btn -> {
                editBgColorIndex = (editBgColorIndex + 1) % TipSignData.BG_PRESETS.length;
                btn.setMessage(Component.literal("Theme: " + TipSignData.BG_PRESET_NAMES[editBgColorIndex]));
            }
        ).bounds(panelLeft + 10, y, 120, 20).build());

        // Formatting reference (same line as theme button)
        y += 26;

        // Save / Cancel / Delete buttons
        int btnY = panelTop + PANEL_HEIGHT - 28;
        this.addRenderableWidget(Button.builder(Component.translatable("tipsign.screen.author.save"), btn -> {
            save();
        }).bounds(panelLeft + 10, btnY, 80, 20).build());

        this.addRenderableWidget(Button.builder(Component.translatable("tipsign.screen.author.cancel"), btn -> {
            this.onClose();
        }).bounds(panelLeft + 95, btnY, 80, 20).build());

        // Delete All Content button (destructive, right side)
        this.addRenderableWidget(Button.builder(
            Component.translatable("tipsign.screen.author.delete_all").withStyle(Style.EMPTY.withColor(0xFF6666)),
            btn -> {
                if (deleteConfirmPending) {
                    deleteAllContent();
                    deleteConfirmPending = false;
                } else {
                    deleteConfirmPending = true;
                }
            }
        ).bounds(panelLeft + PANEL_WIDTH - 120, btnY, 110, 20).build());
    }

    private void save() {
        validationError = null;

        // Validate Ko-fi URL
        String kofiUrl = null;
        if (!editKofi.isBlank()) {
            kofiUrl = UrlValidator.toKofiUrl(editKofi);
            if (kofiUrl == null) {
                validationError = "Invalid Ko-fi URL or username";
                return;
            }
        }

        // Validate Patreon URL
        String patreonUrl = null;
        if (!editPatreon.isBlank()) {
            patreonUrl = UrlValidator.toPatreonUrl(editPatreon);
            if (patreonUrl == null) {
                validationError = "Invalid Patreon URL or username";
                return;
            }
        }

        TipSignData updated = new TipSignData(
            originalData.id(),
            editTitle,
            new ArrayList<>(pages),
            kofiUrl,
            patreonUrl,
            originalData.ownerUuid(),
            originalData.ownerUsername(),
            originalData.placedAt(),
            originalData.lastEditedAt(),
            editBgColorIndex
        );

        VersionAdapter.INSTANCE.sendUpdateToServer(pos, updated);
        this.onClose();
    }

    private void deleteAllContent() {
        pages.clear();
        pages.add("");
        currentPage = 0;
        editTitle = TipSignData.DEFAULT_TITLE;
        editKofi = "";
        editPatreon = "";

        titleField.setValue(editTitle);
        bodyField.setValue("");
        kofiField.setValue("");
        patreonField.setValue("");

        // Send blank data to server
        TipSignData blank = new TipSignData(
            originalData.id(),
            TipSignData.DEFAULT_TITLE,
            new ArrayList<>(pages),
            null,
            null,
            originalData.ownerUuid(),
            originalData.ownerUsername(),
            originalData.placedAt(),
            originalData.lastEditedAt(),
            editBgColorIndex
        );
        VersionAdapter.INSTANCE.sendUpdateToServer(pos, blank);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(graphics, mouseX, mouseY, partialTick);

        int bgColor = TipSignData.bgColor(editBgColorIndex);
        int borderColor = TipSignData.borderColor(editBgColorIndex);

        // Panel background
        graphics.fill(panelLeft - 2, panelTop - 2, panelLeft + PANEL_WIDTH + 2, panelTop + PANEL_HEIGHT + 2, borderColor);
        graphics.fill(panelLeft, panelTop, panelLeft + PANEL_WIDTH, panelTop + PANEL_HEIGHT, bgColor);

        int y = panelTop + 15;

        // Labels
        graphics.drawString(this.font, "Title:", panelLeft + 12, y, LABEL_COLOR);
        y += 24;

        // Body hint text
        graphics.drawString(this.font, "Page " + (currentPage + 1) + "/" + pages.size(),
            panelLeft + 12, y - 2, 0xFFAA9988);
        y += 40 + 4 + 28; // body height + gap + nav

        // Ko-fi / Patreon labels
        graphics.drawString(this.font, "Ko-fi:", panelLeft + 12, y, LABEL_COLOR);
        y += 22;
        graphics.drawString(this.font, "Patreon:", panelLeft + 12, y, LABEL_COLOR);
        y += 24;

        // Formatting reference (below theme button)
        y += 22;
        graphics.drawString(this.font, "\u00a7oLinks: [text](url)  \u00a7oBold: \u00a7l**text**",
            panelLeft + 12, y, 0xFF888877);

        // Validation error (above buttons, within panel)
        if (validationError != null) {
            graphics.drawCenteredString(this.font, validationError,
                panelLeft + PANEL_WIDTH / 2, panelTop + PANEL_HEIGHT - 44, ERROR_COLOR);
        }

        // Delete confirmation message (above buttons, within panel)
        if (deleteConfirmPending) {
            graphics.drawCenteredString(this.font,
                Component.translatable("tipsign.screen.author.delete_confirm"),
                panelLeft + PANEL_WIDTH / 2, panelTop + PANEL_HEIGHT - 44, ERROR_COLOR);
        }

        super.render(graphics, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
