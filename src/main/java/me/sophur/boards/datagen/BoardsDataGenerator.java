package me.sophur.boards.datagen;

import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import org.jspecify.annotations.NonNull;

public class BoardsDataGenerator implements DataGeneratorEntrypoint {
    @Override
    public void onInitializeDataGenerator(@NonNull FabricDataGenerator fabricDataGenerator) {
        var pack = fabricDataGenerator.createPack();

        pack.addProvider(BoardsBlockTagProvider::new);
        pack.addProvider(BoardsItemTagProvider::new);
        pack.addProvider(BoardsBlockLootTableProvider::new);
        pack.addProvider(BoardsRecipeProvider::new);
    }
}
