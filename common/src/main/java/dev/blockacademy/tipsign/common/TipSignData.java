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
    Instant lastEditedAt
) {
    public static final String DEFAULT_TITLE = "Notice";
    public static final int MAX_TITLE_LENGTH = 32;

    public static TipSignData createNew(UUID ownerUuid, String ownerUsername) {
        Instant now = Instant.now();
        List<String> pages = new ArrayList<>();
        pages.add("");
        return new TipSignData(
            UUID.randomUUID().toString(),
            DEFAULT_TITLE,
            pages,
            null,
            null,
            ownerUuid,
            ownerUsername,
            now,
            now
        );
    }

    public TipSignData withTitle(String title) {
        return new TipSignData(id, title, pages, kofiUrl, patreonUrl, ownerUuid, ownerUsername, placedAt, Instant.now());
    }

    public TipSignData withPages(List<String> pages) {
        return new TipSignData(id, title, pages, kofiUrl, patreonUrl, ownerUuid, ownerUsername, placedAt, Instant.now());
    }

    public TipSignData withKofiUrl(String kofiUrl) {
        return new TipSignData(id, title, pages, kofiUrl, patreonUrl, ownerUuid, ownerUsername, placedAt, Instant.now());
    }

    public TipSignData withPatreonUrl(String patreonUrl) {
        return new TipSignData(id, title, pages, kofiUrl, patreonUrl, ownerUuid, ownerUsername, placedAt, Instant.now());
    }

    public TipSignData withLastEditedAt(Instant lastEditedAt) {
        return new TipSignData(id, title, pages, kofiUrl, patreonUrl, ownerUuid, ownerUsername, placedAt, lastEditedAt);
    }
}
