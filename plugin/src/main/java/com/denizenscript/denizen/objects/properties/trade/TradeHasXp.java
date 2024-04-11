package com.denizenscript.denizen.objects.properties.trade;

import com.denizenscript.denizen.objects.TradeTag;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.core.ElementTag;

public class TradeHasXp extends TradeProperty<ElementTag> {

    // <--[property]
    // @object TradeTag
    // @name has_xp
    // @input ElementTag(Boolean)
    // @description
    // Controls whether this trade will reward XP upon successful trading.
    // -->

    public static boolean describes(TradeTag recipe) {
        return true;
    }

    @Override
    public ElementTag getPropertyValue() {
        return new ElementTag(getRecipe().hasExperienceReward());
    }

    @Override
    public void setPropertyValue(ElementTag val, Mechanism mechanism) {
        if (mechanism.requireBoolean()) {
            getRecipe().setExperienceReward(mechanism.getValue().asBoolean());
        }
    }

    @Override
    public String getPropertyId() {
        return "has_xp";
    }

    public static void register() {
        autoRegister("has_xp", TradeHasXp.class, ElementTag.class, false);
    }
}
