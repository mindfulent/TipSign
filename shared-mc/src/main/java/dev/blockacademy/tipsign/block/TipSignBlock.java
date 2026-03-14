package dev.blockacademy.tipsign.block;

import dev.blockacademy.tipsign.TipSignMod;
import dev.blockacademy.tipsign.TipSignPermissions;
import dev.blockacademy.tipsign.common.TipSignConfig;
import dev.blockacademy.tipsign.common.TipSignData;
import dev.blockacademy.tipsign.compat.VersionAdapter;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
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
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

public class TipSignBlock extends BaseEntityBlock {

    public static final MapCodec<TipSignBlock> CODEC = simpleCodec(TipSignBlock::new);
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;

    // Collision shape: a post with a sign board on top
    private static final VoxelShape SHAPE = Block.box(4, 0, 4, 12, 16, 12);

    public TipSignBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext ctx) {
        return this.defaultBlockState().setValue(FACING, ctx.getHorizontalDirection().getOpposite());
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext ctx) {
        return SHAPE;
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
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

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }

        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof TipSignBlockEntity tipSign && player instanceof ServerPlayer serverPlayer) {
            boolean isOwner = player.getUUID().equals(tipSign.getData().ownerUuid());
            boolean isAdmin = player.hasPermissions(2);
            boolean sneaking = player.isShiftKeyDown();

            if (sneaking && (isOwner || isAdmin)) {
                // Owner/admin shift+right-click → Author UI
                VersionAdapter.INSTANCE.sendOpenSignToClient(serverPlayer, pos, tipSign.getData(), true);
            } else if (sneaking && !isOwner && !isAdmin) {
                // Non-owner shift+right-click → denied
                serverPlayer.sendSystemMessage(Component.literal("You are not the owner of this Tip Sign."));
            } else {
                // Normal right-click → Reader UI
                VersionAdapter.INSTANCE.sendOpenSignToClient(serverPlayer, pos, tipSign.getData(), false);
            }
            return InteractionResult.CONSUME;
        }

        return InteractionResult.PASS;
    }

    @Override
    public BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        if (!level.isClientSide() && player instanceof ServerPlayer serverPlayer) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof TipSignBlockEntity tipSign) {
                if (TipSignConfig.get().ownerOnlyBreak()
                    && !TipSignPermissions.canBreak(serverPlayer, tipSign)) {
                    // Block resists breaking for non-owners
                    serverPlayer.sendSystemMessage(Component.translatable("tipsign.message.not_owner"));
                    return state;
                }

                // Serialize data to dropped item
                ItemStack drop = new ItemStack(TipSignMod.SIGN_POST_ITEM);
                VersionAdapter.INSTANCE.writeToItemStack(drop, tipSign.getData());
                VersionAdapter.INSTANCE.setItemTooltipTitle(drop, tipSign.getData().title());
                Block.popResource(level, pos, drop);
            }
        }
        return super.playerWillDestroy(level, pos, state, player);
    }
}
