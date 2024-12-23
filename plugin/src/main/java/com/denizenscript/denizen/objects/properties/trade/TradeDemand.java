package com.denizenscript.denizen.objects.properties.trade;

import com.denizenscript.denizen.objects.TradeTag;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.core.ElementTag;

public class TradeDemand extends TradeProperty<ElementTag> {

    // <--[property]
    // @object TradeTag
    // @name demand
    // @input ElementTag(Number)
    // @description
    // Controls the demand level of the trade.
    // -->

    public static boolean describes(TradeTag recipe) {
        return true;
    }

    @Override
    public ElementTag getPropertyValue() {
        return new ElementTag(getRecipe().getDemand());
    }

    @Override
    public void setPropertyValue(ElementTag val, Mechanism mechanism) {
        if (mechanism.requireInteger()) {
            getRecipe().setDemand(mechanism.getValue().asInt());
        }
    }

    @Override
    public String getPropertyId() {
        return "demand";
    }

    public static void register() {
        autoRegister("demand", TradeDemand.class, ElementTag.class, false);
    }
}
