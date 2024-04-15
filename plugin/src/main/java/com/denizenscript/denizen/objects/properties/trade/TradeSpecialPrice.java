package com.denizenscript.denizen.objects.properties.trade;

import com.denizenscript.denizen.objects.TradeTag;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.core.ElementTag;

public class TradeSpecialPrice extends TradeProperty<ElementTag> {

    // <--[property]
    // @object TradeTag
    // @name special_price
    // @input ElementTag(Number)
    // @description
    // Controls the special price for this trade.
    // -->

    public static boolean describes(TradeTag recipe) {
        return true;
    }

    @Override
    public ElementTag getPropertyValue() {
        return new ElementTag(getRecipe().getSpecialPrice());
    }

    @Override
    public void setPropertyValue(ElementTag val, Mechanism mechanism) {
        if (mechanism.requireInteger()) {
            getRecipe().setSpecialPrice(mechanism.getValue().asInt());
        }
    }

    @Override
    public String getPropertyId() {
        return "special_price";
    }

    public static void register() {
        autoRegister("special_price", TradeSpecialPrice.class, ElementTag.class, false);
    }
}
