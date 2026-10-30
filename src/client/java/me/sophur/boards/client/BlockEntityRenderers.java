package me.sophur.boards.client;

import me.sophur.boards.BlockEntityTypes;
import me.sophur.boards.client.blockentityrenderers.FrameBlockEntityRenderer;

import static net.minecraft.client.renderer.blockentity.BlockEntityRenderers.register;

public class BlockEntityRenderers {
    private BlockEntityRenderers() {
    }

    public static void initialize() {
    }

    static {
        register(BlockEntityTypes.FRAME_BLOCK_ENTITY.type(), FrameBlockEntityRenderer::new);
    }
}
