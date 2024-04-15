package com.denizenscript.denizen.objects.properties.trade;

import com.denizenscript.denizen.objects.TradeTag;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.core.ElementTag;

public class TradeMaxUses extends TradeProperty<ElementTag> {

    // <--[property]
    // @object TradeTag
    // @name max_uses
    // @input ElementTag(Number)
    // @description
    // Controls the maximum amount of times that the trade can be used.
    // -->

    public static boolean describes(TradeTag recipe) {
        return true;
    }

    @Override
    public ElementTag getPropertyValue() {
        return new ElementTag(getRecipe().getMaxUses());
    }

    @Override
    public void setPropertyValue(ElementTag val, Mechanism mechanism) {
        if (mechanism.requireInteger()) {
            getRecipe().setMaxUses(mechanism.getValue().asInt());
        }
    }

    @Override
    public String getPropertyId() {
        return "max_uses";
    }

    public static void register() {
        autoRegister("max_uses", TradeMaxUses.class, ElementTag.class, false);
    }
}
