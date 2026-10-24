package me.sophur.boards.blockentities;

import me.sophur.boards.BlockEntityTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ShelfBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jspecify.annotations.NonNull;

public class FrameBlockEntity extends BlockEntity {
    public FrameBlockEntity(BlockPos worldPosition, BlockState blockState) {
        super(BlockEntityTypes.frameBlock.type(), worldPosition, blockState);
        this.items = NonNullList.withSize(itemCount, ItemStack.EMPTY);
    }

    private NonNullList<ItemStack> items;
    private static final int itemCount = Direction.values().length;

    public boolean canSetFaceItem(Direction direction, ItemStack item) {
        if (item == null) item = ItemStack.EMPTY;
        //return state.isCollisionShapeFullBlock();
        return true;
    }

    public ItemStack getFaceItem(Direction direction) {
        return items.get(direction.get3DDataValue());
    }

    public boolean setFaceItem(Direction direction, ItemStack item) {
        if (item == null) item = ItemStack.EMPTY;
        if (!canSetFaceItem(direction, item)) return false;
        items.set(direction.get3DDataValue(), item);
        setChanged();
        return true;
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
}
