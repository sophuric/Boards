package me.sophur.boards.util;

import com.mojang.datafixers.util.Pair;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.*;

import java.lang.Math;

public class VectorUtil {
    private VectorUtil() {
    }

    public static Vector2f project(Vector2fc a, Vector2fc b) {
        float scale = a.dot(b) / b.lengthSquared();
        return b.mul(scale, new Vector2f());
    }

    public static Vector3f project(Vector3fc a, Vector3fc b) {
        float scale = a.dot(b) / b.lengthSquared();
        return b.mul(scale, new Vector3f());
    }

    public static Vector2d project(Vector2dc a, Vector2dc b) {
        double scale = a.dot(b) / b.lengthSquared();
        return b.mul(scale, new Vector2d());
    }

    public static Vector3d project(Vector3dc a, Vector3dc b) {
        double scale = a.dot(b) / b.lengthSquared();
        return b.mul(scale, new Vector3d());
    }

    public static Vector2f flatten(Vector2fc a, Vector2fc b) {
        return a.sub(project(a, b), new Vector2f());
    }

    public static Vector3f flatten(Vector3fc a, Vector3fc b) {
        return a.sub(project(a, b), new Vector3f());
    }

    public static Vector2d flatten(Vector2dc a, Vector2dc b) {
        return a.sub(project(a, b), new Vector2d());
    }

    public static Vector3d flatten(Vector3dc a, Vector3dc b) {
        return a.sub(project(a, b), new Vector3d());
    }

    public static @Nullable Pair<Vector2f, Vector2f> findCircleIntersections(@NotNull Vector2f centerA, float radiusA, @NotNull Vector2f centerB, float radiusB) {
        // https://stackoverflow.com/a/3349134
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
}
