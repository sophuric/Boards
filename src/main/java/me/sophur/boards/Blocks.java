package me.sophur.boards;

import me.sophur.boards.blocks.FrameBlock;
import net.fabricmc.fabric.api.registry.FlammableBlockRegistry;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.material.MapColor;

public class Blocks {
    private Blocks() {
    }

    public static void initialize() {
    }

    private static final FlammableBlockRegistry FLAMMABLE = FlammableBlockRegistry.getDefaultInstance();

    public final static FrameBlock FRAME_BLOCK = RegistryUtil.registerBlock(
        "frame", FrameBlock::new, BlockBehaviour.Properties.of()
            .mapColor(MapColor.WOOD).instrument(NoteBlockInstrument.BASS)
            .strength(2.0F, 3.0F).sound(SoundType.WOOD)
            .ignitedByLava());

    static {
        FLAMMABLE.add(FRAME_BLOCK, 5, 20);
    }
}

