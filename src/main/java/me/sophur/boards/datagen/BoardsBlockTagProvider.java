package me.sophur.boards.datagen;

import me.sophur.boards.util.RegistryUtil;
import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagsProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.Block;
import org.jspecify.annotations.NonNull;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;

import static me.sophur.boards.BoardsBlocks.FRAME_BLOCK;

public class BoardsBlockTagProvider extends FabricTagsProvider.BlockTagsProvider {
    public BoardsBlockTagProvider(FabricPackOutput output, CompletableFuture<HolderLookup.Provider> registryLookupFuture) {
        super(output, registryLookupFuture);
    }

    @Override
    protected void addTags(HolderLookup.@NonNull Provider registries) {
        tag(BlockTags.MINEABLE_WITH_AXE)
            .add(key(FRAME_BLOCK));
    }

    private ResourceKey<Block> key(Block block) {
        return Objects.requireNonNull(RegistryUtil.getBlockResourceKey(block));
    }
}
