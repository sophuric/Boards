package me.sophur.boards;

import me.sophur.boards.blockentities.FrameBlockEntity;
import me.sophur.boards.util.RegistryUtil;

import java.util.List;

public class BoardsBlockEntityTypes {
    private BoardsBlockEntityTypes() {
    }

    public static void initialize() {
    }

    public final static RegistryUtil.IDBlockEntityType<? extends FrameBlockEntity> FRAME_BLOCK_ENTITY =
        RegistryUtil.registerBlockEntityType("frame", FrameBlockEntity::new, List.of(BoardsBlocks.FRAME_BLOCK));
}

