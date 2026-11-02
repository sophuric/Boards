package me.sophur.boards;

import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ItemLike;

import static me.sophur.boards.BoardsMain.identifier;

public class BlockBehaviourTags {
    private BlockBehaviourTags() {
    }

    public static final TagKey<Item> FRAME_COMPATIBLE_TAG = TagKey.create(Registries.ITEM, identifier("blocks"));
    public static final TagKey<Item> NON_CONDUCTIVE_TAG = TagKey.create(Registries.ITEM, identifier("non_conductive"));
    public static final TagKey<Item> POWERED_TAG = TagKey.create(Registries.ITEM, identifier("powered"));
    public static final TagKey<Item> SIGNAL_SOURCE = TagKey.create(Registries.ITEM, identifier("signal_source"));

    @SuppressWarnings("deprecation")
    public static boolean hasTag(ItemLike item, TagKey<Item> tag) {
        return item.asItem().builtInRegistryHolder().is(tag);
    }
}
