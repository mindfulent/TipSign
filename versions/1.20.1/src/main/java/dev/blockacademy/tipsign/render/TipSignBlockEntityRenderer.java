package dev.blockacademy.tipsign.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import dev.blockacademy.tipsign.block.TipSignBlock;
import dev.blockacademy.tipsign.block.TipSignBlockEntity;
import dev.blockacademy.tipsign.common.TipSignData;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;

public class TipSignBlockEntityRenderer implements BlockEntityRenderer<TipSignBlockEntity> {

    private final Font font;

    public TipSignBlockEntityRenderer(BlockEntityRendererProvider.Context ctx) {
        this.font = ctx.getFont();
    }

    @Override
    public void render(TipSignBlockEntity be, float partialTick, PoseStack poseStack,
                       MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        TipSignData data = be.getData();
        if (data == null) return;

        String title = data.title() != null ? data.title() : TipSignData.DEFAULT_TITLE;
        if (title.isEmpty()) return;

        BlockState state = be.getBlockState();
        Direction facing = state.getValue(TipSignBlock.FACING);

        poseStack.pushPose();

        // Position text centered on the sign board face
        // Board is at y=7-14 (center y=10.5/16 = 0.656), z=6 on north face
        poseStack.translate(0.5, 0.656, 0.5);

        // Rotate to face the correct direction
        float rotation = switch (facing) {
            case SOUTH -> 0f;
            case WEST -> 90f;
            case NORTH -> 180f;
            case EAST -> 270f;
            default -> 0f;
        };
        poseStack.mulPose(Axis.YP.rotationDegrees(rotation));

        // Move forward to board face (z=6/16 = 0.375 from center)
        poseStack.translate(0, 0, -0.377);
        float scale = 0.012f;
        poseStack.scale(-scale, -scale, scale);

        // Center the title text
        int textWidth = this.font.width(title);
        float x = -textWidth / 2f;

        this.font.drawInBatch(
            title, x, 0, 0xFFEEDDCC,
            false, poseStack.last().pose(), bufferSource,
            Font.DisplayMode.NORMAL, 0, packedLight
        );

        poseStack.popPose();
    }
}
