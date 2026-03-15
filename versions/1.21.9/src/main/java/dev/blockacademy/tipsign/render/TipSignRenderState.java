package dev.blockacademy.tipsign.render;

import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.core.Direction;

/**
 * Band G (MC 1.21.9–1.21.11) render state for TipSign.
 * Holds extracted data from TipSignBlockEntity for the submit-based renderer.
 */
public class TipSignRenderState extends BlockEntityRenderState {
    public String title = "";
    public Direction facing = Direction.SOUTH;
    public boolean isWall = false;
    public long gameTime = 0;
    public float partialTick = 0;
    public boolean isTargeted = false;
}
