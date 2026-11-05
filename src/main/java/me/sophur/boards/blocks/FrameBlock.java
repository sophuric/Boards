package me.sophur.boards.blocks;

import com.mojang.math.OctahedralGroup;
import me.sophur.boards.BlockBehaviourTags;
import me.sophur.boards.util.Util;
import me.sophur.boards.blockentities.FrameBlockEntity;
import me.sophur.stairstone.api.v2.StairstoneBehaviour;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Map;

import static net.minecraft.world.level.block.state.properties.BlockStateProperties.WATERLOGGED;

public class FrameBlock extends BaseEntityBlock implements SimpleWaterloggedBlock,
    StairstoneBehaviour.RedstonePower, StairstoneBehaviour.RedstoneWireConnectivity {
    public FrameBlock(Properties properties) {
        properties = properties.dynamicShape().isRedstoneConductor(Blocks::never);
        super(properties);

        BlockState state = defaultBlockState();
        for (Direction dir : Direction.values()) {
            state = state.setValue(Util.getDirectionalProperty(dir), false);
        }
        state = state.setValue(WATERLOGGED, false);
        registerDefaultState(state);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.@NonNull Builder<Block, BlockState> builder) {
        for (Direction dir : Direction.values()) {
            builder.add(Util.getDirectionalProperty(dir));
        }
        builder.add(WATERLOGGED);
    }

    @Override
    public @Nullable BlockState getStateForPlacement(@NonNull BlockPlaceContext context) {
        return Util.getWaterloggedStateForPlacement(defaultBlockState(), context);
    }

    @Override
    protected @NonNull FluidState getFluidState(@NonNull BlockState state) {
        return Util.getWaterloggedFluidState(state, super::getFluidState);
    }

    public @NonNull BlockState updateShape(@NonNull BlockState state, @NonNull LevelReader level, @NonNull ScheduledTickAccess ticks, @NonNull BlockPos pos) {
        Util.updateShapeWaterlogged(state, level, ticks, pos);
        return state;
    }

    @Override
    protected @NonNull BlockState updateShape(@NonNull BlockState state, @NonNull LevelReader level, @NonNull ScheduledTickAccess ticks, @NonNull BlockPos pos, @NonNull Direction directionToNeighbour, @NonNull BlockPos neighbourPos, @NonNull BlockState neighbourState, @NonNull RandomSource random) {
        return updateShape(state, level, ticks, pos);
    }

    private static final VoxelShape BASE_SHAPE;
    public static final double BASE_RADIUS = 2.0 / 16.0;
    private static final Map<Direction, VoxelShape> FACE_SHAPES;
    public static final double FACE_THICKNESS = 1.0 / 16.0;

    static {
        // 3D cross shape
        VoxelShape barX = Shapes.box(
            0, 0.5 - BASE_RADIUS, 0.5 - BASE_RADIUS,
            1, 0.5 + BASE_RADIUS, 0.5 + BASE_RADIUS),
            barY = Shapes.rotate(barX, OctahedralGroup.ROT_90_Z_POS),
            barZ = Shapes.rotate(barX, OctahedralGroup.ROT_90_Y_POS);
        BASE_SHAPE = Shapes.or(barX, barY, barZ);

        // each face
        var northFace = Shapes.box(0, 0, 0, 1, 1, FACE_THICKNESS);
        FACE_SHAPES = Shapes.rotateAll(northFace);
    }

    @Override
    protected @NonNull VoxelShape getShape(@NonNull BlockState state, @NonNull BlockGetter level, @NonNull BlockPos pos, @NonNull CollisionContext context) {
        VoxelShape shape = BASE_SHAPE;
        for (Direction dir : Direction.values()) {
            if (state.getValue(Util.getDirectionalProperty(dir))) {
                shape = Shapes.or(shape, FACE_SHAPES.get(dir));
            }
        }
        return shape;
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(@NonNull BlockPos worldPosition, @NonNull BlockState blockState) {
        return new FrameBlockEntity(worldPosition, blockState);
    }

    private static List<Util.DirectionsResult> getHitResultInfo(BlockPos pos, BlockHitResult hitResult) {
        var localPos = hitResult.getLocation().subtract(new Vec3(pos)).subtract(0.5, 0.5, 0.5);
        return Util.getMajorDirections(localPos);
    }

    @Override
    protected @NonNull InteractionResult useItemOn(@NonNull ItemStack itemStack, @NonNull BlockState state, @NonNull Level level, @NonNull BlockPos pos, @NonNull Player player, @NonNull InteractionHand hand, @NonNull BlockHitResult hitResult) {
        if (itemStack.isEmpty()) return InteractionResult.TRY_WITH_EMPTY_HAND;
        if (!player.mayBuild()) return InteractionResult.PASS;

        if (level.getBlockEntity(pos) instanceof FrameBlockEntity be
            && be.canSetFaceItem(itemStack)) {
            var info = getHitResultInfo(pos, hitResult);

            // detect only right-clicking on the base cross shape
            if (info.get(1).absValue() <= BASE_RADIUS) {
                var face = info.getFirst().direction();
                if (!be.hasFaceItem(face)) {
                    itemStack = itemStack.consumeAndReturn(1, player); // decrease item count from player
                    itemStack.setCount(1);
                    be.setFaceItem(face, itemStack);
                    return InteractionResult.SUCCESS;
                }
            }

            // prevent player from misplacing a block by accident
            if (info.get(0).absValue() < 0.5) return InteractionResult.CONSUME;
        }
        return InteractionResult.PASS;
    }

    @Override
    protected @NonNull InteractionResult useWithoutItem(@NonNull BlockState state, Level level, @NonNull BlockPos pos, @NonNull Player player, @NonNull BlockHitResult hitResult) {
        if (!player.mayBuild()) return InteractionResult.PASS;
        if (level.getBlockEntity(pos) instanceof FrameBlockEntity be) {
            var info = getHitResultInfo(pos, hitResult);
            for (var face : info) {
                if (info.get(1).absValue() <= BASE_RADIUS && info.get(0).direction() != face.direction()) continue;

                var itemStack = be.getFaceItem(face.direction());
                if (itemStack.isEmpty()) continue;
                itemStack = itemStack.copyAndClear(); // remove item from face
                if (player.hasInfiniteMaterials()) {
                    // give creative-mode player the itemStack if they don't already have it, so the player doesn't lose the item
                    final var itemStack_ = itemStack; // java quirks
                    if (!player.getInventory().hasAnyMatching(i -> ItemStack.isSameItemSameComponents(i, itemStack_)))
                        player.getInventory().add(player.getInventory().getSelectedSlot(), itemStack);
                } else if (!level.isClientSide()) {
                    Block.popResourceFromFace(level, pos, face.direction(), itemStack);
                }
                be.setChanged();
                return InteractionResult.SUCCESS;
            }
        }
        return InteractionResult.PASS;
    }

    @Override
    protected boolean useShapeForLightOcclusion(@NonNull BlockState state) {
        return true;
    }

    @Override
    protected @NonNull List<ItemStack> getDrops(@NonNull BlockState state, LootParams.@NonNull Builder params) {
        var drops = super.getDrops(state, params);

        if (params.getOptionalParameter(LootContextParams.BLOCK_ENTITY)
            instanceof FrameBlockEntity blockEntity) {
            drops.addAll(Util.stackItems(blockEntity.getItems().stream()));
        }

        return drops;
    }

    private boolean isSolidFace(Direction direction, BlockGetter level, BlockPos blockPos, BlockState blockState) {
        if (level.getBlockEntity(blockPos) instanceof FrameBlockEntity be) {
            return be.hasSolidFace(direction);
        }
        return false;
    }

    @Override
    public boolean isSolidFaceForRedstonePower(Direction direction, BlockGetter level, BlockPos blockPos, BlockState blockState) {
        return isSolidFace(direction, level, blockPos, blockState);
    }

    @Override
    public boolean isSolidFaceForRedstoneWireConnectivity(Direction direction, BlockGetter level, BlockPos blockPos, BlockState blockState) {
        return isSolidFace(direction, level, blockPos, blockState);
    }

    @Override
    protected boolean shouldRedstoneWireConnectTo(@NonNull BlockState blockState, @NonNull BlockGetter level, @NonNull BlockPos blockPos, @Nullable Direction direction) {
        if (direction == null) return false;
        if (!(level.getBlockEntity(blockPos) instanceof FrameBlockEntity be)) return false;
        // direction refers to the way the Redstone wire will face if this is true, not the face of our block
        return be.faceHasTag(direction.getOpposite(), BlockBehaviourTags.SIGNAL_SOURCE);
    }

    @Override
    protected int getSignal(@NonNull BlockState blockState, @NonNull BlockGetter level, @NonNull BlockPos blockPos, @NonNull Direction direction) {
        if (!(level.getBlockEntity(blockPos) instanceof FrameBlockEntity be)) return 0;
        return be.faceHasTag(direction.getOpposite(), BlockBehaviourTags.POWERED_TAG) ? 15 : 0;
    }

    @Override
    protected @NonNull VoxelShape getOcclusionShape(@NonNull BlockState state) {
        return Shapes.empty();
    }
}
