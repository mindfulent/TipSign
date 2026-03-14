package dev.blockacademy.tipsign.discovery;

import dev.blockacademy.tipsign.TipSignMod;
import dev.blockacademy.tipsign.block.TipSignBlockEntity;
import dev.blockacademy.tipsign.common.*;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Bridges block entity events to snapshot file writes and webhook pushes.
 * Runs on a dedicated ScheduledExecutorService with 5-second debounce.
 */
public class DiscoveryManager {

    private static DiscoveryManager INSTANCE;

    private final ScheduledExecutorService executor;
    private final DebounceTimer debounce;
    private final WebhookDispatcher webhookDispatcher;
    private final ConcurrentHashMap<BlockPos, TipSignData> trackedSigns = new ConcurrentHashMap<>();
    private MinecraftServer server;
    private Path snapshotPath;

    private DiscoveryManager() {
        this.executor = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "TipSign-Discovery");
            t.setDaemon(true);
            return t;
        });
        this.debounce = new DebounceTimer(executor, 5000);
        this.webhookDispatcher = new WebhookDispatcher(executor);
    }

    public static DiscoveryManager get() {
        if (INSTANCE == null) {
            INSTANCE = new DiscoveryManager();
        }
        return INSTANCE;
    }

    /**
     * Initialize with server reference for collecting signs across worlds.
     */
    public void init(MinecraftServer server, Path configDir) {
        TipSignConfig config = TipSignConfig.get();
        if (!config.discoveryEnabled()) return;

        this.snapshotPath = configDir.resolve("tipsign").resolve("tipsigns.json");
        this.server = server;

        // Start background interval timer
        int interval = config.discoveryIntervalSeconds();
        executor.scheduleAtFixedRate(this::writeSnapshot, interval, interval, TimeUnit.SECONDS);

        if (!config.webhookUrl().isEmpty()) {
            TipSignMod.LOGGER.info("TipSign webhook configured -> {}", config.webhookUrl());
        }

        // Write initial snapshot
        executor.schedule(this::writeSnapshot, 5, TimeUnit.SECONDS);
    }

    /**
     * Track a sign for discovery snapshot collection.
     */
    public void trackSign(BlockPos pos, TipSignData data) {
        trackedSigns.put(pos, data);
    }

    /**
     * Remove a sign from tracking (e.g., when broken).
     */
    public void untrackSign(BlockPos pos) {
        trackedSigns.remove(pos);
    }

    /**
     * Called when a sign is placed, edited, deleted, or broken.
     * Debounces rapid edits into a single snapshot write.
     */
    public void onSignChanged() {
        if (!TipSignConfig.get().discoveryEnabled()) return;
        debounce.schedule(this::writeSnapshot);
    }

    private void writeSnapshot() {
        if (server == null || snapshotPath == null) return;

        try {
            TipSignConfig config = TipSignConfig.get();
            List<TipSignSnapshot.SignEntry> entries = collectSigns(server);
            TipSignSnapshot snapshot = TipSignSnapshot.create(config.serverId(), entries);
            String json = snapshot.toJson();

            // Write to disk
            SnapshotWriter.writeAtomically(snapshotPath, json);

            // Push to webhook if configured
            if (!config.webhookUrl().isEmpty()) {
                webhookDispatcher.push(config.webhookUrl(), json, config.serverId(), config.webhookSecret());
            }
        } catch (Exception e) {
            TipSignMod.LOGGER.warn("Failed to write TipSign snapshot: {}", e.getMessage());
        }
    }

    private List<TipSignSnapshot.SignEntry> collectSigns(MinecraftServer server) {
        List<TipSignSnapshot.SignEntry> entries = new ArrayList<>();

        // Collect signs from all loaded worlds
        // Note: we can only access loaded chunks' block entities
        for (ServerLevel level : server.getAllLevels()) {
            String worldId = level.dimension().location().toString();
            // Use tracked signs from our registry
            for (var entry : trackedSigns.entrySet()) {
                BlockPos pos = entry.getKey();
                TipSignData data = entry.getValue();
                if (data != null) {
                    entries.add(TipSignSnapshot.SignEntry.from(data, worldId, pos.getX(), pos.getY(), pos.getZ()));
                }
            }
        }

        return entries;
    }

    /**
     * Graceful shutdown on server stop.
     */
    public void shutdown() {
        debounce.cancel();
        executor.shutdown();
        try {
            executor.awaitTermination(5, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        INSTANCE = null;
    }
}
