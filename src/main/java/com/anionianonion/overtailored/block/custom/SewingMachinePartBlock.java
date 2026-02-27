package com.anionianonion.overtailored.block.custom;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class SewingMachinePartBlock extends Block {

    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final VoxelShape SHAPE = SewingMachinePartBlock.box(0, 0, 0, 16, 16, 16);

    public SewingMachinePartBlock(Properties props) {
        super(props);
        this.registerDefaultState(this.getStateDefinition().any()
                .setValue(FACING, Direction.NORTH));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext ctx) {
        Direction facing = state.getValue(FACING);
        BlockPos mainPos = pos.relative(facing.getClockWise());
        BlockState mainState = level.getBlockState(mainPos);

        if (mainState.getBlock() instanceof SewingMachineBlock sewingMachineBlock) {
            VoxelShape mainShape = sewingMachineBlock.getShape(mainState, level, mainPos, ctx);

            // Translate the main block's shape into the extension block's coordinate space
            double dx = mainPos.getX() - pos.getX();
            double dy = mainPos.getY() - pos.getY();
            double dz = mainPos.getZ() - pos.getZ();

            return mainShape.move(dx, dy, dz);
        }

        return Shapes.empty();
    }



    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext ctx) {
        return Shapes.empty();
    }

    @Override
    public VoxelShape getInteractionShape(BlockState state, BlockGetter level, BlockPos pos) {
        return SHAPE;
    }


    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {

        // find the MAIN block
        Direction facing = state.getValue(FACING);
        BlockPos mainPos = pos.relative(facing.getClockWise()); // EXTENSION is counterclockwise of main

        BlockState mainState = level.getBlockState(mainPos);

        if (mainState.getBlock() instanceof SewingMachineBlock sewingMachineBlock) {
            return sewingMachineBlock.useItemOn(stack, mainState, level, mainPos, player, hand, hitResult);
        }

        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {

        // find the MAIN block
        Direction facing = state.getValue(FACING);
        BlockPos mainPos = pos.relative(facing.getClockWise()); // EXTENSION is counterclockwise of main

        BlockState mainState = level.getBlockState(mainPos);

        if (mainState.getBlock() instanceof SewingMachineBlock sewingMachineBlock) {
            return sewingMachineBlock.useWithoutItem(mainState, level, mainPos, player, hitResult);
        }

        return InteractionResult.PASS;
    }


    /*
    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos,
                         BlockState newState, boolean isMoving) {

        if (!state.is(newState.getBlock())) {

            // If this is the MAIN block, destroy the extension
            if (state.getValue(PART) == SewingMachineBlock.Part.MAIN) {
                Direction facing = state.getValue(FACING);
                BlockPos extPos = pos.relative(facing.getCounterClockWise());
                level.destroyBlock(extPos, false);
            }

            // If this is the EXTENSION block, destroy the main
            else {
                Direction facing = state.getValue(FACING);
                BlockPos mainPos = pos.relative(facing.getClockWise());
                level.destroyBlock(mainPos, true);
            }
        }

        super.onRemove(state, level, pos, newState, isMoving);
    }

     */

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos,
                         BlockState newState, boolean isMoving) {

        if (!state.is(newState.getBlock())) {
            // EXTENSION is being removed (not replaced with itself)
            Direction facing = state.getValue(FACING);
            BlockPos mainPos = pos.relative(facing.getClockWise());

            BlockState mainState = level.getBlockState(mainPos);

            // Only break MAIN if it still exists and is not already being removed
            if (mainState.getBlock() instanceof SewingMachineBlock) {
                level.destroyBlock(mainPos, true); // no player, but still drops
            }
        }

        super.onRemove(state, level, pos, newState, isMoving);
    }

    @Override
    protected void spawnDestroyParticles(Level level, Player player, BlockPos pos, BlockState state) {
        level.addParticle(
                ParticleTypes.ASH,
                pos.getX() + 0.5,
                pos.getY() + 0.5,
                pos.getZ() + 0.5,
                0,
                0.02,
                0
        );
    }


    @Override
    public BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        if (!level.isClientSide) {
            Direction facing = state.getValue(FACING);
            BlockPos mainPos = pos.relative(facing.getClockWise());

            BlockState mainState = level.getBlockState(mainPos);
            if (mainState.getBlock() instanceof SewingMachineBlock) {
                // Break main block AS IF the player broke it
                level.destroyBlock(mainPos, true, player);
            }
        }

        return super.playerWillDestroy(level, pos, state, player);
    }


    //block picking (middle-click by default)
    @Override
    public ItemStack getCloneItemStack(BlockState state, HitResult target, LevelReader level, BlockPos pos, Player player) {
        // Find the main block
        Direction facing = state.getValue(FACING);
        BlockPos mainPos = pos.relative(facing.getClockWise());
        BlockState mainState = level.getBlockState(mainPos);

        if (mainState.getBlock() instanceof SewingMachineBlock) {
            return new ItemStack(mainState.getBlock());
        }

        // Fallback: return nothing
        return ItemStack.EMPTY;
    }
}

