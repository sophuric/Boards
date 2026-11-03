package me.sophur.boards.util;

import me.sophur.boards.BoardsMain;
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
import org.jspecify.annotations.Nullable;

import java.util.Collection;
import java.util.Objects;
import java.util.function.Function;

public class RegistryUtil {
    private RegistryUtil() {
    }

    public record IDBlockEntityType<T extends BlockEntity>
        (BlockEntityType<T> type, ResourceKey<BlockEntityType<?>> key) {
    }

    public static boolean resourceKeyEquals(ResourceKey<?> a, ResourceKey<?> b) {
        if (a == b) return true;
        if (a == null || b == null) return false;
        return Objects.equals(a.registry(), b.registry()) && Objects.equals(a.identifier(), b.identifier());
    }

    public static <T extends Block> T registerBlock(String id, Function<BlockBehaviour.Properties, T> factory, BlockBehaviour.Properties properties) {
        return registerBlock(BoardsMain.identifier(id), factory, properties);
    }

    public static <T extends Item> T registerItem(String id, Function<Item.Properties, T> factory, Item.Properties properties) {
        return registerItem(BoardsMain.identifier(id), factory, properties);
    }

    public static <T extends BlockEntity> IDBlockEntityType<T> registerBlockEntityType(String id, FabricBlockEntityTypeBuilder.Factory<? extends T> factory, Collection<Block> blocks) {
        return registerBlockEntityType(BoardsMain.identifier(id), factory, blocks);
    }

    public static <T extends Block> T registerBlock(Identifier id, Function<BlockBehaviour.Properties, T> factory, BlockBehaviour.Properties blockProperties) {
        ResourceKey<Block> key = ResourceKey.create(Registries.BLOCK, Objects.requireNonNull(id));
        T block = factory.apply(blockProperties.setId(key));
        assert resourceKeyEquals(key, getBlockResourceKey(block));
        return Registry.register(BuiltInRegistries.BLOCK, key, block);
    }

    public static <T extends Item> T registerItem(Identifier id, Function<Item.Properties, T> factory, Item.Properties itemProperties) {
        ResourceKey<Item> key = ResourceKey.create(Registries.ITEM, Objects.requireNonNull(id));
        T item = factory.apply(itemProperties.setId(key));
        return Registry.register(BuiltInRegistries.ITEM, key, item);
    }

    public static <T extends Block> BlockItem registerBlockItem(T block, Item.Properties itemProperties) {
        return registerItem(getBlockID(block), prop -> new BlockItem(block, prop.useBlockDescriptionPrefix()), itemProperties);
    }

    public static <T extends BlockEntity> IDBlockEntityType<T> registerBlockEntityType(Identifier id, FabricBlockEntityTypeBuilder.Factory<? extends T> factory, Collection<Block> blocks) {
        ResourceKey<BlockEntityType<?>> key = ResourceKey.create(Registries.BLOCK_ENTITY_TYPE, Objects.requireNonNull(id));
        BlockEntityType<T> blockEntityType = Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, key,
            FabricBlockEntityTypeBuilder.<T>create(factory).addBlocks(blocks).build());
        return new IDBlockEntityType<>(blockEntityType, key);
    }

    public static @Nullable ResourceKey<Block> getBlockResourceKey(Block block) {
        return block.properties().blockId();
    }

    public static @Nullable Identifier getBlockID(Block block) {
        ResourceKey<Block> key = getBlockResourceKey(block);
        return key != null ? key.identifier() : null;
    }
}
