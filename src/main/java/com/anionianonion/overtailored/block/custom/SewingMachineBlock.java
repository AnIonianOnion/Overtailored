package com.anionianonion.overtailored.block.custom;

import com.anionianonion.overtailored.SewingMachineTier;
import com.anionianonion.overtailored.block.ModBlocks;
import com.anionianonion.overtailored.block.entity.SewingMachineBlockEntity;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

//multiblock 2x1 -> uses an additional setPlacedBy(), onRemove(), and modified getStateForPlacement() for multiblock position's logic
//based onhttps://github.com/phuccom000/Overgeared/blob/1.21.1/src/main/java/net/stirdrem/overgeared/block/custom/SteelSmithingAnvil.java
public class SewingMachineBlock extends AbstractSewingMachineBlock {

    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;

    public static final VoxelShape SHAPE_NORTH = SewingMachineBlock.box(-16, 0, 0, 16, 16, 16);
    public static final VoxelShape SHAPE_SOUTH = SewingMachineBlock.box(0, 0, 0, 32, 16, 16);
    public static final VoxelShape SHAPE_EAST  = SewingMachineBlock.box(0, 0, -16, 16, 16, 16);
    public static final VoxelShape SHAPE_WEST  = SewingMachineBlock.box(0, 0, 0, 16, 16, 32);

    public SewingMachineBlock(SewingMachineTier sewingMachineTier, BlockBehaviour.Properties properties) {
        super(sewingMachineTier, properties);
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return null;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext ctx) {
        Direction dir =  state.getValue(FACING);
        if (dir.getAxis() == Direction.Axis.X) {
            return dir.getStepX() > 0 ? SHAPE_EAST : SHAPE_WEST;
        }
        else {
            return dir.getStepZ() > 0 ? SHAPE_SOUTH : SHAPE_NORTH;
        }
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext ctx) {
        Level level = ctx.getLevel();
        BlockPos pos = ctx.getClickedPos();
        Direction facing = ctx.getHorizontalDirection();

        BlockPos extPos = pos.relative(facing.getCounterClockWise());

        //If extension space is blocked, cancel placement entirely
        if (!level.getBlockState(extPos).canBeReplaced(ctx)) {
            return null; //returning null cancels placement AND prevents item consumption
        }

        return this.defaultBlockState().setValue(FACING, facing);
    }


    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
        Direction facing = state.getValue(FACING);
        BlockPos extPos = pos.relative(facing.getCounterClockWise());

        level.setBlock(extPos,
                ModBlocks.SEWING_MACHINE_EXTENSION_BLOCK.get().defaultBlockState()
                        .setValue(SewingMachinePartBlock.FACING, facing),
                3
        );
    }


    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext ctx) {
        return getShape(state, level, pos, ctx);
    }


    @Override
    public @NotNull VoxelShape getInteractionShape(@NotNull BlockState state, @NotNull BlockGetter level, @NotNull BlockPos pos) {
        return getShape(state, level, pos, CollisionContext.empty());
    }


    @Override
    public @NotNull BlockState rotate(BlockState pState, Rotation pRotation) {
        return pState.setValue(FACING, pRotation.rotate(pState.getValue(FACING)));
    }

    @Override
    public BlockState mirror(BlockState state, Mirror mirror) {
        Rotation rotation = mirror.getRotation(state.getValue(FACING));
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> pBuilder) {
        pBuilder.add(FACING);
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new SewingMachineBlockEntity(pos, state);
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (!state.is(newState.getBlock())) {
            Direction facing = state.getValue(FACING);
            BlockPos extendedPos = pos.relative(facing.getCounterClockWise());

            BlockState extState = level.getBlockState(extendedPos);
            if (extState.getBlock() instanceof SewingMachinePartBlock) {
                // Remove extension WITHOUT drops
                level.removeBlock(extendedPos, false);
            }
        }
        super.onRemove(state, level, pos, newState, isMoving);
    }

    @Override
    public InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        return super.useWithoutItem(state, level, pos, player, hitResult);
    }

    @Override
    public ItemInteractionResult useItemOn(ItemStack held, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        return super.useItemOn(held, state, level, pos, player, hand, hit);
    }
}
