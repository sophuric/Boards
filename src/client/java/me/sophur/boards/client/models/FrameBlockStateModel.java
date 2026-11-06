package me.sophur.boards.client.models;

import com.mojang.serialization.MapCodec;
import me.sophur.boards.BoardsBlocks;
import me.sophur.boards.blocks.FrameBlock;
import me.sophur.boards.client.ModelUtil;
import me.sophur.boards.util.Box;
import me.sophur.boards.util.DirectionalMap;
import me.sophur.boards.util.Util;
import net.fabricmc.fabric.api.client.model.loading.v1.CustomUnbakedBlockStateModel;
import net.fabricmc.fabric.api.client.renderer.v1.mesh.QuadEmitter;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.renderer.block.dispatch.WeightedVariants;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.util.random.WeightedList;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Predicate;

import static me.sophur.boards.blocks.FrameBlock.FACE_THICKNESS;
import static me.sophur.boards.util.Util.cycleEnum;
import static net.minecraft.core.Direction.AxisDirection.NEGATIVE;
import static net.minecraft.core.Direction.AxisDirection.POSITIVE;

// A lot of this is copied from Fabric renderer API test mod
public class FrameBlockStateModel implements BlockStateModel {
    private static final List<Vector3f> DIRECTION_STEPS =
        Arrays.stream(Direction.values())
            .map(Direction::getUnitVec3f)
            .map(Vector3f::new)
            .toList();

    // positions of the centers of each face
    private static final List<Vector3f> DIRECTION_FACE_CENTER =
        DIRECTION_STEPS.stream()
            .map(d -> new Vector3f(d).mul(0.5f).add(0.5f, 0.5f, 0.5f))
            .toList();

    // what to scale the vertex position by
    private static final List<Vector3f> DIRECTION_FACE_SCALE =
        DIRECTION_STEPS.stream()
            .map(Vector3f::absolute)
            .map(d -> new Vector3f(d).mul((float) FACE_THICKNESS - 1))
            .map(d -> d.add(1, 1, 1))
            // FACE_THICKNESS in the axis of the direction, 1.0 in the other two axes
            .toList();

    private static BlockStateModel getModel(Block block) {
        if (block == null || block.equals(Blocks.AIR)) return null;
        // prevent stack overflow
        if (block.equals(BoardsBlocks.FRAME_BLOCK)) return null;
        var model = ModelUtil.getModel(block.defaultBlockState());
        if (model instanceof FrameBlockStateModel) return null;
        return model;
    }

    @Override
    public void emitQuads(@NonNull QuadEmitter emitter, @NonNull BlockAndTintGetter level, @NonNull BlockPos blockPos, @NonNull BlockState state, @NonNull RandomSource random, @NonNull Predicate<@Nullable Direction> cullTest) {
        if (!(level.getBlockEntityRenderData(blockPos) instanceof DirectionalMap.BlockMap blockMap)) return;

        Predicate<Direction> hasFace = direction -> getModel(blockMap.get(direction)) != null;

        blockMap.forEach((direction, block) -> {
            var faceModel = getModel(block);
            if (faceModel == null) return;
            int ordinal = direction.ordinal();
            var faceCenter = DIRECTION_FACE_CENTER.get(ordinal);
            var faceScale = DIRECTION_FACE_SCALE.get(ordinal);

            var opposite = direction.getOpposite();
            var directionAxis = direction.getAxis();

            emitter.pushTransform(quad -> {
                var cullFace = quad.cullFace();
                // if this is a side face
                if (cullFace != null && !direction.equals(cullFace) && !opposite.equals(cullFace)) {
                    if (faceModel.equals(getModel(blockMap.get(cullFace)))) {
                        // if the other face is the same block, don't draw the diagonal quad between the faces
                        // this mimics the vanilla behaviour of how glass blocks of the same colour don't render between faces
                        return false;
                    }
                }
                BiFunction<Integer, Vector3f, Vector3f> transformation = (_, pos) -> {
                    // scale pos towards the face direction
                    var scaledPos = pos.sub(faceCenter, new Vector3f()).mul(faceScale).add(faceCenter);

                    var depth = Util.choose(directionAxis, pos);
                    if (direction.getAxisDirection().equals(POSITIVE)) depth = 1 - depth;
                    final var depth_ = depth;

                    BiConsumer<Direction.Axis, Vector3f> offsetFromSides = (sideAxis, posToOffset) -> {
                        var sidewaysValue = Util.choose(sideAxis, posToOffset);
                        var sideAxisDirection = sidewaysValue > 0.5 ? POSITIVE : NEGATIVE;
                        if (sideAxisDirection == POSITIVE) sidewaysValue = 1 - sidewaysValue;
                        var sideDirection = Direction.fromAxisAndDirection(sideAxis, sideAxisDirection);

                        // only offset if there is a face here
                        if (!hasFace.test(sideDirection)) return;

                        var offsetAmount = Math.max((float) FACE_THICKNESS - sidewaysValue, 0) * depth_;
                        var offsetVec = sideDirection.getUnitVec3f().mul(offsetAmount, new Vector3f());
                        posToOffset.sub(offsetVec); // subtract, because we want to offset inwards
                    };

                    // offset from the two other sides
                    offsetFromSides.accept(cycleEnum(directionAxis, 1), scaledPos);
                    offsetFromSides.accept(cycleEnum(directionAxis, 2), scaledPos);

                    return scaledPos;
                };

                ModelUtil.transformQuad(quad, transformation, true);

                // don't cull faces that have been moved
                if (cullFace != null && (opposite.equals(cullFace) || hasFace.test(cullFace)))
                    quad.cullFace(null);
                return true;
            });
            Predicate<@Nullable Direction> newCullTest = cullFace -> {
                if (cullFace != null && (opposite.equals(cullFace) || hasFace.test(cullFace)))
                    return false;
                return cullTest.test(cullFace);
            };
            faceModel.emitQuads(emitter, level, blockPos, state, random, newCullTest);
            emitter.popTransform();
        });
    }

