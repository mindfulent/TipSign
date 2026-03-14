package dev.blockacademy.tipsign;

import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TipSignMod implements ModInitializer {
    public static final String MOD_ID = "tipsign";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        LOGGER.info("TipSign loaded");
    }
}
