package me.sophur.boards.util;

import com.mojang.datafixers.util.Pair;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.NonNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;

import static net.minecraft.world.level.block.state.properties.BlockStateProperties.WATERLOGGED;

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

    public static BlockState getWaterloggedStateForPlacement(BlockState state, @NonNull BlockPlaceContext context) {
        return state.setValue(WATERLOGGED,
            context.getLevel().getFluidState(context.getClickedPos()).is(Fluids.WATER));
    }

    public static @NonNull FluidState getWaterloggedFluidState(BlockState state, Function<BlockState, FluidState> superFunc) {
        return state.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : superFunc.apply(state);
    }

    public static void updateShapeWaterlogged(@NonNull BlockState state, @NonNull LevelReader level, @NonNull ScheduledTickAccess ticks, @NonNull BlockPos pos) {
        if (!state.getValue(WATERLOGGED)) return;
        ticks.scheduleTick(pos, Fluids.WATER, Fluids.WATER.getTickDelay(level));
    }

    public static boolean mergeItemStack(ItemStack stack1, ItemStack stack2) {
        if (stack1.isEmpty() || stack2.isEmpty()) return false;
        if (!stack1.isStackable()) return false;
        if (!ItemStack.isSameItemSameComponents(stack1, stack2)) return false;

        int stack1Count = stack1.getCount(),
            maxCount = stack1.getMaxStackSize();
        if (stack1Count >= maxCount) return false;

        int combinedCount = stack1Count + stack2.getCount();
        stack1.setCount(Math.min(maxCount, combinedCount));
        stack2.setCount(Math.max(0, combinedCount - maxCount));
        return true;
    }

    public static List<ItemStack> stackItems(Stream<ItemStack> stacks) {
        List<ItemStack> result = new ArrayList<>();
        stacks.forEach(stack -> {
            if (stack.isEmpty()) return;

            if (stack.isStackable())
                for (ItemStack existing : result) {
                    mergeItemStack(existing, stack);
                }

            if (!stack.isEmpty())
                result.add(stack);
        });
        return result;
    }

    public static <T> Stream<T> stream(Pair<T, T> pair) {
        return Stream.of(pair.getFirst(), pair.getSecond());
    }
}
