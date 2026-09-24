package com.denizenscript.denizen.paper.datacomponents;

import com.denizenscript.denizen.utilities.Utilities;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.core.ElementTag;
import io.papermc.paper.datacomponent.DataComponentTypes;
import net.kyori.adventure.key.Key;

public class BreakSoundAdapter extends DataComponentAdapter.Valued<ElementTag, Key> {

    // <--[property]
    // @object ItemTag
    // @name break_sound
    // @input ElementTag
    // @description
    // Controls an item's break sound <@link language Item Components> in namespaced key format.
    // The default namespace is "minecraft", so for example an input of "block.anvil.land" becomes "minecraft:block.anvil.land".
    // @mechanism
    // Provide no input to reset the item to its default value.
    // -->

    public BreakSoundAdapter() {
        super(ElementTag.class, DataComponentTypes.BREAK_SOUND, "break_sound");
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
