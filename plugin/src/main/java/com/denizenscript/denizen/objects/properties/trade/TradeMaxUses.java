package com.denizenscript.denizen.objects.properties.trade;

import com.denizenscript.denizen.objects.TradeTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.properties.Property;
import com.denizenscript.denizencore.tags.Attribute;

public class TradeMaxUses implements Property {

    public static boolean describes(ObjectTag recipe) {
        return recipe instanceof TradeTag;
    }

    public static TradeMaxUses getFrom(ObjectTag recipe) {
        if (!describes(recipe)) {
            return null;
        }
        return new TradeMaxUses((TradeTag) recipe);
    }

    public static final String[] handledTags = new String[] {
            "max_uses"
    };

    public static final String[] handledMechs = new String[] {
            "max_uses"
    };

    private TradeTag recipe;

    public TradeMaxUses(TradeTag recipe) {
        this.recipe = recipe;
    }

    public String getPropertyString() {
        if (recipe.getRecipe() == null) {
            return null;
        }
        return String.valueOf(recipe.getRecipe().getMaxUses());
    }

    public String getPropertyId() {
        return "max_uses";
    }

    public ObjectTag getObjectAttribute(Attribute attribute) {
        if (attribute == null) {
            return null;
        }

        // <--[tag]
        // @attribute <TradeTag.max_uses>
        // @returns ElementTag(Number)
        // @mechanism TradeTag.max_uses
        // @description
        // Returns the maximum amount of times that the trade can be used.
        // -->
        if (attribute.startsWith("max_uses")) {
            return new ElementTag(recipe.getRecipe().getMaxUses()).getObjectAttribute(attribute.fulfill(1));
        }

        return null;
    }

    public void adjust(Mechanism mechanism) {

        // <--[mechanism]
        // @object TradeTag
        // @name max_uses
        // @input Element(Number)
        // @description
        // Sets the maximum amount of times that the trade can be used.
        // @tags
        // //
        // -->
        if (mechanism.matches("max_uses") && mechanism.requireInteger()) {
            recipe.getRecipe().setMaxUses(mechanism.getValue().asInt());
        }
    }
}
