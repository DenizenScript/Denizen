package com.denizenscript.denizen.objects.properties.trade;

import com.denizenscript.denizen.objects.TradeTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.properties.PropertyParser;

public class TradePriceMultiplier extends TradeProperty {

    public static boolean describes(TradeTag recipe) {
        return true;
    }

    @Override
    public ElementTag getPropertyValue() {
        return new ElementTag(getRecipe().getPriceMultiplier());
    }

    @Override
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
        PropertyParser.registerTag(TradePriceMultiplier.class, ElementTag.class, "price_multiplier", (attribute, prop) -> {
            return new ElementTag(prop.getRecipe().getPriceMultiplier());
        });

        // <--[mechanism]
        // @object TradeTag
        // @name price_multiplier
        // @input ElementTag(Decimal)
        // @description
        // Sets the price multiplier for this trade.
        // @tags
        // <TradeTag.price_multiplier>
        // -->
        PropertyParser.registerMechanism(TradePriceMultiplier.class, ElementTag.class, "price_multiplier", (prop, mechanism, param) -> {
            if (mechanism.requireFloat()) {
                prop.getRecipe().setPriceMultiplier(mechanism.getValue().asFloat());
            }
        });
    }
}
