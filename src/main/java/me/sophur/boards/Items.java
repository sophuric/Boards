package me.sophur.boards;

import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;

public class Items {
    private Items() {
    }

    public static void initialize() {
        CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.REDSTONE_BLOCKS).register(tab -> {
            tab.accept(frameBlock);
        });
    }

    public static BlockItem frameBlock = RegistryUtil.registerBlockItem(Blocks.frameBlock, new Item.Properties());
}
