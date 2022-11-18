package com.denizenscript.denizen.objects.properties.trade;

import com.denizenscript.denizen.objects.TradeTag;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.properties.Property;
import com.denizenscript.denizencore.objects.properties.PropertyParser;

public class TradePriceMultiplier implements Property {

    public static boolean describes(ObjectTag recipe) {
        return recipe instanceof TradeTag;
    }

    public static TradePriceMultiplier getFrom(ObjectTag recipe) {
        if (!describes(recipe)) {
            return null;
        }
        return new TradePriceMultiplier((TradeTag) recipe);
    }

    public static final String[] handledMechs = new String[] {
            "price_multiplier"
    };

    private TradeTag recipe;

    public TradePriceMultiplier(TradeTag recipe) {
        this.recipe = recipe;
    }

    public String getPropertyString() {
        if (recipe.getRecipe() == null) {
            return null;
        }
        return String.valueOf(recipe.getRecipe().getPriceMultiplier());
    }

    public String getPropertyId() {
        return "price_multiplier";
    }

    public static void register() {

        // <--[tag]
        // @attribute <TradeTag.price_multiplier>
        // @returns ElementTag(Decimal)
        // @mechanism TradeTag.price_multiplier
        // @description
        // Returns the price multiplier for this trade.
        // -->
        PropertyParser.registerTag(TradePriceMultiplier.class, ElementTag.class, "price_multiplier", (attribute, recipe) -> {
            return new ElementTag(recipe.recipe.getRecipe().getPriceMultiplier());
        });
    }

    public void adjust(Mechanism mechanism) {

        // <--[mechanism]
        // @object TradeTag
        // @name price_multiplier
        // @input ElementTag(Decimal)
        // @description
        // Sets the price multiplier for this trade.
        // @tags
        // <TradeTag.price_multiplier>
        // -->
        if (mechanism.matches("price_multiplier") && mechanism.requireFloat()) {
            recipe.getRecipe().setPriceMultiplier(mechanism.getValue().asFloat());
        }
    }
}
