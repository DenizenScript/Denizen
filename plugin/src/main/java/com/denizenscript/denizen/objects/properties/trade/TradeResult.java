package com.denizenscript.denizen.objects.properties.trade;

import com.denizenscript.denizen.objects.ItemTag;
import com.denizenscript.denizen.objects.TradeTag;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.properties.Property;
import com.denizenscript.denizencore.objects.properties.PropertyParser;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.MerchantRecipe;

public class TradeResult implements Property {

    public static boolean describes(ObjectTag recipe) {
        return recipe instanceof TradeTag;
    }

    public static TradeResult getFrom(ObjectTag recipe) {
        if (!describes(recipe)) {
            return null;
        }
        return new TradeResult((TradeTag) recipe);
    }

    public static final String[] handledMechs = new String[] {
            "result"
    };

    private TradeTag recipe;

    public TradeResult(TradeTag recipe) {
        this.recipe = recipe;
    }

    public String getPropertyString() {
        if (recipe.getRecipe() == null) {
            return null;
        }
        return (new ItemTag(recipe.getRecipe().getResult())).identify();
    }

    public String getPropertyId() {
        return "result";
    }

    public static void registerTags() {

        // <--[tag]
        // @attribute <TradeTag.result>
        // @returns ItemTag
        // @mechanism TradeTag.result
        // @description
        // Returns what the trade will give the player.
        // -->
        PropertyParser.<TradeResult>registerTag("result", (attribute, recipe) -> {
            return new ItemTag(recipe.recipe.getRecipe().getResult());
        });
    }

    public void adjust(Mechanism mechanism) {

        // <--[mechanism]
        // @object TradeTag
        // @name result
        // @input ItemTag
        // @description
        // Sets what the trade will give the player.
        // @tags
        // <TradeTag.result>
        // -->
        if (mechanism.matches("result") && mechanism.requireObject(ItemTag.class)) {
            ItemStack item = mechanism.valueAsType(ItemTag.class).getItemStack();
            MerchantRecipe oldRecipe = recipe.getRecipe();

            MerchantRecipe newRecipe = new MerchantRecipe(item, oldRecipe.getUses(), oldRecipe.getMaxUses(), oldRecipe.hasExperienceReward());
            newRecipe.setIngredients(oldRecipe.getIngredients());

            recipe.setRecipe(newRecipe);
        }
    }
}
