package com.denizenscript.denizen.paper.datacomponents;

import com.denizenscript.denizen.utilities.Utilities;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.core.ElementTag;
import io.papermc.paper.datacomponent.DataComponentTypes;
import net.kyori.adventure.key.Key;

public class ItemModelAdapter extends DataComponentAdapter.Valued<ElementTag, Key> {

    // <--[property]
    // @object ItemTag
    // @name item_model
    // @input ElementTag
    // @description
    // Controls an item's model <@link language Item Components> in namespaced key format.
    // The default namespace is "minecraft", so for example an input of "stone" becomes "minecraft:stone", and will set the item model to a stone block.
    // This can also be used to display item models from your own custom resource packs.
    // @mechanism
    // Provide no input to reset the item to its default value.
    // -->

    public ItemModelAdapter() {
        super(ElementTag.class, DataComponentTypes.ITEM_MODEL, "item_model");
    }

    @Override
    public ElementTag toDenizen(Key value) {
        return new ElementTag(value.asMinimalString(), true);
    }

    @Override
    public Key fromDenizen(ElementTag value, Mechanism mechanism) {
        return Utilities.parseNamespacedKey(value.asString());
    }
}
