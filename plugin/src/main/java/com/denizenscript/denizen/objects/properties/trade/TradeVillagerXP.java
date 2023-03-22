package com.denizenscript.denizen.objects.properties.trade;

import com.denizenscript.denizen.objects.TradeTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.properties.PropertyParser;

public class TradeVillagerXP extends TradeProperty {

    public static boolean describes(TradeTag recipe) {
        return true;
    }

    @Override
    public ElementTag getPropertyValue() {
        return new ElementTag(getRecipe().getVillagerExperience());
    }

    @Override
    public String getPropertyId() {
        return "villager_xp";
    }

    public static void register() {

        // <--[tag]
        // @attribute <TradeTag.villager_xp>
        // @returns ElementTag(Number)
        // @mechanism TradeTag.villager_xp
        // @description
        // Returns the amount of experience a villager gains from this trade.
        // -->
        PropertyParser.registerTag(TradeVillagerXP.class, ElementTag.class, "villager_xp", (attribute, prop) -> {
            return new ElementTag(prop.getRecipe().getVillagerExperience());
        });

        // <--[mechanism]
        // @object TradeTag
        // @name villager_xp
        // @input ElementTag(Number)
        // @description
        // Sets the amount of experience a villager gains from this trade.
        // @tags
        // <TradeTag.villager_xp>
        // -->
        PropertyParser.registerMechanism(TradeVillagerXP.class, ElementTag.class, "villager_xp", (prop, mechanism, param) -> {
            if (mechanism.requireInteger()) {
                prop.getRecipe().setVillagerExperience(mechanism.getValue().asInt());
            }
        });
    }
}
