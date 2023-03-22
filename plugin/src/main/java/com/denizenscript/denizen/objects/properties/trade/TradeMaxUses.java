package com.denizenscript.denizen.objects.properties.trade;

import com.denizenscript.denizen.objects.TradeTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.properties.PropertyParser;

public class TradeMaxUses extends TradeProperty {

    public static boolean describes(TradeTag recipe) {
        return true;
    }

    @Override
    public ElementTag getPropertyValue() {
        return new ElementTag(getRecipe().getMaxUses());
    }

    @Override
    public String getPropertyId() {
        return "max_uses";
    }

    public static void register() {

        // <--[tag]
        // @attribute <TradeTag.max_uses>
        // @returns ElementTag(Number)
        // @mechanism TradeTag.max_uses
        // @description
        // Returns the maximum amount of times that the trade can be used.
        // -->
        PropertyParser.registerTag(TradeMaxUses.class, ElementTag.class, "max_uses", (attribute, prop) -> {
            return new ElementTag(prop.getRecipe().getMaxUses());
        });

        // <--[mechanism]
        // @object TradeTag
        // @name max_uses
        // @input ElementTag(Number)
        // @description
        // Sets the maximum amount of times that the trade can be used.
        // @tags
        // <TradeTag.max_uses>
        // -->
        PropertyParser.registerMechanism(TradeMaxUses.class, ElementTag.class, "max_uses", (prop, mechanism, param) -> {
            if (mechanism.requireInteger()) {
                prop.getRecipe().setMaxUses(mechanism.getValue().asInt());
            }
        });
    }
}
