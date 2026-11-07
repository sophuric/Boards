package me.sophur.boards.client;

import me.sophur.boards.util.VectorUtil;
import net.fabricmc.fabric.api.client.renderer.v1.mesh.MutableQuadView;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.world.level.block.state.BlockState;
import org.joml.Vector2f;
import org.joml.Vector3f;

import java.util.Comparator;
import java.util.Objects;
import java.util.function.BiFunction;

import static me.sophur.boards.util.Util.stream;

public class ModelUtil {
    private static final int VERTEX_COUNT = 4;

    public static boolean transformQuad(MutableQuadView quad, BiFunction<Integer, Vector3f, Vector3f> transformation, boolean affectUVs) {
        Vector3f[] pos = new Vector3f[VERTEX_COUNT];
        Vector2f[] uv = new Vector2f[VERTEX_COUNT];
        Vector3f[] normal = new Vector3f[VERTEX_COUNT];
        Vector3f[] flattenedPos = new Vector3f[VERTEX_COUNT];
        if (affectUVs)
            for (int i = 0; i < VERTEX_COUNT; ++i) {
                pos[i] = quad.copyPos(i, null);
                uv[i] = quad.copyUv(i, null);
                normal[i] = quad.copyNormal(i, null);
                flattenedPos[i] = VectorUtil.flatten(pos[i], Objects.requireNonNull(normal[i]));
            }

        for (int i = 0; i < VERTEX_COUNT; ++i) {
            var pos_ = affectUVs ? pos[i] : quad.copyPos(i, null);
            var newPos = transformation.apply(i, new Vector3f(pos_));
            quad.pos(i, newPos);
            if (!affectUVs) continue;

            Vector2f[] uvs = new Vector2f[3];
            float[] distanceNewUVs = new float[3];
            // loop over the other three points
            for (int j = 0, k = 0; j < VERTEX_COUNT; ++j) {
                if (j == i) continue;
                // find distance from this point to the other points
                float distancePos = flattenedPos[j].distance(flattenedPos[i]),
                    distanceNewPos = flattenedPos[j].distance(VectorUtil.flatten(newPos, normal[i])),
                    // distance in UV coordinates
                    distanceUV = uv[j].distance(uv[i]),
                    // essentially we find the factor by which the distances between this
                    // vector and each of the other three vectors are scaled by
                    // then multiply the distances in UV coordinate by that factor
                    distanceNewUV = distanceNewPos / distancePos * distanceUV;
                uvs[k] = uv[j];
                distanceNewUVs[k++] = distanceNewUV;
            }

            // after we find the new UV distances, we find the new UV position from the new distances
            // we have two positions and two distances (radii) from a certain point, we need to find that point
            // this is essentially finding the intersection of a circle
            var intersections = VectorUtil.findCircleIntersections(uvs[0], distanceNewUVs[0], uvs[1], distanceNewUVs[1]);
            if (intersections == null) return false;
            // we have another position and another radius, so we can use this to find which solution is correct
            // find which one is closest to the third circle
            var intersection = stream(intersections).min(Comparator.comparing(
                v -> Math.abs(uvs[2].distance(v) - distanceNewUVs[2])
            )).orElseThrow();
            // new UV position
            quad.uv(i, intersection);
        }

        return true;
    }

    public static BlockStateModel getModel(BlockState state) {
        return Minecraft.getInstance().getModelManager().getBlockStateModelSet().get(state);
    }
}
