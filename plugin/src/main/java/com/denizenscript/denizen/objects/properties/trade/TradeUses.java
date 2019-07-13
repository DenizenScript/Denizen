package com.denizenscript.denizen.objects.properties.trade;

import com.denizenscript.denizen.objects.TradeTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.properties.Property;
import com.denizenscript.denizencore.tags.Attribute;

public class TradeUses implements Property {

    public static boolean describes(ObjectTag recipe) {
        return recipe instanceof TradeTag;
    }

    public static TradeUses getFrom(ObjectTag recipe) {
        if (!describes(recipe)) {
            return null;
        }
        return new TradeUses((TradeTag) recipe);
    }

    public static final String[] handledTags = new String[] {
            "uses"
    };

    public static final String[] handledMechs = new String[] {
            "uses"
    };

    private TradeTag recipe;

    public TradeUses(TradeTag recipe) {
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
        // @attribute <TradeTag.uses>
        // @returns ElementTag(Number)
        // @mechanism TradeTag.uses
        // @description
        // Returns how many times the trade has been used.
        // -->
        if (attribute.startsWith("uses")) {
            return new ElementTag(recipe.getRecipe().getUses()).getAttribute(attribute.fulfill(1));
        }

        return null;
    }

    public void adjust(Mechanism mechanism) {

        // <--[mechanism]
        // @object TradeTag
        // @name uses
        // @input Element(Number)
        // @description
        // Sets the amount of times the trade has been used.
        // @tags
        // <TradeTag.uses>
        // -->
        if (mechanism.matches("uses") && mechanism.requireInteger()) {
            recipe.getRecipe().setUses(mechanism.getValue().asInt());
        }
    }
}
