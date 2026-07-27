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
    // Controls the item's item_name component. In effect, this changes its display name.
    // Unlike <@link mechanism ItemTag.display>, this name cannot be changed by an anvil, but will be hidden when the display is set, such as with an anvil.
    // That is, it will remain under the display name and be shown again if the display is removed.
    // See also <@link language Item Components>.
    // @mechanism
    // Provide no input to reset the item to its default value.
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
