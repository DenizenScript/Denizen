package com.denizenscript.denizen.paper.datacomponents;

public class ComponentAdaptersRegistry {

    public static void register() {
        DataComponentAdapter.register(new FoodAdapter());
        DataComponentAdapter.register(new GliderAdapter());
        DataComponentAdapter.register(new ItemModelAdapter());
        DataComponentAdapter.register(new MaxDurabilityAdapter());
        DataComponentAdapter.register(new MaxStackSizeAdapter());
        DataComponentAdapter.register(new RarityAdapter());
    }
}
