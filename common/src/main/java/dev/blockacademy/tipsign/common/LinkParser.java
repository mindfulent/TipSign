package dev.blockacademy.tipsign.common;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parses [text](url) markdown-style inline links from body text.
 */
public final class LinkParser {

    private static final Pattern LINK_PATTERN = Pattern.compile("\\[([^\\]]+)]\\(([^)]+)\\)");

    private LinkParser() {}

    public record ParsedLink(String text, String url, int startIndex, int endIndex) {}

    /**
     * Extracts all [text](url) links from input text.
     */
    public static List<ParsedLink> extractLinks(String text) {
        List<ParsedLink> links = new ArrayList<>();
        if (text == null) return links;

        Matcher matcher = LINK_PATTERN.matcher(text);
        while (matcher.find()) {
            links.add(new ParsedLink(
                matcher.group(1),
                matcher.group(2),
                matcher.start(),
                matcher.end()
            ));
        }
        return links;
    }

    /**
     * Validates all links in text against allowed schemes and domains.
     * Returns list of invalid link URLs.
     */
    public static List<String> findInvalidLinks(String text, List<String> allowedSchemes, List<String> allowedDomains) {
        List<String> invalid = new ArrayList<>();
        for (ParsedLink link : extractLinks(text)) {
            if (UrlValidator.validate(link.url(), allowedSchemes, allowedDomains) == null) {
                invalid.add(link.url());
            }
        }
        return invalid;
    }

    /**
     * Strips link markdown syntax, returning plain text with link text inline.
     */
    public static String stripLinks(String text) {
        if (text == null) return null;
        return LINK_PATTERN.matcher(text).replaceAll("$1");
    }
}
