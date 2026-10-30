package me.sophur.boards.blockentities;

import me.sophur.boards.BlockEntityTypes;
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
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

public class FrameBlockEntity extends BlockEntity {
    public FrameBlockEntity(BlockPos worldPosition, BlockState blockState) {
        super(BlockEntityTypes.FRAME_BLOCK_ENTITY.type(), worldPosition, blockState);
        this.items = NonNullList.withSize(itemCount, ItemStack.EMPTY);
    }

    private NonNullList<ItemStack> items;
    private static final int itemCount = Direction.values().length;

    public boolean canSetFaceItem(ItemStack itemStack) {
        if (itemStack == null || itemStack.isEmpty()) return true;
        Item item = itemStack.getItem();
        if (!(item instanceof BlockItem blockItem)) return false;
        Block block = blockItem.getBlock();

        //return state.isCollisionShapeFullBlock();
        return true;
    }

    public ItemStack getFaceItem(Direction direction) {
        return items.get(direction.get3DDataValue());
    }

    public void setFaceItem(Direction direction, ItemStack item) {
        if (!canSetFaceItem(item))
            throw new IllegalArgumentException("Cannot set this " + item + " on the face");
        if (item == null) item = ItemStack.EMPTY;
        items.set(direction.get3DDataValue(), item);
        setChanged();
    }

    @Override
    protected void saveAdditional(@NonNull ValueOutput output) {
        super.saveAdditional(output);
        ContainerHelper.saveAllItems(output, this.items);
    }

    @Override
    protected void loadAdditional(@NonNull ValueInput input) {
        super.loadAdditional(input);
        this.items = NonNullList.withSize(itemCount, ItemStack.EMPTY);
        ContainerHelper.loadAllItems(input, this.items);
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

    public void loopItems(Consumer<ItemStack> consumer) {
        items.forEach(consumer);
    }

    public List<ItemStack> getItems() {
        return Collections.unmodifiableList(items);
    }
}
