package com.denizenscript.denizen.objects.properties.trade;

import com.denizenscript.denizen.objects.TradeTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.properties.PropertyParser;

public class TradeUses extends TradeProperty {

    public static boolean describes(TradeTag recipe) {
        return true;
    }

    @Override
    public ElementTag getPropertyValue() {
        return new ElementTag(getRecipe().getUses());
    }

    @Override
    public String getPropertyId() {
        return "uses";
    }

    public static void register() {

        // <--[tag]
        // @attribute <TradeTag.uses>
        // @returns ElementTag(Number)
        // @mechanism TradeTag.uses
        // @description
        // Returns how many times the trade has been used.
        // -->
        PropertyParser.registerTag(TradeUses.class, ElementTag.class, "uses", (attribute, prop) -> {
            return new ElementTag(prop.getRecipe().getUses());
        });

        // <--[mechanism]
        // @object TradeTag
        // @name uses
        // @input ElementTag(Number)
        // @description
        // Sets the amount of times the trade has been used.
        // @tags
        // <TradeTag.uses>
        // -->
        PropertyParser.registerMechanism(TradeUses.class, ElementTag.class, "uses", (prop, mechanism, param) -> {
            if (mechanism.requireInteger()) {
                prop.getRecipe().setUses(mechanism.getValue().asInt());
            }
        });
    }
}
