package com.denizenscript.denizen.utilities.inventory;

import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.RecipeChoice;

public class BrewingRecipe {

    public RecipeChoice input;
    public RecipeChoice ingredient;
    public ItemStack result;

    public BrewingRecipe(RecipeChoice ingredient, RecipeChoice input, ItemStack result) {
        this.ingredient = ingredient;
        this.input = input;
        this.result = result;
    }
}
