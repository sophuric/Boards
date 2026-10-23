package me.sophur.boards.blockentities;

import me.sophur.boards.BlockEntityTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class FrameBlockEntity extends BlockEntity {
    public FrameBlockEntity(BlockPos worldPosition, BlockState blockState) {
        super(BlockEntityTypes.frameBlock.type(), worldPosition, blockState);
    }
}
