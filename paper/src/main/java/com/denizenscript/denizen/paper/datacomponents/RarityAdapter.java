package com.denizenscript.denizen.paper.datacomponents;

import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.core.ElementTag;
import io.papermc.paper.datacomponent.DataComponentTypes;
import org.bukkit.inventory.ItemRarity;

public class RarityAdapter extends DataComponentAdapter.Valued<ElementTag, ItemRarity> {

    // <--[property]
    // @object ItemTag
    // @name rarity
    // @input ElementTag
    // @description
    // Controls an item's rarity <@link language Item Components>.
    // See <@link url https://jd.papermc.io/paper/org/bukkit/inventory/ItemRarity.html> for valid rarity values.
    // @mechanism
    // Provide no input to reset the item to its default value.
    // -->

    public RarityAdapter() {
        super(ElementTag.class, DataComponentTypes.RARITY, "rarity");
    }

    @Override
    public ElementTag toDenizen(ItemRarity value) {
        return new ElementTag(value);
    }

    @Override
    public ItemRarity fromDenizen(ElementTag value, Mechanism mechanism) {
        return mechanism.requireEnum(ItemRarity.class) ? value.asEnum(ItemRarity.class) : null;
    }
}
