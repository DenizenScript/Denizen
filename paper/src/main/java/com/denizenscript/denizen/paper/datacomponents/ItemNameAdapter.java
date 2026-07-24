package com.denizenscript.denizen.paper.datacomponents;

import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.core.ElementTag;
import io.papermc.paper.datacomponent.DataComponentTypes;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

public class ItemNameAdapter extends DataComponentAdapter.Valued<ElementTag, Component> {

    // <--[property]
    // @object ItemTag
    // @name item_name
    // @input ElementTag
    // @description
    // Controls the item's item name component. This exists under the item's <@link mechanism ItemTag.display> and persists if it is renamed in an anvil.
    // See also <@link language Item Components>.
    // @mechanism
    // Provide no input to reset the item to its default value.
    // -->

    public ItemNameAdapter() {
        super(ElementTag.class, DataComponentTypes.ITEM_NAME, "item_name");
    }

    @Override
    public ElementTag toDenizen(Component value) {
        return new ElementTag(LegacyComponentSerializer.legacyAmpersand().serialize(value));
    }

    @Override
    public Component fromDenizen(ElementTag value, Mechanism mechanism) {
        return Component.text(value.toString());
    }
}
