package me.sophur.boards;

import me.sophur.boards.blocks.FrameBlock;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;

import java.util.Collection;
import java.util.List;
import java.util.function.Function;

public class RegistryUtil {
    private RegistryUtil() {
    }

    public record Registered<T, U extends T>(U entry, ResourceKey<T> key) {
    }

    public static <T extends Block> Registered<Block, T> registerBlock(String id, Function<BlockBehaviour.Properties, T> factory, BlockBehaviour.Properties properties) {
        return registerBlock(BoardsMain.identifier(id), factory, properties);
    }

    public static <T extends Item> Registered<Item, T> registerItem(String id, Function<Item.Properties, T> factory, Item.Properties properties) {
        return registerItem(BoardsMain.identifier(id), factory, properties);
    }

    public static <T extends BlockEntity> Registered<BlockEntityType<?>, BlockEntityType<T>> registerBlockEntityType(String id, FabricBlockEntityTypeBuilder.Factory<? extends T> factory, Collection<Block> blocks) {
        return registerBlockEntityType(BoardsMain.identifier(id), factory, blocks);
    }

    public static <T extends Block> Registered<Block, T> registerBlock(Identifier id, Function<BlockBehaviour.Properties, T> factory, BlockBehaviour.Properties properties) {
        ResourceKey<Block> key = ResourceKey.create(Registries.BLOCK, id);
        T block = factory.apply(properties.setId(key));
        Registry.register(BuiltInRegistries.BLOCK, key, block);
        return new Registered<>(block, key);
    }

    public static <T extends Item> Registered<Item, T> registerItem(Identifier id, Function<Item.Properties, T> factory, Item.Properties properties) {
        ResourceKey<Item> key = ResourceKey.create(Registries.ITEM, id);
        T item = factory.apply(properties.setId(key));
        Registry.register(BuiltInRegistries.ITEM, key, item);
        return new Registered<>(item, key);
    }

    public static <T extends Block> Registered<Item, BlockItem> registerBlockItem(Registered<Block, T> block, Item.Properties properties) {
        return registerItem(block.key.identifier(), prop -> new BlockItem(block.entry, prop.useBlockDescriptionPrefix()), properties);
    }

    public static <T extends BlockEntity> Registered<BlockEntityType<?>, BlockEntityType<T>> registerBlockEntityType(Identifier id, FabricBlockEntityTypeBuilder.Factory<? extends T> factory, Collection<Block> blocks) {
        ResourceKey<BlockEntityType<?>> key = ResourceKey.create(Registries.BLOCK_ENTITY_TYPE, id);
        BlockEntityType<T> blockEntityType = Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, key,
                FabricBlockEntityTypeBuilder.<T>create(factory).addBlocks(blocks).build());
        return new Registered<>(blockEntityType, key);
    }
}
