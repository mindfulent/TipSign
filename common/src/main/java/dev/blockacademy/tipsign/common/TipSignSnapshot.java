package dev.blockacademy.tipsign.common;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.time.Instant;
import java.util.List;

/**
 * JSON snapshot model matching US-702 schema for the discovery API.
 */
public record TipSignSnapshot(
    String server_id,
    String generated_at,
    List<SignEntry> signs
) {
    public static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public record SignEntry(
        String id,
        String title,
        String world,
        int x, int y, int z,
        String owner_username,
        String owner_uuid,
        String kofi_url,
        String patreon_url,
        List<String> pages,
        String placed_at,
        String last_edited_at
    ) {
        public static SignEntry from(TipSignData data, String world, int x, int y, int z) {
            return new SignEntry(
                data.id(),
                data.title(),
                world,
                x, y, z,
                data.ownerUsername(),
                data.ownerUuid() != null ? data.ownerUuid().toString() : null,
                data.kofiUrl(),
                data.patreonUrl(),
                data.pages(),
                data.placedAt() != null ? data.placedAt().toString() : null,
                data.lastEditedAt() != null ? data.lastEditedAt().toString() : null
            );
        }
    }

    public static TipSignSnapshot create(String serverId, List<SignEntry> signs) {
        return new TipSignSnapshot(serverId, Instant.now().toString(), signs);
    }

    public String toJson() {
        return GSON.toJson(this);
    }
}