    @Override
    public @Nullable Object createGeometryKey(@NonNull BlockAndTintGetter level, @NonNull BlockPos pos, @NonNull BlockState state, @NonNull RandomSource random) {
        if (!(level.getBlockEntityRenderData(pos) instanceof DirectionalMap.BlockMap blockMap)) return this;
        return blockMap;
    }

    @Override
    public Material.@NonNull Baked particleMaterial(BlockAndTintGetter level, @NonNull BlockPos pos, @NonNull BlockState state) {
        WeightedList.Builder<BlockStateModel> list = WeightedList.builder();
        if (level.getBlockEntityRenderData(pos) instanceof DirectionalMap.BlockMap blockMap) {
            blockMap.forEach(faceBlock -> {
                var faceModel = getModel(faceBlock);
                if (faceModel == null) return;
                list.add(faceModel);
            });
        }
        return new WeightedVariants(list.build()).particleMaterial();
    }

    @Override
    public @BakedQuad.MaterialFlags int materialFlags(@NonNull BlockAndTintGetter level, @NonNull BlockPos pos, @NonNull BlockState state, @NonNull RandomSource random) {
        if (!(level.getBlockEntityRenderData(pos) instanceof DirectionalMap.BlockMap blockMap)) return 0;

        Box.Integer flags = new Box.Integer(0);

        blockMap.forEach(faceBlock -> {
            var model = getModel(faceBlock);
            if (model == null) return;
            int modelFlags = model.materialFlags(level, pos, state, random);
            // bitwise OR all the flags together
            flags.set(flags.get() | modelFlags);
        });

        return flags.get();
    }

    @Override
    public Material.@NonNull Baked particleMaterial() {
        return new WeightedVariants(WeightedList.of()).particleMaterial();
    }

    @Override
    public @BakedQuad.MaterialFlags int materialFlags() {
        return BakedQuad.FLAG_ANIMATED | BakedQuad.FLAG_TRANSLUCENT;
    }

    @Override
    public void collectParts(@NonNull RandomSource random, @NonNull List<BlockStateModelPart> output) {
    }

    public record Unbaked() implements CustomUnbakedBlockStateModel {
        public static final MapCodec<Unbaked> CODEC = MapCodec.unit(Unbaked::new);

        @Override
        public @NonNull MapCodec<? extends CustomUnbakedBlockStateModel> codec() {
            return CODEC;
        }

        @Override
        public @NonNull BlockStateModel bake(@NonNull ModelBaker modelBakery) {
            return new FrameBlockStateModel();
        }

        @Override
        public void resolveDependencies(@NonNull Resolver resolver) {
        }
    }
}
