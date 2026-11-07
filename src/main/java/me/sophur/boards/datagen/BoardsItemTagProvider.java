package me.sophur.boards.datagen;

import me.sophur.boards.BlockBehaviourTags;
import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagsProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.NonNull;

import java.util.Set;
import java.util.concurrent.CompletableFuture;

import static me.sophur.boards.datagen.DummyBlockGetter.POSITION;

public class BoardsItemTagProvider extends FabricTagsProvider.ItemTagsProvider {
    public BoardsItemTagProvider(FabricPackOutput output, CompletableFuture<HolderLookup.Provider> registryLookupFuture) {
        super(output, registryLookupFuture);
    }

    private static final Set<Class<? extends Block>> allowedBlockTypes = Set.of(
        Block.class,
        HalfTransparentBlock.class,
        TransparentBlock.class,
        NetherrackBlock.class,
        StainedGlassBlock.class,
        PoweredBlock.class,
        TargetBlock.class,
        BarrierBlock.class
    );

    @Override
    protected void addTags(HolderLookup.@NonNull Provider registries) {
        var tempLevel = new DummyBlockGetter();

        var frameCompatible = tag(BlockBehaviourTags.FRAME_COMPATIBLE_TAG);
        var nonConductive = tag(BlockBehaviourTags.NON_CONDUCTIVE_TAG);
        var powered = tag(BlockBehaviourTags.POWERED_TAG);
        var signalSource = tag(BlockBehaviourTags.SIGNAL_SOURCE);

        registries.lookupOrThrow(Registries.BLOCK)
            .listElements()
            .forEach(holder -> {
                Block block = holder.value();
                Item item = block.asItem();
                if (item == Items.AIR) return;

                BlockState blockState = block.defaultBlockState();
                tempLevel.setState(blockState);

                if (!blockState.isCollisionShapeFullBlock(tempLevel, POSITION)) return;
                if (!allowedBlockTypes.contains(block.getClass())) return;

                @SuppressWarnings("deprecation")
                var key = item.builtInRegistryHolder().key();

                frameCompatible.addOptional(key);

                if (block instanceof PoweredBlock) powered.addOptional(key);
                if (!blockState.isRedstoneConductor(tempLevel, POSITION)) nonConductive.addOptional(key);
                if (blockState.isSignalSource()) signalSource.addOptional(key);
            });
    }
}
