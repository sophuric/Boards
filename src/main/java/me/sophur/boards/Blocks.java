package me.sophur.boards;

import me.sophur.boards.blocks.FrameBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;

public class Blocks {
    private Blocks() {
    }

    public static void initialize() {
    }

    public static RegistryUtil.Registered<Block, FrameBlock> frameBlock = RegistryUtil.registerBlock("frame", FrameBlock::new, BlockBehaviour.Properties.of());
}

