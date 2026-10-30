package me.sophur.boards.client.blockentityrenderers;

import com.mojang.blaze3d.vertex.PoseStack;
import me.sophur.boards.blockentities.FrameBlockEntity;
import me.sophur.boards.client.blockentityrenderstates.FrameBlockEntityRenderState;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public class FrameBlockEntityRenderer implements BlockEntityRenderer<FrameBlockEntity, FrameBlockEntityRenderState> {
    public FrameBlockEntityRenderer(BlockEntityRendererProvider.Context context) {

    }

    @Override
    public @NonNull FrameBlockEntityRenderState createRenderState() {
        return new FrameBlockEntityRenderState();
    }

    @Override
    public void extractRenderState(@NonNull FrameBlockEntity blockEntity, @NonNull FrameBlockEntityRenderState state, float partialTicks, @NonNull Vec3 cameraPosition, ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(blockEntity, state, partialTicks, cameraPosition, breakProgress);
    }

    @Override
    public void submit(@NonNull FrameBlockEntityRenderState state, @NonNull PoseStack poseStack, @NonNull SubmitNodeCollector submitNodeCollector, @NonNull CameraRenderState camera) {

    }
}
