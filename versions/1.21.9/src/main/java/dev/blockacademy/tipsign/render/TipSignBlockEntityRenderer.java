package dev.blockacademy.tipsign.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import dev.blockacademy.tipsign.block.TipSignBlock;
import dev.blockacademy.tipsign.block.TipSignBlockEntity;
import dev.blockacademy.tipsign.common.TipSignData;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.phys.Vec3;

import java.util.List;

/**
 * Band G (MC 1.21.9–1.21.11) TipSignBlockEntityRenderer.
 * Uses submit-based rendering with TipSignRenderState.
 */
public class TipSignBlockEntityRenderer implements BlockEntityRenderer<TipSignBlockEntity, TipSignRenderState> {

    private static final int TEXT_COLOR = 0xFF1A1008; // Dark brown (near-black)
    private static final int MAX_TEXT_WIDTH = 65; // Font-units; board is ~73 at scale 0.012
    private static final int LINE_HEIGHT = 10; // Font height (~9) + 1px spacing

    // Debug tuning — uncomment keybinds in TipSignModClient to re-enable
    // public static float debugRotOffset = 0f;
    // public static float debugZStanding = -0.132f;
    // public static float debugZWall = 0.436f;
    // public static float debugYStanding = 0.646f;
    // public static float debugYWall = 0.490f;
    // public static int debugMode = 0;

    private final Font font;

    public TipSignBlockEntityRenderer(BlockEntityRendererProvider.Context ctx) {
        this.font = ctx.font();
    }

    @Override
    public TipSignRenderState createRenderState() {
        return new TipSignRenderState();
    }

    @Override
    public void extractRenderState(TipSignBlockEntity be, TipSignRenderState state, float partialTick,
                                   Vec3 cameraPos, ModelFeatureRenderer.CrumblingOverlay crumbling) {
        BlockEntityRenderState.extractBase(be, state, crumbling);
        TipSignData data = be.getData();
        state.title = (data != null && data.title() != null) ? data.title() : TipSignData.DEFAULT_TITLE;
        state.facing = be.getBlockState().getValue(TipSignBlock.FACING);
        state.isWall = be.getBlockState().getValue(TipSignBlock.WALL);
    }

    @Override
    public void submit(TipSignRenderState state, PoseStack poseStack,
                       SubmitNodeCollector collector, CameraRenderState cameraRenderState) {
        if (state.title.isEmpty()) return;

        poseStack.pushPose();

        if (state.isWall) {
            float rotation = switch (state.facing) {
                case SOUTH -> 0f;
                case WEST -> 90f;
                case NORTH -> 180f;
                case EAST -> 270f;
                default -> 0f;
            };
            poseStack.translate(0.5, 0.490, 0.5);
            poseStack.mulPose(Axis.YP.rotationDegrees(rotation));
            poseStack.translate(0, 0, 0.436);
        } else {
            float rotation = switch (state.facing) {
                case NORTH -> 0f;
                case EAST -> 90f;
                case SOUTH -> 180f;
                case WEST -> 270f;
                default -> 0f;
            };
            poseStack.translate(0.5, 0.646, 0.5);
            poseStack.mulPose(Axis.YP.rotationDegrees(rotation));
            poseStack.translate(0, 0, -0.132);
        }

        float scale = 0.012f;
        poseStack.scale(-scale, -scale, scale);

        // Word-wrap the title to fit within the sign board
        List<FormattedCharSequence> lines = this.font.split(FormattedText.of(state.title), MAX_TEXT_WIDTH);
        float totalHeight = lines.size() * LINE_HEIGHT;
        float startY = -totalHeight / 2f;

        for (int i = 0; i < lines.size(); i++) {
            FormattedCharSequence line = lines.get(i);
            int lineWidth = this.font.width(line);
            float x = -lineWidth / 2f;
            float y = startY + i * LINE_HEIGHT;

            // submitText(PoseStack, x, y, text, shadow, displayMode, light, color, bgColor, outlineColor)
            collector.submitText(poseStack, x, y, line, false,
                Font.DisplayMode.NORMAL, state.lightCoords, TEXT_COLOR, 0, 0);
        }

        poseStack.popPose();
    }
}
