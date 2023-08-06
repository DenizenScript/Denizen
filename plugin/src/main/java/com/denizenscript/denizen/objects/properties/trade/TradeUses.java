package com.denizenscript.denizen.objects.properties.trade;

import com.denizenscript.denizen.objects.TradeTag;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.core.ElementTag;

public class TradeUses extends TradeProperty<ElementTag> {

    // <--[property]
    // @object TradeTag
    // @name uses
    // @input ElementTag(Number)
    // @description
    // Controls the amount of times the trade has been used.
    // -->

    public static boolean describes(TradeTag recipe) {
        return true;
    }

    @Override
    public ElementTag getPropertyValue() {
        return new ElementTag(getRecipe().getUses());
    }

    @Override
    public void setPropertyValue(ElementTag val, Mechanism mechanism) {
        if (mechanism.requireInteger()) {
            getRecipe().setUses(mechanism.getValue().asInt());
        }
    }

    @Override
    public String getPropertyId() {
        return "uses";
    }

    public static void register() {
        autoRegister("uses", TradeUses.class, ElementTag.class, false);
    }
}
