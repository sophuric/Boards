package me.sophur.boards;

import me.sophur.boards.blockentities.FrameBlockEntity;
import me.sophur.boards.blocks.FrameBlock;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;

import java.util.List;

public class BlockEntityTypes {
    private BlockEntityTypes() {
    }

    public static void initialize() {
    }

    public static RegistryUtil.IDBlockEntityType<? extends FrameBlockEntity> frameBlock =
            RegistryUtil.registerBlockEntityType("frame", FrameBlockEntity::new, List.of(Blocks.frameBlock));
}

