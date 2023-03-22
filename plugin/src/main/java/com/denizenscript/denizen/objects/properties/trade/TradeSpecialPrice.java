package com.denizenscript.denizen.objects.properties.trade;

import com.denizenscript.denizen.objects.TradeTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.properties.PropertyParser;

public class TradeSpecialPrice extends TradeProperty {

    public static boolean describes(TradeTag recipe) {
        return true;
    }

    @Override
    public ElementTag getPropertyValue() {
        return new ElementTag(getRecipe().getSpecialPrice());
    }

    @Override
    public String getPropertyId() {
        return "special_price";
    }

    public static void register() {

        // <--[tag]
        // @attribute <TradeTag.special_price>
        // @returns ElementTag(Number)
        // @mechanism TradeTag.special_price
        // @description
        // Returns the special price for this trade.
        // -->
        PropertyParser.registerTag(TradeSpecialPrice.class, ElementTag.class, "special_price", (attribute, prop) -> {
            return new ElementTag(prop.getRecipe().getSpecialPrice());
        });

        // <--[mechanism]
        // @object TradeTag
        // @name special_price
        // @input ElementTag(Number)
        // @description
        // Sets the special price for this trade.
        // @tags
        // <TradeTag.special_price>
        // -->
        PropertyParser.registerMechanism(TradeSpecialPrice.class, ElementTag.class, "special_price", (prop, mechanism, param) -> {
            if (mechanism.requireInteger()) {
                prop.getRecipe().setSpecialPrice(mechanism.getValue().asInt());
            }
        });
    }
}
