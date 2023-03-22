package com.denizenscript.denizen.objects.properties.trade;

import com.denizenscript.denizen.objects.TradeTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.properties.Property;
import com.denizenscript.denizencore.objects.properties.PropertyParser;

public class TradeMaxUses implements Property {

    public static boolean describes(ObjectTag recipe) {
        return recipe instanceof TradeTag;
    }

    public static TradeMaxUses getFrom(ObjectTag recipe) {
        if (!describes(recipe)) {
            return null;
        }
        return new TradeMaxUses((TradeTag) recipe);
    }

    public static final String[] handledMechs = new String[] {
            "max_uses"
    };

    public TradeTag recipe;

    public TradeMaxUses(TradeTag recipe) {
        this.recipe = recipe;
    }

    public String getPropertyString() {
        if (recipe.getRecipe() == null) {
            return null;
        }
        return String.valueOf(recipe.getRecipe().getMaxUses());
    }

    public String getPropertyId() {
        return "max_uses";
    }

    public static void register() {

        // <--[tag]
        // @attribute <TradeTag.max_uses>
        // @returns ElementTag(Number)
        // @mechanism TradeTag.max_uses
        // @description
        // Returns the maximum amount of times that the trade can be used.
        // -->
        PropertyParser.registerTag(TradeMaxUses.class, ElementTag.class, "max_uses", (attribute, recipe) -> {
            return new ElementTag(recipe.recipe.getRecipe().getMaxUses());
        });
    }

    public void adjust(Mechanism mechanism) {

        // <--[mechanism]
        // @object TradeTag
        // @name max_uses
        // @input ElementTag(Number)
        // @description
        // Sets the maximum amount of times that the trade can be used.
        // @tags
        // <TradeTag.max_uses>
        // -->
        if (mechanism.matches("max_uses") && mechanism.requireInteger()) {
            recipe.getRecipe().setMaxUses(mechanism.getValue().asInt());
        }
    }
}
