package me.sophur.boards;

import me.sophur.boards.util.RegistryUtil;
import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;

public class BoardsItems {
    private BoardsItems() {
    }

    public static void initialize() {
        CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.REDSTONE_BLOCKS).register(tab -> {
            tab.accept(FRAME_BLOCK);
        });
    }

    public final static BlockItem FRAME_BLOCK = RegistryUtil.registerBlockItem(BoardsBlocks.FRAME_BLOCK, new Item.Properties());
}
