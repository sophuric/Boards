package me.sophur.boards.util;

import java.util.function.*;

public final class Box<T> {
    public Box(T value) {
        this.value = value;
    }

    public static <A> Box<A> of(A a) {
        return new Box<>(a);
    }

    public T get() {
        return value;
    }

    public void set(T value) {
        this.value = value;
    }

    public T value;

    public static <T, R> Function<Box<T>, R> boxFunction(Function<T, R> function) {
        return box -> function.apply(box.value);
    }

    public static <T, R> Function<T, R> unboxFunction(Function<Box<T>, R> function) {
        return (value) -> function.apply(of(value));
    }

    public static <T> Predicate<Box<T>> boxPredicate(Predicate<T> function) {
        return box -> function.test(box.value);
    }

    public static <T> Predicate<T> unboxPredicate(Predicate<Box<T>> function) {
        return (value) -> function.test(of(value));
    }

    public static <T> Consumer<Box<T>> boxConsumer(Consumer<T> consumer) {
        return box -> consumer.accept(box.value);
    }

    public static <T> Consumer<T> unboxConsumer(Consumer<Box<T>> consumer) {
        return (value) -> consumer.accept(of(value));
    }

    public static final class Boolean {
        public Boolean(boolean value) {
            this.value = value;
        }

        public boolean get() {
            return value;
        }

        public void set(boolean value) {
            this.value = value;
        }

        public boolean value;
    }

    public static final class Character {
        public Character(char value) {
            this.value = value;
        }

        public char get() {
            return value;
        }

        public void set(char value) {
            this.value = value;
        }

        public char value;
    }

    public static final class Byte {
        public Byte(byte value) {
            this.value = value;
        }

        public byte get() {
            return value;
        }

        public void set(byte value) {
            this.value = value;
        }

        public byte value;
    }

    public static final class Short {
        public Short(short value) {
            this.value = value;
        }

        public short get() {
            return value;
        }

        public void set(short value) {
            this.value = value;
        }

        public short value;
    }

    public static final class Integer {
        public Integer(int value) {
            this.value = value;
        }

        public int get() {
            return value;
        }

        public void set(int value) {
            this.value = value;
        }

        public int value;
    }

    public static final class Long {
        public Long(long value) {
            this.value = value;
        }

        public long get() {
            return value;
        }

        public void set(long value) {
            this.value = value;
        }

        public long value;
    }

    public static final class Float {
        public Float(float value) {
            this.value = value;
        }

        public float get() {
            return value;
        }

        public void set(float value) {
            this.value = value;
        }

        public float value;
    }

    public static final class Double {
        public Double(double value) {
            this.value = value;
        }

        public double get() {
            return value;
        }

        public void set(double value) {
            this.value = value;
        }

        public double value;
    }
}
