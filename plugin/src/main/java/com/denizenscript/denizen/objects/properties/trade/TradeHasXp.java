package com.denizenscript.denizen.objects.properties.trade;

import com.denizenscript.denizen.objects.TradeTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.properties.PropertyParser;

public class TradeHasXp extends TradeProperty {

    public static boolean describes(TradeTag recipe) {
        return true;
    }

    @Override
    public ElementTag getPropertyValue() {
        return new ElementTag(getRecipe().hasExperienceReward());
    }

    @Override
    public String getPropertyId() {
        return "has_xp";
    }

    public static void register() {

        // <--[tag]
        // @attribute <TradeTag.has_xp>
        // @returns ElementTag(Boolean)
        // @mechanism TradeTag.has_xp
        // @description
        // Returns whether the trade has an experience reward.
        // -->
        PropertyParser.registerTag(TradeHasXp.class, ElementTag.class, "has_xp", (attribute, prop) -> {
            return new ElementTag(prop.getRecipe().hasExperienceReward());
        });

        // <--[mechanism]
        // @object TradeTag
        // @name has_xp
        // @input ElementTag(Boolean)
        // @description
        // Sets whether this trade will reward XP upon successful trading.
        // @tags
        // <TradeTag.has_xp>
        // -->
        PropertyParser.registerMechanism(TradeHasXp.class, ElementTag.class, "has_xp", (prop, mechanism, param) -> {
            if (mechanism.requireBoolean()) {
                prop.getRecipe().setExperienceReward(mechanism.getValue().asBoolean());
            }
        });
    }
}
