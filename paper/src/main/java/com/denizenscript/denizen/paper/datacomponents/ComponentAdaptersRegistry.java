package com.denizenscript.denizen.paper.datacomponents;

public class ComponentAdaptersRegistry {

    public static void register() {
        DataComponentAdapter.register(new FoodAdapter());
        DataComponentAdapter.register(new GliderAdapter());
    }
}
