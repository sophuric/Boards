package me.sophur.boards.blocks;

import com.mojang.serialization.MapCodec;
import me.sophur.boards.Util;
import me.sophur.boards.blockentities.FrameBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public class FrameBlock extends BaseEntityBlock {
    public FrameBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected @NonNull MapCodec<? extends BaseEntityBlock> codec() {
        return simpleCodec(FrameBlock::new);
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(@NonNull BlockPos worldPosition, @NonNull BlockState blockState) {
        return new FrameBlockEntity(worldPosition, blockState);
    }

    private static Direction getFace(BlockPos pos, BlockHitResult hitResult) {
        var localPos = hitResult.getLocation().subtract(new Vec3(pos)).subtract(0.5, 0.5, 0.5);
        return Util.getMajorDirection(localPos);
    }

    @Override
    protected InteractionResult useItemOn(@NonNull ItemStack itemStack, @NonNull BlockState state, Level level, @NonNull BlockPos pos, @NonNull Player player, @NonNull InteractionHand hand, @NonNull BlockHitResult hitResult) {
        if (itemStack.isEmpty()) return InteractionResult.TRY_WITH_EMPTY_HAND;

        if (level.getBlockEntity(pos) instanceof FrameBlockEntity be) {
            Direction face = getFace(pos, hitResult);
            if (!be.hasFaceItem(face) && be.canSetFaceItem(itemStack)) {
                itemStack = itemStack.consumeAndReturn(1, player); // decrease item count from player
                itemStack.setCount(1);
                be.setFaceItem(face, itemStack);
                return InteractionResult.SUCCESS;
            }
        }
        return InteractionResult.PASS;
    }

    @Override
    protected InteractionResult useWithoutItem(@NonNull BlockState state, Level level, @NonNull BlockPos pos, @NonNull Player player, @NonNull BlockHitResult hitResult) {
        // way for creative players to be able to change the faces
        if (level.getBlockEntity(pos) instanceof FrameBlockEntity be) {
            Direction face = getFace(pos, hitResult);
            var itemStack = be.getFaceItem(face);
            if (!itemStack.isEmpty()) {
                itemStack = itemStack.copyAndClear(); // remove item from face
                if (!player.hasInfiniteMaterials())
                    Block.popResourceFromFace(level, pos, face, itemStack);
                be.setChanged();
                return InteractionResult.SUCCESS;
            }
        }
        return InteractionResult.PASS;
    }
}
