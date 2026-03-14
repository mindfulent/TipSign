package dev.blockacademy.tipsign.common;

import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import com.electronwill.nightconfig.core.file.FileConfig;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.UUID;

/**
 * Server-side TOML configuration parsed by NightConfig.
 */
public class TipSignConfig {

    private static TipSignConfig INSTANCE;

    // Block behavior
    private boolean ownerOnlyBreak = true;
    private boolean requireConfirmBeforeBrowserOpen = true;
    private int maxPages = 10;
    private boolean craftingEnabled = true;

    // URL settings
    private List<String> allowedUrlSchemes = List.of("https");
    private boolean allowInlineLinks = true;
    private List<String> allowedLinkDomains = List.of("ko-fi.com", "patreon.com");

    // Discovery API
    private boolean discoveryEnabled = true;
    private int discoveryIntervalSeconds = 300;
    private String serverId = "";
    private String webhookUrl = "";
    private String webhookSecret = "";

    private TipSignConfig() {}

    public static TipSignConfig get() {
        if (INSTANCE == null) {
            INSTANCE = new TipSignConfig();
        }
        return INSTANCE;
    }

    public static void load(Path configDir) {
        TipSignConfig config = new TipSignConfig();
        Path configPath = configDir.resolve("tipsign.toml");

        boolean firstLaunch = !Files.exists(configPath);

        try {
            Files.createDirectories(configDir);
        } catch (Exception e) {
            warn("Failed to create config directory: " + e.getMessage());
        }

        try (CommentedFileConfig fileConfig = CommentedFileConfig.builder(configPath)
                .autosave()
                .preserveInsertionOrder()
                .build()) {

            fileConfig.load();

            // Block behavior
            config.ownerOnlyBreak = getBoolean(fileConfig, "ownerOnlyBreak", true);
            fileConfig.setComment("ownerOnlyBreak", " Whether only the owner can break a placed Tip Sign");
            fileConfig.set("ownerOnlyBreak", config.ownerOnlyBreak);

            config.requireConfirmBeforeBrowserOpen = getBoolean(fileConfig, "requireConfirmBeforeBrowserOpen", true);
            fileConfig.setComment("requireConfirmBeforeBrowserOpen", " Show confirmation dialog before opening external URLs in browser");
            fileConfig.set("requireConfirmBeforeBrowserOpen", config.requireConfirmBeforeBrowserOpen);

            config.maxPages = getInt(fileConfig, "maxPages", 10, 1, 50);
            fileConfig.setComment("maxPages", " Maximum number of pages per sign (1-50)");
            fileConfig.set("maxPages", config.maxPages);

            config.craftingEnabled = getBoolean(fileConfig, "craftingEnabled", true);
            fileConfig.setComment("craftingEnabled", " Whether the crafting recipe is enabled (false = /give only)");
            fileConfig.set("craftingEnabled", config.craftingEnabled);

            // URL settings
            config.allowedUrlSchemes = getStringList(fileConfig, "allowedUrlSchemes", List.of("https"));
            fileConfig.setComment("allowedUrlSchemes", " Allowed URL schemes for links");
            fileConfig.set("allowedUrlSchemes", config.allowedUrlSchemes);

            config.allowInlineLinks = getBoolean(fileConfig, "allowInlineLinks", true);
            fileConfig.setComment("allowInlineLinks", " Whether inline [text](url) hyperlinks are clickable");
            fileConfig.set("allowInlineLinks", config.allowInlineLinks);

            config.allowedLinkDomains = getStringList(fileConfig, "allowedLinkDomains", List.of("ko-fi.com", "patreon.com"));
            fileConfig.setComment("allowedLinkDomains", " Whitelist of permitted domains for supporter buttons and inline links");
            fileConfig.set("allowedLinkDomains", config.allowedLinkDomains);

            // Discovery API
            config.discoveryEnabled = getBoolean(fileConfig, "discoveryEnabled", true);
            fileConfig.setComment("discoveryEnabled", " Enable/disable all discovery output (file + webhook)");
            fileConfig.set("discoveryEnabled", config.discoveryEnabled);

            config.discoveryIntervalSeconds = getInt(fileConfig, "discoveryIntervalSeconds", 300, 60, 3600);
            fileConfig.setComment("discoveryIntervalSeconds", " Background snapshot write interval in seconds (60-3600)");
            fileConfig.set("discoveryIntervalSeconds", config.discoveryIntervalSeconds);

            String sid = getString(fileConfig, "serverId", "");
            if (sid.isEmpty()) {
                sid = UUID.randomUUID().toString();
            }
            config.serverId = sid;
            fileConfig.setComment("serverId", " Stable server ID (auto-generated UUID on first launch, do not edit)");
            fileConfig.set("serverId", config.serverId);

            config.webhookUrl = getString(fileConfig, "webhookUrl", "");
            fileConfig.setComment("webhookUrl", " Webhook URL for push notifications (empty = disabled)");
            fileConfig.set("webhookUrl", config.webhookUrl);

            config.webhookSecret = getString(fileConfig, "webhookSecret", "");
            fileConfig.setComment("webhookSecret", " HMAC-SHA256 secret for webhook signature (empty = unsigned)");
            fileConfig.set("webhookSecret", config.webhookSecret);
        }

        INSTANCE = config;
    }

