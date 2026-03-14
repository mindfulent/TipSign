package dev.blockacademy.tipsign.common;

import java.net.URI;
import java.util.List;

/**
 * URL validation with scheme whitelist, domain whitelist, and Ko-fi/Patreon URL construction.
 */
public final class UrlValidator {

    private UrlValidator() {}

    /**
     * Validates a URL against allowed schemes and domains.
     * Returns null if invalid, the validated URL if valid.
     */
    public static String validate(String url, List<String> allowedSchemes, List<String> allowedDomains) {
        if (url == null || url.isBlank()) return null;

        try {
            URI uri = URI.create(url.trim());
            String scheme = uri.getScheme();
            String host = uri.getHost();

            if (scheme == null || host == null) return null;

            if (!allowedSchemes.contains(scheme.toLowerCase())) return null;

            // Check domain against whitelist (match domain or subdomain)
            String lowerHost = host.toLowerCase();
            boolean domainAllowed = allowedDomains.stream().anyMatch(domain -> {
                String d = domain.toLowerCase();
                return lowerHost.equals(d) || lowerHost.endsWith("." + d);
            });

            if (!domainAllowed) return null;

            return uri.toString();
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Constructs a Ko-fi URL from a username or validates a full URL.
     * Accepts: "username", "ko-fi.com/username", "https://ko-fi.com/username"
     */
    public static String toKofiUrl(String input) {
        if (input == null || input.isBlank()) return null;
        String trimmed = input.trim();

        // Already a full URL
        if (trimmed.startsWith("https://ko-fi.com/") || trimmed.startsWith("https://www.ko-fi.com/")) {
            return trimmed;
        }
        if (trimmed.startsWith("http://")) return null; // Reject http

        // Domain + path
        if (trimmed.startsWith("ko-fi.com/") || trimmed.startsWith("www.ko-fi.com/")) {
            return "https://" + trimmed;
        }

        // Just a username — no slashes or dots allowed
        if (!trimmed.contains("/") && !trimmed.contains(".") && !trimmed.contains(" ")) {
            return "https://ko-fi.com/" + trimmed;
        }

        return null;
    }

    /**
     * Constructs a Patreon URL from a username or validates a full URL.
     * Accepts: "username", "patreon.com/username", "https://www.patreon.com/username"
     */
    public static String toPatreonUrl(String input) {
        if (input == null || input.isBlank()) return null;
        String trimmed = input.trim();

        // Already a full URL
        if (trimmed.startsWith("https://www.patreon.com/") || trimmed.startsWith("https://patreon.com/")) {
            return trimmed;
        }
        if (trimmed.startsWith("http://")) return null;

        // Domain + path
        if (trimmed.startsWith("patreon.com/") || trimmed.startsWith("www.patreon.com/")) {
            return "https://www." + trimmed.replaceFirst("^www\\.", "");
        }

        // Just a username
        if (!trimmed.contains("/") && !trimmed.contains(".") && !trimmed.contains(" ")) {
            return "https://www.patreon.com/" + trimmed;
        }

        return null;
    }
}
