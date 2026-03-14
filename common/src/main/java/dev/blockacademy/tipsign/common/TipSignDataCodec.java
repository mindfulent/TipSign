package dev.blockacademy.tipsign.common;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * JSON serializer/deserializer for TipSignData used in networking payloads.
 */
public final class TipSignDataCodec {

    private static final Gson GSON = new GsonBuilder().create();

    private TipSignDataCodec() {}

    public static String toJson(TipSignData data) {
        JsonObject obj = new JsonObject();
        obj.addProperty("id", data.id());
        obj.addProperty("title", data.title());

        JsonArray pages = new JsonArray();
        for (String page : data.pages()) {
            pages.add(page);
        }
        obj.add("pages", pages);

        if (data.kofiUrl() != null) obj.addProperty("kofiUrl", data.kofiUrl());
        if (data.patreonUrl() != null) obj.addProperty("patreonUrl", data.patreonUrl());
        if (data.ownerUuid() != null) obj.addProperty("ownerUuid", data.ownerUuid().toString());
        if (data.ownerUsername() != null) obj.addProperty("ownerUsername", data.ownerUsername());
        if (data.placedAt() != null) obj.addProperty("placedAt", data.placedAt().toString());
        if (data.lastEditedAt() != null) obj.addProperty("lastEditedAt", data.lastEditedAt().toString());

        return GSON.toJson(obj);
    }

    public static TipSignData fromJson(String json) {
        JsonObject obj = GSON.fromJson(json, JsonObject.class);

        String id = getStr(obj, "id", UUID.randomUUID().toString());
        String title = getStr(obj, "title", TipSignData.DEFAULT_TITLE);

        List<String> pages = new ArrayList<>();
        if (obj.has("pages")) {
            for (JsonElement el : obj.getAsJsonArray("pages")) {
                pages.add(el.getAsString());
            }
        }
        if (pages.isEmpty()) pages.add("");

        String kofiUrl = obj.has("kofiUrl") ? obj.get("kofiUrl").getAsString() : null;
        String patreonUrl = obj.has("patreonUrl") ? obj.get("patreonUrl").getAsString() : null;

        UUID ownerUuid = obj.has("ownerUuid") ? UUID.fromString(obj.get("ownerUuid").getAsString()) : new UUID(0, 0);
        String ownerUsername = getStr(obj, "ownerUsername", "");

        Instant placedAt = parseInstant(obj, "placedAt");
        Instant lastEditedAt = parseInstant(obj, "lastEditedAt");

        return new TipSignData(id, title, pages, kofiUrl, patreonUrl, ownerUuid, ownerUsername, placedAt, lastEditedAt);
    }

    private static String getStr(JsonObject obj, String key, String def) {
        return obj.has(key) ? obj.get(key).getAsString() : def;
    }

    private static Instant parseInstant(JsonObject obj, String key) {
        if (!obj.has(key)) return null;
        try {
            return Instant.parse(obj.get(key).getAsString());
        } catch (Exception e) {
            return null;
        }
    }
}
