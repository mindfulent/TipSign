package dev.blockacademy.tipsign.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import dev.blockacademy.tipsign.block.TipSignBlock;
import dev.blockacademy.tipsign.block.TipSignBlockEntity;
import dev.blockacademy.tipsign.common.TipSignData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.List;

/**
 * Band F (MC 1.21.6–1.21.8) TipSignBlockEntityRenderer.
 * render() signature gains Vec3 parameter in 1.21.6.
 */
public class TipSignBlockEntityRenderer implements BlockEntityRenderer<TipSignBlockEntity> {

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
        this.font = ctx.getFont();
    }

    @Override
    public void render(TipSignBlockEntity be, float partialTick, PoseStack poseStack,
                       MultiBufferSource bufferSource, int packedLight, int packedOverlay,
                       Vec3 cameraPos) {
        TipSignData data = be.getData();
        if (data == null) return;

        String title = data.title() != null ? data.title() : TipSignData.DEFAULT_TITLE;
        if (title.isEmpty()) return;

        BlockState state = be.getBlockState();
        Direction facing = state.getValue(TipSignBlock.FACING);
        boolean isWall = state.getValue(TipSignBlock.WALL);

        poseStack.pushPose();

        if (isWall) {
            // Axis.YP rotates CCW (right-hand rule), blockstate "y" rotates CW,
            // so EAST/WEST are swapped vs the blockstate values.
            float rotation = switch (facing) {
                case SOUTH -> 0f;
                case WEST -> 270f;
                case NORTH -> 180f;
                case EAST -> 90f;
                default -> 0f;
            };
            poseStack.translate(0.5, 0.490, 0.5);
            poseStack.mulPose(Axis.YP.rotationDegrees(rotation));
            poseStack.translate(0, 0, 0.436);
        } else {
            // Axis.YP rotates CCW (right-hand rule), blockstate "y" rotates CW,
            // so EAST/WEST are swapped vs the blockstate values.
            float rotation = switch (facing) {
                case NORTH -> 0f;
                case EAST -> 270f;
                case SOUTH -> 180f;
                case WEST -> 90f;
                default -> 0f;
            };
            poseStack.translate(0.5, 0.646, 0.5);
            poseStack.mulPose(Axis.YP.rotationDegrees(rotation));
            poseStack.translate(0, 0, -0.132);
        }

        float scale = 0.012f;
        poseStack.scale(-scale, -scale, scale);

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

        // Render "Right-click me!" indicator when crosshair targets this sign
        HitResult hitResult = Minecraft.getInstance().hitResult;
        if (hitResult instanceof BlockHitResult blockHit
                && blockHit.getBlockPos().equals(be.getBlockPos())
                && be.getLevel() != null) {

            long gameTime = be.getLevel().getGameTime();
            float pulse = (float) (Math.sin((gameTime + partialTick) * 0.15) * 0.5 + 0.5);
            int alpha = 160 + (int) (pulse * 95); // 160–255: always readable
            int goldColor = (alpha << 24) | 0xFF4444;
            int redColor = (alpha << 24) | 0xFF4444;
            int fullBright = 0xF000F0;

            float indicatorY = isWall ? 0.85f : 1.25f;
            float zOffset = isWall ? 0.436f : -0.132f;
            float rotation;
            if (isWall) {
                rotation = switch (facing) {
                    case SOUTH -> 0f;
                    case WEST -> 270f;
                    case NORTH -> 180f;
                    case EAST -> 90f;
                    default -> 0f;
                };
            } else {
                rotation = switch (facing) {
                    case NORTH -> 0f;
                    case EAST -> 270f;
                    case SOUTH -> 180f;
                    case WEST -> 90f;
                    default -> 0f;
                };
            }

            poseStack.pushPose();
            poseStack.translate(0.5, indicatorY, 0.5);
            poseStack.mulPose(Axis.YP.rotationDegrees(rotation));
            poseStack.translate(0, 0, zOffset);
            poseStack.scale(-scale, -scale, scale);

            String text = "Right-click me!";
            int textWidth = this.font.width(text);
            this.font.drawInBatch(text, -textWidth / 2f, 0, goldColor,
                    false, poseStack.last().pose(), bufferSource,
                    Font.DisplayMode.NORMAL, 0, fullBright);

            String arrow = "\u25BC";
            int arrowWidth = this.font.width(arrow);
            this.font.drawInBatch(arrow, -arrowWidth / 2f, 10, redColor,
                    false, poseStack.last().pose(), bufferSource,
                    Font.DisplayMode.NORMAL, 0, fullBright);

            poseStack.popPose();
        }
    }
}
