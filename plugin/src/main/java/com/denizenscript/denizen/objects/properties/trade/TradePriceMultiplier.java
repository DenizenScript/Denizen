package com.denizenscript.denizen.objects.properties.trade;

import com.denizenscript.denizen.objects.TradeTag;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.core.ElementTag;

public class TradePriceMultiplier extends TradeProperty<ElementTag> {

    // <--[property]
    // @object TradeTag
    // @name price_multiplier
    // @input ElementTag(Decimal)
    // @description
    // Controls the price multiplier for this trade.
    // -->

    public static boolean describes(TradeTag recipe) {
        return true;
    }

    @Override
    public ElementTag getPropertyValue() {
        return new ElementTag(getRecipe().getPriceMultiplier());
    }

    @Override
    public void setPropertyValue(ElementTag val, Mechanism mechanism) {
        if (mechanism.requireFloat()) {
            getRecipe().setPriceMultiplier(mechanism.getValue().asFloat());
        }
    }

    @Override
    public String getPropertyId() {
        return "price_multiplier";
    }

    public static void register() {
        autoRegister("price_multiplier", TradePriceMultiplier.class, ElementTag.class, false);
    }
}
