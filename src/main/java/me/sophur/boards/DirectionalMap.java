package me.sophur.boards;

import com.mojang.datafixers.util.Pair;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;

import java.util.Arrays;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;

public class DirectionalMap<T> {
    private final NonNullList<T> entries;
    private final T defaultValue;

    public DirectionalMap(T defaultValue) {
        this.defaultValue = defaultValue;
        entries = NonNullList.withSize(6, defaultValue);
    }

    public DirectionalMap(DirectionalMap<T> other) {
        this(other.defaultValue);
        for (int i = 0; i < entries.size(); ++i) {
            entries.set(i, other.entries.get(i));
        }
    }

    public void set(Direction direction, T entry) {
        entries.set(direction.ordinal(), entry);
    }

    public T get(Direction direction) {
        return entries.get(direction.ordinal());
    }

    public <U> DirectionalMap<U> map(BiFunction<Direction, T, U> mapper, U defaultValue) {
        var newMap = new DirectionalMap<>(defaultValue);
        for (Direction direction : Direction.values()) {
            var newEntry = mapper.apply(direction, get(direction));
            newMap.set(direction, newEntry);
        }
        return newMap;
    }

    public <U> DirectionalMap<U> map(Function<T, U> mapper, U defaultValue) {
        return map((_, val) -> mapper.apply(val), defaultValue);
    }

    public void forEach(BiConsumer<Direction, T> action) {
        for (Direction direction : Direction.values()) {
            action.accept(direction, get(direction));
        }
    }

    public void forEach(Consumer<T> action) {
        forEach((_, val) -> action.accept(val));
    }

    public Stream<Pair<Direction, T>> streamKeyValue() {
        return Arrays.stream(Direction.values())
            .map(dir -> Pair.of(dir, get(dir)));
    }

    public Stream<T> stream() {
        return entries.stream();
    }

    public NonNullList<T> getEntries() {
        return entries;
    }

    public T getDefaultValue() {
        return defaultValue;
    }
}
