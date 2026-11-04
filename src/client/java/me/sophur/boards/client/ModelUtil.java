package me.sophur.boards.client;

import com.mojang.datafixers.util.Pair;
import net.fabricmc.fabric.api.client.renderer.v1.mesh.MutableQuadView;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector3fc;

import java.util.Comparator;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.stream.Stream;

import static me.sophur.boards.util.Util.stream;

public class ModelUtil {
    private static final int VERTEX_COUNT = 4;

    public static @Nullable Pair<Vector2f, Vector2f> findCircleIntersections(@NotNull Vector2f centerA, float radiusA, @NotNull Vector2f centerB, float radiusB) {
        // https://stackoverflow.com/a/3349134
        // find other point using distances to three other points
        // essentially intersection of three circles
        float distance = centerA.distance(centerB);
        float radiiDiff = Math.abs(radiusA - radiusB);
        if (distance > radiusA + radiusB) return null; // circles are too far away
        if (distance < radiiDiff) return null; // one circle contains another
        if (distance == 0 && radiiDiff == 0) return null; // infinite solutions

        // a = (r0^2 - r1^2 + d^2) / (2d)
        float triWidth = ((radiusA * radiusA) - (radiusB * radiusB) + (distance * distance)) / (distance * 2);
        // h = ±√(r0^2 - a^2)
        float triHeight = Mth.sqrt((radiusA * radiusA) - (triWidth * triWidth));
        // P2 = P0 + a (P1 - P0) / d
        Vector2f offset = centerB.sub(centerA, new Vector2f());
        Vector2f triBase = centerA.add(offset.mul(triWidth / distance, new Vector2f()), new Vector2f());
        // x3 = x2 + h (y1 - y0) / d
        // y3 = y2 + h (x1 - x0) / d
        Vector2f offsetSwapped = offset.perpendicular();
        Vector2f triBaseOffset = offsetSwapped.mul(triHeight / distance);

        // h = ±..., gives two solutions
        return Pair.of(
            triBase.add(triBaseOffset, new Vector2f()),
            triBase.sub(triBaseOffset, new Vector2f())
        );
    }

    public static Vector3f project(Vector3fc a, Vector3fc b) {
        float scale = a.dot(b) / b.lengthSquared();
        return b.mul(scale, new Vector3f());
    }

    public static Vector3f flatten(Vector3fc a, Vector3fc b) {
        return a.sub(project(a, b), new Vector3f());
    }

    public static boolean transformQuad(MutableQuadView quad, BiFunction<Integer, Vector3f, Vector3f> transformation, boolean affectUVs) {
        Vector3f[] pos = new Vector3f[VERTEX_COUNT];
        Vector2f[] uv = new Vector2f[VERTEX_COUNT];
        Vector3f[] normal = new Vector3f[VERTEX_COUNT];
        Vector3f[] flattenedPos = new Vector3f[VERTEX_COUNT];
        for (int i = 0; i < VERTEX_COUNT; ++i) {
            pos[i] = quad.copyPos(i, null);
            if (!affectUVs) continue;
            uv[i] = quad.copyUv(i, null);
            normal[i] = quad.copyNormal(i, null);
            flattenedPos[i] = flatten(pos[i], Objects.requireNonNull(normal[i]));
        }

        for (int i = 0; i < VERTEX_COUNT; ++i) {
            var newPos = transformation.apply(i, new Vector3f(pos[i]));
            quad.pos(i, newPos);
            if (!affectUVs) continue;

            Vector2f[] uvs = new Vector2f[3];
            float[] distanceUVs = new float[3];
            for (int j = 0, k = 0; j < VERTEX_COUNT; ++j) {
                if (j == i) continue;
                // find distance to other points
                float distancePos = flattenedPos[j].distance(flattenedPos[i]),
                    distanceNewPos = flattenedPos[j].distance(flatten(newPos, normal[i])),
                    distanceUV = uv[j].distance(uv[i]),
                    distanceNewUV = distanceNewPos / distancePos * distanceUV;
                uvs[k] = uv[j];
                distanceUVs[k++] = distanceNewUV;
            }

            var intersections = findCircleIntersections(uvs[0], distanceUVs[0], uvs[1], distanceUVs[1]);
            if (intersections == null) {
                return false;
            }
            // find which one is closest to the third circle
            var intersection = stream(intersections).min(Comparator.comparing(
                v -> Math.abs(uvs[2].distance(v) - distanceUVs[2])
            )).orElseThrow();
            quad.uv(i, intersection);
        }

        return true;
    }

    public static BlockStateModel getModel(BlockState state) {
        return Minecraft.getInstance().getModelManager().getBlockStateModelSet().get(state);
    }
}
