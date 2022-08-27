package com.denizenscript.denizen.objects.properties.trade;

import com.denizenscript.denizen.objects.TradeTag;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.properties.Property;
import com.denizenscript.denizencore.objects.properties.PropertyParser;

public class TradeDemand implements Property {

    public static boolean describes(ObjectTag recipe) {
        return recipe instanceof TradeTag;
    }

    public static TradeDemand getFrom(ObjectTag recipe) {
        if (!describes(recipe)) {
            return null;
        }
        return new TradeDemand((TradeTag) recipe);
    }

    public static final String[] handledMechs = new String[] {
            "demand"
    };

    private TradeTag recipe;

    public TradeDemand(TradeTag recipe) {
        this.recipe = recipe;
    }

    public String getPropertyString() {
        if (recipe.getRecipe() == null) {
            return null;
        }
        return String.valueOf(recipe.getRecipe().getDemand());
    }

    public String getPropertyId() {
        return "demand";
    }

    public static void registerTags() {

        // <--[tag]
        // @attribute <TradeTag.demand>
        // @returns ElementTag(Number)
        // @mechanism TradeTag.demand
        // @description
        // Returns the demand level of the trade.
        // -->
        PropertyParser.registerTag(TradeDemand.class, ElementTag.class, "demand", (attribute, recipe) -> {
            return new ElementTag(recipe.recipe.getRecipe().getDemand());
        });
    }

    public void adjust(Mechanism mechanism) {

        // <--[mechanism]
        // @object TradeTag
        // @name demand
        // @input ElementTag(Number)
        // @description
        // Sets the demand level of the trade.
        // @tags
        // <TradeTag.demand>
        // -->
        if (mechanism.matches("demand") && mechanism.requireInteger()) {
            recipe.getRecipe().setDemand(mechanism.getValue().asInt());
        }
    }
}
