package dev.blockacademy.tipsign.common;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Plain Java record holding all TipSign data — no Minecraft dependencies.
 */
public record TipSignData(
    String id,
    String title,
    List<String> pages,
    String kofiUrl,
    String patreonUrl,
    UUID ownerUuid,
    String ownerUsername,
    Instant placedAt,
    Instant lastEditedAt,
    int bgColorIndex
) {
    public static final String DEFAULT_TITLE = "Notice";
    public static final int MAX_TITLE_LENGTH = 48;

    // Background color presets: [panel fill, border]
    public static final int[] BG_PRESETS = {
        0xEE3B2A1A,  // Oak (default brown)
        0xEE2A1E12,  // Dark Oak
        0xEE3A3021,  // Spruce
        0xEEBCA87A,  // Birch
        0xEE6B3231,  // Crimson
        0xEE2C6E5E,  // Warped
        0xEE4A4A50,  // Stone
        0xEE1A1A2A,  // Obsidian
    };
    public static final int[] BG_BORDER_PRESETS = {
        0xFF2A1A0A,  // Oak
        0xFF1A0E02,  // Dark Oak
        0xFF2A2011,  // Spruce
        0xFFA0906A,  // Birch
        0xFF4B1211,  // Crimson
        0xFF1C4E3E,  // Warped
        0xFF2A2A30,  // Stone
        0xFF0A0A1A,  // Obsidian
    };
    public static final String[] BG_PRESET_NAMES = {
        "Oak", "Dark Oak", "Spruce", "Birch", "Crimson", "Warped", "Stone", "Obsidian"
    };

    public static int bgColor(int index) {
        return BG_PRESETS[Math.max(0, Math.min(index, BG_PRESETS.length - 1))];
    }

    public static int borderColor(int index) {
        return BG_BORDER_PRESETS[Math.max(0, Math.min(index, BG_BORDER_PRESETS.length - 1))];
    }

    public static TipSignData createNew(UUID ownerUuid, String ownerUsername) {
        Instant now = Instant.now();
        List<String> pages = new ArrayList<>();
        pages.add("");
        String title = (ownerUsername != null && !ownerUsername.isEmpty())
            ? "Build by " + ownerUsername
            : DEFAULT_TITLE;
        return new TipSignData(
            UUID.randomUUID().toString(),
            title,
            pages,
            null,
            null,
            ownerUuid,
            ownerUsername,
            now,
            now,
            0
        );
    }

    public TipSignData withTitle(String title) {
        return new TipSignData(id, title, pages, kofiUrl, patreonUrl, ownerUuid, ownerUsername, placedAt, Instant.now(), bgColorIndex);
    }

    public TipSignData withPages(List<String> pages) {
        return new TipSignData(id, title, pages, kofiUrl, patreonUrl, ownerUuid, ownerUsername, placedAt, Instant.now(), bgColorIndex);
    }

    public TipSignData withKofiUrl(String kofiUrl) {
        return new TipSignData(id, title, pages, kofiUrl, patreonUrl, ownerUuid, ownerUsername, placedAt, Instant.now(), bgColorIndex);
    }

    public TipSignData withPatreonUrl(String patreonUrl) {
        return new TipSignData(id, title, pages, kofiUrl, patreonUrl, ownerUuid, ownerUsername, placedAt, Instant.now(), bgColorIndex);
    }

    public TipSignData withLastEditedAt(Instant lastEditedAt) {
        return new TipSignData(id, title, pages, kofiUrl, patreonUrl, ownerUuid, ownerUsername, placedAt, lastEditedAt, bgColorIndex);
    }

    public TipSignData withBgColorIndex(int bgColorIndex) {
        return new TipSignData(id, title, pages, kofiUrl, patreonUrl, ownerUuid, ownerUsername, placedAt, Instant.now(), bgColorIndex);
    }
}
