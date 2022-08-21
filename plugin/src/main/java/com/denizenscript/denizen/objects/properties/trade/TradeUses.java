package com.denizenscript.denizen.objects.properties.trade;

import com.denizenscript.denizen.objects.TradeTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.properties.Property;
import com.denizenscript.denizencore.objects.properties.PropertyParser;

public class TradeUses implements Property {

    public static boolean describes(ObjectTag recipe) {
        return recipe instanceof TradeTag;
    }

    public static TradeUses getFrom(ObjectTag recipe) {
        if (!describes(recipe)) {
            return null;
        }
        return new TradeUses((TradeTag) recipe);
    }

    public static final String[] handledMechs = new String[] {
            "uses"
    };

    private TradeTag recipe;

    public TradeUses(TradeTag recipe) {
        this.recipe = recipe;
    }

    public String getPropertyString() {
        if (recipe.getRecipe() == null) {
            return null;
        }
        return String.valueOf(recipe.getRecipe().getUses());
    }

    public String getPropertyId() {
        return "uses";
    }

    public static void registerTags() {

        // <--[tag]
        // @attribute <TradeTag.uses>
        // @returns ElementTag(Number)
        // @mechanism TradeTag.uses
        // @description
        // Returns how many times the trade has been used.
        // -->
        PropertyParser.registerTag(TradeUses.class, ElementTag.class, "uses", (attribute, recipe) -> {
            return new ElementTag(recipe.recipe.getRecipe().getUses());
        });
    }

    public void adjust(Mechanism mechanism) {

        // <--[mechanism]
        // @object TradeTag
        // @name uses
        // @input ElementTag(Number)
        // @description
        // Sets the amount of times the trade has been used.
        // @tags
        // <TradeTag.uses>
        // -->
        if (mechanism.matches("uses") && mechanism.requireInteger()) {
            recipe.getRecipe().setUses(mechanism.getValue().asInt());
        }
    }
}
