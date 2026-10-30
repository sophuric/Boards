package me.sophur.boards;

import com.mojang.datafixers.util.Pair;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

public class Util {
    private Util() {
    }

    public static BooleanProperty getDirectionalProperty(Direction direction) {
        return switch (direction) {
            case UP -> BlockStateProperties.UP;
            case DOWN -> BlockStateProperties.DOWN;
            case NORTH -> BlockStateProperties.NORTH;
            case EAST -> BlockStateProperties.EAST;
            case SOUTH -> BlockStateProperties.SOUTH;
            case WEST -> BlockStateProperties.WEST;
        };
    }

    public static Direction.AxisDirection getAxisDirection(double value) {
        return value < 0 ? Direction.AxisDirection.NEGATIVE : Direction.AxisDirection.POSITIVE;
    }

    public static Stream<Pair<Direction.Axis, Double>> streamAxes(Vec3 position) {
        var axes = Stream.of(Direction.Axis.X, Direction.Axis.Y, Direction.Axis.Z);
        return axes.map(axis -> Pair.of(axis, position.get(axis)));
    }

    public record DirectionsResult(Direction direction, double absValue) {
    }

    /// @param position Position centered around (0.0, 0.0, 0.0)
    /// @return A list of directions sorted descendingly by the absolute value of the position in that axis
    public static List<DirectionsResult> getMajorDirections(Vec3 position) {
        // finds the axis with the greatest absolute value, then the second greatest, then the last

        var pairs = streamAxes(position);
        var absPairs = pairs.map(x -> x.mapSecond(Math::abs));
        var sorted = absPairs.sorted(Comparator.comparing(Pair::getSecond));
        var major = sorted.map(x -> x.mapFirst(axis ->
                Direction.fromAxisAndDirection(axis, getAxisDirection(position.get(axis)))));
        var output = major.map(e -> new DirectionsResult(e.getFirst(), e.getSecond()));
        return output.toList().reversed();
    }

    public static <T, U> List<T> getFirst(Collection<Pair<T, U>> list) {
        return list.stream().map(Pair::getFirst).toList();
    }

    public static <T, U> List<U> getSecond(Collection<Pair<T, U>> list) {
        return list.stream().map(Pair::getSecond).toList();
    }
}
