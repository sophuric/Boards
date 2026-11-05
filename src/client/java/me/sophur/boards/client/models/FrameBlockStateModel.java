package me.sophur.boards.client.models;

import com.mojang.serialization.MapCodec;
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
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Predicate;

import static me.sophur.boards.blocks.FrameBlock.FACE_THICKNESS;
import static me.sophur.boards.client.ModelUtil.getModel;
import static me.sophur.boards.util.Util.magnitudeSquared;
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

    @Override
    public void emitQuads(@NonNull QuadEmitter emitter, @NonNull BlockAndTintGetter level, @NonNull BlockPos pos, @NonNull BlockState state, @NonNull RandomSource random, @NonNull Predicate<@Nullable Direction> cullTest) {
        if (!(level.getBlockEntityRenderData(pos) instanceof DirectionalMap.BlockMap blockMap)) return;

        blockMap.forEach((direction, block) -> {
            if (block.equals(Blocks.AIR)) return;

            var faceModel = getModel(block.defaultBlockState());
            int ordinal = direction.ordinal();
            var faceCenter = DIRECTION_FACE_CENTER.get(ordinal);
            var faceScale = DIRECTION_FACE_SCALE.get(ordinal);
            BiFunction<Integer, Vector3f, Vector3f> baseTransformation =
                (_, v) -> v.sub(faceCenter, new Vector3f()).mul(faceScale).add(faceCenter);

            var opposite = direction.getOpposite();

            emitter.pushTransform(quad -> {
                var transformation = baseTransformation;
                var cullFace = quad.cullFace();
                // if this is the side face
                if (cullFace != null && !direction.equals(cullFace) && !opposite.equals(cullFace)) {
                    if (!blockMap.get(cullFace).equals(Blocks.AIR)) {
                        // if that face already has a block here, discard the quad so we don't get plane fighting
                        return false;
                    }
                    transformation = (i, v) -> {
                        // offset vertex so that the corner doesn't Z-fight with the other face's corner
                        var newV = baseTransformation.apply(i, v);
                        var vecNormal = Objects.requireNonNull(quad.copyNormal(i, null));
                        var dirNormal = direction.getUnitVec3f();
                        var dirSideNormal = dirNormal.cross(vecNormal, new Vector3f());
                        var dirSide = Direction.getApproximateNearest(new Vec3(dirSideNormal));

                        // figure out which side this vertex is
                        var sidewaysPosition = Util.choose(dirSide.getAxis(), v);
                        if (dirSide.getAxisDirection().equals(POSITIVE)) sidewaysPosition = 1 - sidewaysPosition;
                        if (sidewaysPosition > 0.5) {
                            sidewaysPosition = 1 - sidewaysPosition;
                            dirSideNormal = Util.invert(dirSideNormal);
                            dirSide = dirSide.getOpposite();
                        }

                        // other face is not present, so we don't need to offset
                        if (blockMap.get(dirSide).equals(Blocks.AIR)) return newV;

                        // depth = how far from direction to the opposite side that the vertex position is
                        var depth = Util.choose(direction.getAxis(), v);
                        if (direction.getAxisDirection().equals(POSITIVE)) depth = 1 - depth;

                        // offset the vector so that the corners don't Z-fight
                        var offsetAmount = Math.max((float) FACE_THICKNESS - sidewaysPosition, 0);
                        var offset = dirSideNormal.mul(depth * -offsetAmount, new Vector3f());

                        return newV.add(offset);
                    };
                }

                ModelUtil.transformQuad(quad, transformation, true);

                // block model has been scaled, such that it no longer touches the opposite block face
                if (opposite.equals(cullFace))
                    quad.cullFace(null);
                return true;
            });
            Predicate<@Nullable Direction> newCullTest = face -> {
                if (opposite.equals(face)) return false;
                return cullTest.test(face);
            };
            faceModel.emitQuads(emitter, level, pos, state, random, newCullTest);
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
                BlockState faceState = faceBlock.defaultBlockState();
                list.add(getModel(faceState));
            });
        }
        return new WeightedVariants(list.build()).particleMaterial();
    }

    @Override
    public @BakedQuad.MaterialFlags int materialFlags(@NonNull BlockAndTintGetter level, @NonNull BlockPos pos, @NonNull BlockState state, @NonNull RandomSource random) {
        if (!(level.getBlockEntityRenderData(pos) instanceof DirectionalMap.BlockMap blockMap)) return 0;

        Box.Integer flags = new Box.Integer(0);

        blockMap.forEach(faceBlock -> {
            var model = getModel(faceBlock.defaultBlockState());
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
