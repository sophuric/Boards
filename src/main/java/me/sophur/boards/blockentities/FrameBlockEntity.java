package me.sophur.boards.blockentities;

import me.sophur.boards.BlockEntityTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public class FrameBlockEntity extends BlockEntity {
    public FrameBlockEntity(BlockPos worldPosition, BlockState blockState) {
        super(BlockEntityTypes.FRAME_BLOCK_ENTITY.type(), worldPosition, blockState);
        this.items = NonNullList.withSize(itemCount, ItemStack.EMPTY);
    }

    private NonNullList<ItemStack> items;
    private static final int itemCount = Direction.values().length;

    public boolean canSetFaceItem(ItemStack item) {
        if (item == null || item.isEmpty()) return true;
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

    @Override
    public void setChanged() {
        super.setChanged();
        if (level == null) return;

        BlockState state = getBlockState();
        level.sendBlockUpdated(worldPosition, state, state, Block.UPDATE_ALL);
        level.updateNeighborsAt(worldPosition, getBlockState().getBlock());
    }

    public boolean hasFaceItem(Direction face) {
        return !getFaceItem(face).isEmpty();
    }
}
