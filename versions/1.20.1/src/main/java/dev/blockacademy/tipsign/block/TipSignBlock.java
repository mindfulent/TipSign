package dev.blockacademy.tipsign.block;

import dev.blockacademy.tipsign.TipSignMod;
import dev.blockacademy.tipsign.TipSignPermissions;
import dev.blockacademy.tipsign.common.TipSignConfig;
import dev.blockacademy.tipsign.common.TipSignData;
import dev.blockacademy.tipsign.compat.VersionAdapter;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

public class TipSignBlock extends BaseEntityBlock {

    // No CODEC field needed in 1.20.1 (block codecs not required)
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final BooleanProperty WALL = BooleanProperty.create("wall");

    // Standing sign sub-shapes (from model JSON coordinates)
    private static final VoxelShape POST = Block.box(7, 0, 7, 9, 16, 9);
    private static final VoxelShape CAP  = Block.box(6, 14, 6, 10, 16, 10);

    // Board per facing (14x7x1, rotated around block center)
    private static final VoxelShape BOARD_NORTH = Block.box(1, 7, 6, 15, 14, 7);
    private static final VoxelShape BOARD_SOUTH = Block.box(1, 7, 9, 15, 14, 10);
    private static final VoxelShape BOARD_EAST  = Block.box(9, 7, 1, 10, 14, 15);
    private static final VoxelShape BOARD_WEST  = Block.box(6, 7, 1, 7, 14, 15);

    // Compound standing shapes
    private static final VoxelShape STANDING_NORTH = Shapes.or(POST, CAP, BOARD_NORTH);
    private static final VoxelShape STANDING_SOUTH = Shapes.or(POST, CAP, BOARD_SOUTH);
    private static final VoxelShape STANDING_EAST  = Shapes.or(POST, CAP, BOARD_EAST);
    private static final VoxelShape STANDING_WEST  = Shapes.or(POST, CAP, BOARD_WEST);

    // Wall-mounted shapes (thin board flush against each wall)
    private static final VoxelShape WALL_NORTH = Block.box(1, 4, 15, 15, 12, 16);
    private static final VoxelShape WALL_SOUTH = Block.box(1, 4, 0, 15, 12, 1);
    private static final VoxelShape WALL_EAST  = Block.box(0, 4, 1, 1, 12, 15);
    private static final VoxelShape WALL_WEST  = Block.box(15, 4, 1, 16, 12, 15);

    public TipSignBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any()
            .setValue(FACING, Direction.NORTH)
            .setValue(WALL, false));
    }

    // No codec() override needed in 1.20.1

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, WALL);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext ctx) {
        Direction clickedFace = ctx.getClickedFace();
        if (clickedFace.getAxis().isHorizontal()) {
            BlockPos behind = ctx.getClickedPos().relative(clickedFace.getOpposite());
            if (ctx.getLevel().getBlockState(behind).isFaceSturdy(ctx.getLevel(), behind, clickedFace)) {
                return this.defaultBlockState()
                    .setValue(FACING, clickedFace)
                    .setValue(WALL, true);
            }
        }
        return this.defaultBlockState()
            .setValue(FACING, ctx.getHorizontalDirection().getOpposite())
            .setValue(WALL, false);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext ctx) {
        if (state.getValue(WALL)) {
            return switch (state.getValue(FACING)) {
                case NORTH -> WALL_NORTH;
                case SOUTH -> WALL_SOUTH;
                case EAST -> WALL_EAST;
                case WEST -> WALL_WEST;
                default -> STANDING_NORTH;
            };
        }
        return switch (state.getValue(FACING)) {
            case NORTH -> STANDING_NORTH;
            case SOUTH -> STANDING_SOUTH;
            case EAST  -> STANDING_EAST;
            case WEST  -> STANDING_WEST;
            default    -> STANDING_NORTH;
        };
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    // 1.20.1: piston push reaction set via properties if available, or property on block
    @SuppressWarnings("deprecation")
    public PushReaction getPistonPushReaction(BlockState state) {
        return PushReaction.BLOCK;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new TipSignBlockEntity(pos, state);
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        if (level.isClientSide()) return;

        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof TipSignBlockEntity tipSign && placer instanceof ServerPlayer player) {
            // Check if item has stored data (picked-up sign)
            TipSignData existingData = VersionAdapter.INSTANCE.readFromItemStack(stack);
            if (existingData != null) {
                tipSign.setData(existingData);
            } else {
                tipSign.setData(TipSignData.createNew(
                    player.getUUID(),
                    player.getGameProfile().getName()
                ));
            }

            // Open Author UI for newly placed signs
            VersionAdapter.INSTANCE.sendOpenSignToClient(player, pos, tipSign.getData(), true);
        }
    }

    // 1.20.1: use() instead of useWithoutItem(), public not protected, takes InteractionHand
    @SuppressWarnings("deprecation")
    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }

        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof TipSignBlockEntity tipSign && player instanceof ServerPlayer serverPlayer) {
            boolean isOwner = player.getUUID().equals(tipSign.getData().ownerUuid());
            boolean isAdmin = player.hasPermissions(2);
            boolean sneaking = player.isShiftKeyDown();

            if (sneaking && (isOwner || isAdmin)) {
                // Owner/admin shift+right-click -> Author UI
                VersionAdapter.INSTANCE.sendOpenSignToClient(serverPlayer, pos, tipSign.getData(), true);
            } else if (sneaking && !isOwner && !isAdmin) {
                // Non-owner shift+right-click -> denied
                serverPlayer.sendSystemMessage(Component.literal("You are not the owner of this Tip Sign."));
            } else {
                // Normal right-click -> Reader UI
                VersionAdapter.INSTANCE.sendOpenSignToClient(serverPlayer, pos, tipSign.getData(), false);
            }
            return InteractionResult.CONSUME;
        }

        return InteractionResult.PASS;
    }

    @Override
    public void playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        if (!level.isClientSide() && player instanceof ServerPlayer serverPlayer) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof TipSignBlockEntity tipSign) {
                if (TipSignConfig.get().ownerOnlyBreak()
                    && !TipSignPermissions.canBreak(serverPlayer, tipSign)) {
                    serverPlayer.sendSystemMessage(Component.translatable("tipsign.message.not_owner"));
                    super.playerWillDestroy(level, pos, state, player);
                    return;
                }

                // Manual drop — loot table is intentionally empty to prevent duplication
                ItemStack drop = new ItemStack(TipSignMod.SIGN_POST_ITEM);
                VersionAdapter.INSTANCE.writeToItemStack(drop, tipSign.getData());
                VersionAdapter.INSTANCE.setItemTooltipTitle(drop, tipSign.getData().title());
                Block.popResource(level, pos, drop);
            }
        }
        super.playerWillDestroy(level, pos, state, player);
    }
}
