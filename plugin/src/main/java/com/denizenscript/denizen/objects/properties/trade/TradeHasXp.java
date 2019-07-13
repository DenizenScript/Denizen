package com.denizenscript.denizen.objects.properties.trade;

import com.denizenscript.denizen.objects.dTrade;
import com.denizenscript.denizencore.objects.ElementTag;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.properties.Property;
import com.denizenscript.denizencore.tags.Attribute;

public class TradeHasXp implements Property {

    public static boolean describes(ObjectTag recipe) {
        return recipe instanceof dTrade;
    }

    public static TradeHasXp getFrom(ObjectTag recipe) {
        if (!describes(recipe)) {
            return null;
        }
        return new TradeHasXp((dTrade) recipe);
    }

    public static final String[] handledTags = new String[] {
            "has_xp"
    };

    public static final String[] handledMechs = new String[] {
            "has_xp"
    };

    private dTrade recipe;

    public TradeHasXp(dTrade recipe) {
        this.recipe = recipe;
    }

    public String getPropertyString() {
        if (recipe.getRecipe() == null) {
            return null;
        }
        return String.valueOf(recipe.getRecipe().hasExperienceReward());
    }

    public String getPropertyId() {
        return "has_xp";
    }

    public String getAttribute(Attribute attribute) {
        if (attribute == null) {
            return null;
        }

        // <--[tag]
        // @attribute <trade@trade.has_xp>
        // @returns ElementTag(Boolean)
        // @mechanism dTrade.has_xp
        // @description
        // Returns whether the trade has an experience reward.
        // -->
        if (attribute.startsWith("has_xp")) {
            return new ElementTag(recipe.getRecipe().hasExperienceReward()).getAttribute(attribute.fulfill(1));
        }

        return null;
    }

    public void adjust(Mechanism mechanism) {

        // <--[mechanism]
        // @object dTrade
        // @name has_xp
        // @input Element(Boolean)
        // @description
        // Sets whether this trade will reward XP upon successful trading.
        // @tags
        // <trade@trade.has_xp>
        // -->
        if (mechanism.matches("has_xp") && mechanism.requireBoolean()) {
            recipe.getRecipe().setExperienceReward(mechanism.getValue().asBoolean());
        }
    }
}
