package me.sophur.boards.blockentities;

import me.sophur.boards.BlockBehaviourTags;
import me.sophur.boards.BlockEntityTypes;
import me.sophur.boards.DirectionalMap;
import me.sophur.boards.Util;
import me.sophur.boards.blocks.FrameBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.tags.TagKey;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static me.sophur.boards.BlockBehaviourTags.hasTag;

public class FrameBlockEntity extends BlockEntity {
    public FrameBlockEntity(BlockPos worldPosition, BlockState blockState) {
        super(BlockEntityTypes.FRAME_BLOCK_ENTITY.type(), worldPosition, blockState);
        updateBlockState();
    }

    private DirectionalMap<ItemStack> items = new DirectionalMap<>(ItemStack.EMPTY);
    private static final int itemCount = Direction.values().length;

    private Item getCompatibleFaceItemType(Direction direction) {
        ItemStack itemStack = items.get(direction);
        if (itemStack.isEmpty()) return null;
        Item item = itemStack.getItem();
        if (!hasTag(item, BlockBehaviourTags.FRAME_COMPATIBLE_TAG)) return null;
        return item;
    }

    public boolean hasSolidFace(Direction direction) {
        Item item = getCompatibleFaceItemType(direction);
        if (item == null) return false;

        if (hasTag(item, BlockBehaviourTags.POWERED_TAG)) return false;
        return !hasTag(item, BlockBehaviourTags.NON_CONDUCTIVE_TAG);
    }

    public boolean faceHasTag(Direction direction, TagKey<Item> itemTag) {
        Item item = getCompatibleFaceItemType(direction);
        if (item == null) return false;
        return hasTag(item, itemTag);
    }

    public boolean canSetFaceItem(ItemStack itemStack) {
        if (itemStack.isEmpty()) return true;
        return hasTag(itemStack.getItem(), BlockBehaviourTags.FRAME_COMPATIBLE_TAG);
    }

    public ItemStack getFaceItem(Direction direction) {
        return items.get(direction);
    }

    public void setFaceItem(Direction direction, ItemStack item) {
        if (item == null) item = ItemStack.EMPTY;
        if (!canSetFaceItem(item))
            throw new IllegalArgumentException("Cannot set this " + item + " on the face");
        items.set(direction, item);
        setChanged();
    }

    @Override
    protected void saveAdditional(@NonNull ValueOutput output) {
        super.saveAdditional(output);
        ContainerHelper.saveAllItems(output, this.items.getEntries());
    }

    @Override
    protected void loadAdditional(@NonNull ValueInput input) {
        super.loadAdditional(input);
        items = new DirectionalMap<>(ItemStack.EMPTY);
        ContainerHelper.loadAllItems(input, this.items.getEntries());
    }

    @Override
    public @NonNull CompoundTag getUpdateTag(HolderLookup.@NonNull Provider registries) {
        return saveWithoutMetadata(registries);
    }

    @Override
    public @Nullable Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    public void updateBlockState() {
        if (level == null) return;

        BlockState state = getBlockState();
        if (!(state.getBlock() instanceof FrameBlock block)) return;

        // set new block state
        for (Direction dir : Direction.values()) {
            state = state.setValue(Util.getDirectionalProperty(dir), hasFaceItem(dir));
        }

        state = block.updateShape(state, level, level, worldPosition);
        level.setBlockAndUpdate(worldPosition, state);
    }

    @Override
    public void setChanged() {
        super.setChanged();
        updateBlockState();
    }

    public boolean hasFaceItem(Direction face) {
        return !getFaceItem(face).isEmpty();
    }

    public DirectionalMap<ItemStack> getItems() {
        return items;
    }
}
