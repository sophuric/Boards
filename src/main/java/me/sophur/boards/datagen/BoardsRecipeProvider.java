package me.sophur.boards.datagen;

import me.sophur.boards.BoardsItems;
import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricRecipeProvider;
import net.minecraft.advancements.Advancement;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Recipe;
import org.jspecify.annotations.NonNull;

import java.util.concurrent.CompletableFuture;

public class BoardsRecipeProvider extends FabricRecipeProvider {
    public BoardsRecipeProvider(FabricPackOutput output, CompletableFuture<HolderLookup.Provider> registriesFuture) {
        super(output, registriesFuture);
    }

    @Override
    protected @NonNull RecipeProvider createRecipeProvider(HolderLookup.@NonNull Provider registries, @NonNull BootstrapContext<Recipe<?>> recipes, @NonNull BootstrapContext<Advancement> advancements) {
        return new RecipeProvider(recipes, advancements) {
            @Override
            public void buildRecipes() {
                var items = registries.lookupOrThrow(Registries.BLOCK);
                shaped(RecipeCategory.REDSTONE, BoardsItems.FRAME_BLOCK, 4)
                    .pattern(" s ")
                    .pattern("sss")
                    .pattern(" s ")
                    .define('s', Items.STICK)
                    .unlockedBy(getHasName(Items.REDSTONE), has(Items.REDSTONE))
                    .save(output);
            }
        };
    }

    @Override
    public @NonNull String getName() {
        return "BoardsRecipeProvider";
    }
}
