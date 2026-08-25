package com.denizenscript.denizen.paper.datacomponents;

import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.core.ElementTag;
import io.papermc.paper.datacomponent.DataComponentTypes;
import net.kyori.adventure.text.Component;
import com.denizenscript.denizen.paper.PaperModule;
import net.md_5.bungee.api.ChatColor;

public class ItemNameAdapter extends DataComponentAdapter.Valued<ElementTag, Component> {

    // <--[property]
    // @object ItemTag
    // @name item_name
    // @input ElementTag
    // @description
    // Controls the item's default name.
    // The item's default name cannot be modified by a player and is displayed when <@link mechanism ItemTag.display> is not set.
    // If a display name is set, item_name will still retain its original value but will be hidden.
    // See also <@link language Item Components>.
    // @mechanism
    // Provide no input to reset the item to its default value.
    // @tag
    // By default, this will return the item's vanilla display name.
    // -->

    public ItemNameAdapter() {
        super(ElementTag.class, DataComponentTypes.ITEM_NAME, "item_name");
    }

    @Override
    public ElementTag toDenizen(Component value) {
        return new ElementTag(PaperModule.stringifyComponent(value), true);
    }

    @Override
    public Component fromDenizen(ElementTag value, Mechanism mechanism) {
        return PaperModule.parseFormattedText(value.toString(), ChatColor.WHITE);
    }
}
