package com.denizenscript.denizen.objects.properties.trade;

import com.denizenscript.denizen.objects.dTrade;
import com.denizenscript.denizencore.objects.Element;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.dObject;
import com.denizenscript.denizencore.objects.properties.Property;
import com.denizenscript.denizencore.tags.Attribute;

public class TradeUses implements Property {

    public static boolean describes(dObject recipe) {
        return recipe instanceof dTrade;
    }

    public static TradeUses getFrom(dObject recipe) {
        if (!describes(recipe)) {
            return null;
        }
        return new TradeUses((dTrade) recipe);
    }

    public static final String[] handledTags = new String[] {
            "uses"
    };

    public static final String[] handledMechs = new String[] {
            "uses"
    };

    private dTrade recipe;

    public TradeUses(dTrade recipe) {
        this.recipe = recipe;
    }

    public String getPropertyString() {
        if (recipe.getRecipe() == null) {
            return null;
        }
        return String.valueOf(recipe.getRecipe().getUses());
    }

    public String getPropertyId() {
        return "uses";
    }

    public String getAttribute(Attribute attribute) {
        if (attribute == null) {
            return null;
        }

        // <--[tag]
        // @attribute <trade@trade.uses>
        // @returns Element(Number)
        // @mechanism dTrade.uses
        // @description
        // Returns how many times the trade has been used.
        // -->
        if (attribute.startsWith("uses")) {
            return new Element(recipe.getRecipe().getUses()).getAttribute(attribute.fulfill(1));
        }

        return null;
    }

    public void adjust(Mechanism mechanism) {

        // <--[mechanism]
        // @object dTrade
        // @name uses
        // @input Element(Number)
        // @description
        // Sets the amount of times the trade has been used.
        // @tags
        // <trade@trade.uses>
        // -->
        if (mechanism.matches("uses") && mechanism.requireInteger()) {
            recipe.getRecipe().setUses(mechanism.getValue().asInt());
        }
    }
}
