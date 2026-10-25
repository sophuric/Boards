package me.sophur.boards;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

import static net.minecraft.core.Direction.AxisDirection.NEGATIVE;
import static net.minecraft.core.Direction.AxisDirection.POSITIVE;

public class Util {
    private Util() {
    }

    public static Direction getMajorDirection(Vec3 position) {
        // finds the axis with the greatest absolute value and return that direction

        double absX = Math.abs(position.x), absY = Math.abs(position.y), absZ = Math.abs(position.z);
        Direction.Axis mostAxis = Direction.Axis.X;
        double mostVal = absX;
        if (absY > mostVal) {
            mostAxis = Direction.Axis.Y;
            mostVal = absY;
        }
        if (absZ > mostVal) {
            mostAxis = Direction.Axis.Z;
        }

        return Direction.fromAxisAndDirection(mostAxis, position.get(mostAxis) < 0 ? NEGATIVE : POSITIVE);
    }
}
