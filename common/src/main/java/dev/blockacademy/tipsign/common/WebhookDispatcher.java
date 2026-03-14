package dev.blockacademy.tipsign.common;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.HexFormat;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

/**
 * Async HTTP POST with HMAC-SHA256 signing and 3-retry exponential backoff.
 */
public class WebhookDispatcher {

    private static final int MAX_RETRIES = 3;
    private static final int[] BACKOFF_MS = {2000, 4000, 8000};

    private final HttpClient httpClient;
    private final Executor executor;

    public WebhookDispatcher(Executor executor) {
        this.executor = executor;
        this.httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .executor(executor)
            .build();
    }

    /**
     * Asynchronously POSTs the JSON payload to the webhook URL.
     * Fire-and-forget — logs warnings on failure, never blocks the game thread.
     */
    public void push(String webhookUrl, String jsonPayload, String serverId, String webhookSecret) {
        if (webhookUrl == null || webhookUrl.isBlank()) return;

        CompletableFuture.runAsync(() -> {
            for (int attempt = 0; attempt <= MAX_RETRIES; attempt++) {
                try {
                    HttpRequest.Builder builder = HttpRequest.newBuilder()
                        .uri(URI.create(webhookUrl))
                        .header("Content-Type", "application/json")
                        .header("X-TipSign-Server-ID", serverId)
                        .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
                        .timeout(Duration.ofSeconds(15));

                    if (webhookSecret != null && !webhookSecret.isBlank()) {
                        String signature = computeHmacSha256(jsonPayload, webhookSecret);
                        builder.header("X-TipSign-Signature", "sha256=" + signature);
                    }

                    HttpResponse<String> response = httpClient.send(builder.build(),
                        HttpResponse.BodyHandlers.ofString());

                    int status = response.statusCode();
                    if (status >= 200 && status < 300) {
                        return; // Success
                    }
                    if (status == 401) {
                        warn("Webhook returned 401 Unauthorized — check webhookSecret");
                        return; // Don't retry auth failures
                    }
                    if (status >= 500 && attempt < MAX_RETRIES) {
                        warn("Webhook returned " + status + ", retrying in " + BACKOFF_MS[attempt] + "ms...");
                        Thread.sleep(BACKOFF_MS[attempt]);
                        continue;
                    }
                    warn("Webhook returned " + status + " after " + (attempt + 1) + " attempt(s)");
                    return;
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return;
                } catch (Exception e) {
                    if (attempt < MAX_RETRIES) {
                        warn("Webhook failed: " + e.getMessage() + ", retrying in " + BACKOFF_MS[attempt] + "ms...");
                        try { Thread.sleep(BACKOFF_MS[attempt]); } catch (InterruptedException ie) {
                            Thread.currentThread().interrupt();
                            return;
                        }
                    } else {
                        warn("Webhook failed after " + (attempt + 1) + " attempts: " + e.getMessage());
                    }
                }
            }
        }, executor);
    }

    static String computeHmacSha256(String data, String secret) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec keySpec = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            mac.init(keySpec);
            byte[] hash = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (Exception e) {
            throw new RuntimeException("Failed to compute HMAC-SHA256", e);
        }
    }

    private static void warn(String message) {
        System.err.println("[TipSign Webhook] WARNING: " + message);
    }
}
