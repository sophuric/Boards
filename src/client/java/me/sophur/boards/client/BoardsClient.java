package me.sophur.boards.client;

import me.sophur.boards.client.models.FrameBlockStateModel;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.model.loading.v1.CustomUnbakedBlockStateModel;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin;

import static me.sophur.boards.BoardsMain.identifier;

public final class BoardsClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        CustomUnbakedBlockStateModel.register(identifier("frame_boards"), FrameBlockStateModel.Unbaked.CODEC);
    }
}
