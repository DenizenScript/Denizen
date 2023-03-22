package com.denizenscript.denizen.objects.properties.trade;

import com.denizenscript.denizen.objects.TradeTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.properties.PropertyParser;

public class TradeDemand extends TradeProperty {

    public static boolean describes(TradeTag recipe) {
        return true;
    }

    @Override
    public ElementTag getPropertyValue() {
        return new ElementTag(getRecipe().getDemand());
    }

    public String getPropertyId() {
        return "demand";
    }

    public static void register() {

        // <--[tag]
        // @attribute <TradeTag.demand>
        // @returns ElementTag(Number)
        // @mechanism TradeTag.demand
        // @description
        // Returns the demand level of the trade.
        // -->
        PropertyParser.registerTag(TradeDemand.class, ElementTag.class, "demand", (attribute, prop) -> {
            return new ElementTag(prop.getRecipe().getDemand());
        });

        // <--[mechanism]
        // @object TradeTag
        // @name demand
        // @input ElementTag(Number)
        // @description
        // Sets the demand level of the trade.
        // @tags
        // <TradeTag.demand>
        // -->
        PropertyParser.registerMechanism(TradeDemand.class, ElementTag.class, "demand", (prop, mechanism, param) -> {
            if (mechanism.requireInteger()) {
                prop.getRecipe().setDemand(mechanism.getValue().asInt());
            }
        });
    }
}
