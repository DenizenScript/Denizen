package com.denizenscript.denizen.paper.datacomponents;

import io.papermc.paper.datacomponent.DataComponentTypes;

public class GliderAdapter extends DataComponentAdapter.NonValued {

    // <--[property]
    // @object ItemTag
    // @name glider
    // @input ElementTag(Boolean)
    // @description
    // Controls whether an item can be used to glide when equipped (like elytras by default), see <@link language Item Components>.
    // @mechanism
    // Provide no input to reset the item to its default value.
    // -->

    public GliderAdapter() {
        super(DataComponentTypes.GLIDER, "glider");
    }
}