    // --- Accessors ---

    public boolean ownerOnlyBreak() { return ownerOnlyBreak; }
    public boolean requireConfirmBeforeBrowserOpen() { return requireConfirmBeforeBrowserOpen; }
    public int maxPages() { return maxPages; }
    public boolean craftingEnabled() { return craftingEnabled; }
    public List<String> allowedUrlSchemes() { return allowedUrlSchemes; }
    public boolean allowInlineLinks() { return allowInlineLinks; }
    public List<String> allowedLinkDomains() { return allowedLinkDomains; }
    public boolean discoveryEnabled() { return discoveryEnabled; }
    public int discoveryIntervalSeconds() { return discoveryIntervalSeconds; }
    public String serverId() { return serverId; }
    public String webhookUrl() { return webhookUrl; }
    public String webhookSecret() { return webhookSecret; }

    // --- Config Helpers ---

    private static boolean getBoolean(FileConfig config, String key, boolean defaultValue) {
        try {
            Object val = config.get(key);
            if (val instanceof Boolean b) return b;
            if (val != null) warn("Invalid boolean for '" + key + "', using default: " + defaultValue);
        } catch (Exception e) {
            warn("Error reading '" + key + "': " + e.getMessage());
        }
        return defaultValue;
    }

    private static int getInt(FileConfig config, String key, int defaultValue, int min, int max) {
        try {
            Object val = config.get(key);
            if (val instanceof Number n) {
                int v = n.intValue();
                if (v < min || v > max) {
                    warn("'" + key + "' value " + v + " out of range [" + min + "," + max + "], using default: " + defaultValue);
                    return defaultValue;
                }
                return v;
            }
            if (val != null) warn("Invalid integer for '" + key + "', using default: " + defaultValue);
        } catch (Exception e) {
            warn("Error reading '" + key + "': " + e.getMessage());
        }
        return defaultValue;
    }

    private static String getString(FileConfig config, String key, String defaultValue) {
        try {
            Object val = config.get(key);
            if (val instanceof String s) return s;
            if (val != null) warn("Invalid string for '" + key + "', using default");
        } catch (Exception e) {
            warn("Error reading '" + key + "': " + e.getMessage());
        }
        return defaultValue;
    }

    @SuppressWarnings("unchecked")
    private static List<String> getStringList(FileConfig config, String key, List<String> defaultValue) {
        try {
            Object val = config.get(key);
            if (val instanceof List<?> list) {
                return (List<String>) list.stream()
                    .filter(e -> e instanceof String)
                    .map(e -> (String) e)
                    .toList();
            }
            if (val != null) warn("Invalid list for '" + key + "', using default");
        } catch (Exception e) {
            warn("Error reading '" + key + "': " + e.getMessage());
        }
        return defaultValue;
    }

    private static void warn(String message) {
        System.err.println("[TipSign Config] WARNING: " + message);
    }
}
