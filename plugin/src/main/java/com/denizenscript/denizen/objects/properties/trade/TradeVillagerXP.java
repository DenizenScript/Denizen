package com.denizenscript.denizen.objects.properties.trade;

import com.denizenscript.denizen.objects.TradeTag;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.core.ElementTag;

public class TradeVillagerXP extends TradeProperty<ElementTag> {

    // <--[property]
    // @object TradeTag
    // @name villager_xp
    // @input ElementTag(Number)
    // @description
    // Controls the amount of experience a villager gains from this trade.
    // -->

    public static boolean describes(TradeTag recipe) {
        return true;
    }

    @Override
    public ElementTag getPropertyValue() {
        return new ElementTag(getRecipe().getVillagerExperience());
    }

    @Override
    public void setPropertyValue(ElementTag val, Mechanism mechanism) {
        if (mechanism.requireInteger()) {
            getRecipe().setVillagerExperience(mechanism.getValue().asInt());
        }
    }

    @Override
    public String getPropertyId() {
        return "villager_xp";
    }

    public static void register() {
        autoRegister("villager_xp", TradeVillagerXP.class, ElementTag.class, false);
    }
}
