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
import net.minecraft.network.chat.FormattedText;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;

public class TipSignBlockEntityRenderer implements BlockEntityRenderer<TipSignBlockEntity> {

    private static final int TEXT_COLOR = 0xFF1A1008; // Dark brown (near-black)
    private static final int MAX_TEXT_WIDTH = 1000; // Font-units; ~12px board with margin
    private static final int LINE_HEIGHT = 10; // Font height (~9) + 1px spacing

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
        boolean isWall = state.getValue(TipSignBlock.WALL);

        poseStack.pushPose();

        // Rotate to face the correct direction
        float rotation = switch (facing) {
            case SOUTH -> 0f;
            case WEST -> 90f;
            case NORTH -> 180f;
            case EAST -> 270f;
            default -> 0f;
        };

        if (isWall) {
            // Wall-mounted: board at y=4-12 (center y=8/16=0.5), flush against back wall
            poseStack.translate(0.5, 0.5, 0.5);
            poseStack.mulPose(Axis.YP.rotationDegrees(rotation));
            poseStack.translate(0, 0, 0.186);
        } else {
            // Standing: board at y=7-14 (center y=10.5/16=0.656), post-mounted
            poseStack.translate(0.5, 0.656, 0.5);
            poseStack.mulPose(Axis.YP.rotationDegrees(rotation));
            poseStack.translate(0, 0, -0.377);
        }

        float scale = 0.012f;
        poseStack.scale(-scale, -scale, scale);

        // Word-wrap the title to fit within the sign board
        List<FormattedCharSequence> lines = this.font.split(FormattedText.of(title), MAX_TEXT_WIDTH);
        float totalHeight = lines.size() * LINE_HEIGHT;
        float startY = -totalHeight / 2f;

        for (int i = 0; i < lines.size(); i++) {
            FormattedCharSequence line = lines.get(i);
            int lineWidth = this.font.width(line);
            float x = -lineWidth / 2f;
            float y = startY + i * LINE_HEIGHT;

            this.font.drawInBatch(
                line, x, y, TEXT_COLOR,
                false, poseStack.last().pose(), bufferSource,
                Font.DisplayMode.NORMAL, 0, packedLight
            );
        }

        poseStack.popPose();
    }
}
