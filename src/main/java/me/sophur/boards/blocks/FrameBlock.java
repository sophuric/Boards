package me.sophur.boards.blocks;

import com.mojang.datafixers.util.Pair;
import com.mojang.math.OctahedralGroup;
import com.mojang.serialization.MapCodec;
import me.sophur.boards.Util;
import me.sophur.boards.blockentities.FrameBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Map;

public class FrameBlock extends BaseEntityBlock {
    public FrameBlock(Properties properties) {
        super(properties);

        BlockState state = defaultBlockState();
        for (Direction dir : Direction.values()) {
            state = state.setValue(Util.getDirectionalProperty(dir), false);
        }
        registerDefaultState(state);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        for (Direction dir : Direction.values()) {
            builder.add(Util.getDirectionalProperty(dir));
        }
    }

    private static final VoxelShape BASE_SHAPE;
    private static final double BASE_RADIUS = 2.0 / 16.0;
    private static final Map<Direction, VoxelShape> FACE_SHAPES;
    private static final double FACE_THICKNESS = 1.0 / 16.0;

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
    protected @NonNull MapCodec<? extends BaseEntityBlock> codec() {
        return simpleCodec(FrameBlock::new);
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
    protected @NonNull InteractionResult useItemOn(@NonNull ItemStack itemStack, @NonNull BlockState state, Level level, @NonNull BlockPos pos, @NonNull Player player, @NonNull InteractionHand hand, @NonNull BlockHitResult hitResult) {
        if (itemStack.isEmpty()) return InteractionResult.TRY_WITH_EMPTY_HAND;

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
        if (level.getBlockEntity(pos) instanceof FrameBlockEntity be) {
            var info = getHitResultInfo(pos, hitResult);
            for (var face : info) {
                if (info.get(1).absValue() <= BASE_RADIUS && info.get(0).direction() != face.direction()) continue;

                var itemStack = be.getFaceItem(face.direction());
                if (itemStack.isEmpty()) continue;
                itemStack = itemStack.copyAndClear(); // remove item from face
                if (!player.hasInfiniteMaterials())
                    Block.popResourceFromFace(level, pos, face.direction(), itemStack);
                be.setChanged();
                return InteractionResult.SUCCESS;
            }
        }
        return InteractionResult.PASS;
    }
}
