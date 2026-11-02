package me.sophur.boards.datagen;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public class DummyBlockGetter implements BlockGetter {
    // dummy BlockGetter for generating block tags, since some Block methods require a level and position

    public static final BlockPos POSITION = new BlockPos(0, 4, 0);
    private BlockState state;

    public BlockState getState() {
        return state;
    }

    public void setState(BlockState state) {
        this.state = state;
    }

    @Override
    public @Nullable BlockEntity getBlockEntity(@NonNull BlockPos pos) {
        return null;
    }

    @Override
    public @NonNull BlockState getBlockState(@NonNull BlockPos pos) {
        return pos.equals(POSITION) ? state : Blocks.AIR.defaultBlockState();
    }

    @Override
    public @NonNull FluidState getFluidState(@NonNull BlockPos pos) {
        return Fluids.EMPTY.defaultFluidState();
    }

    @Override
    public int getHeight() {
        return 8;
    }

    @Override
    public int getMinY() {
        return 0;
    }
}
