package com.denizenscript.denizen.paper.datacomponents;

import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.core.ElementTag;
import io.papermc.paper.datacomponent.DataComponentTypes;

public class MaxDurabilityAdapter extends DataComponentAdapter.Valued<ElementTag, Integer> {

    // <--[property]
    // @object ItemTag
    // @name max_durability
    // @input ElementTag(Number)
    // @description
    // Controls the maximum durability (number of uses) of this item.
    // For use with <@link tag ItemTag.durability> and <@link mechanism ItemTag.durability>.
    // See also <@link language Item Components>.
    // @mechanism
    // Provide no input to reset the item to its default value.
    // -->

    public MaxDurabilityAdapter() {
        super(ElementTag.class, DataComponentTypes.MAX_DAMAGE, "max_durability");
    }

    @Override
    public ElementTag toDenizen(Integer value) {
        return new ElementTag(value);
    }

    @Override
    public Integer fromDenizen(ElementTag value, Mechanism mechanism) {
        return mechanism.requireInteger() ? value.asInt() : null;
    }
}
