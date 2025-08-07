package com.denizenscript.denizen.paper.datacomponents;

import com.denizenscript.denizen.paper.utilities.DataComponentAdapter;

public class ComponentAdaptersRegistry {

    public static void register() {
        DataComponentAdapter.register(new FoodAdapter());
    }
}
