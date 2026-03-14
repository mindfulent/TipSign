package dev.blockacademy.tipsign;

import dev.blockacademy.tipsign.compat.VersionAdapter;
import net.fabricmc.api.ClientModInitializer;

public class TipSignModClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        VersionAdapter.INSTANCE.registerClientPlayReceivers();
    }
}
