package net.aufdemrand.denizen.objects.properties.trade;

import net.aufdemrand.denizen.objects.dTrade;
import net.aufdemrand.denizencore.objects.Element;
import net.aufdemrand.denizencore.objects.Mechanism;
import net.aufdemrand.denizencore.objects.dObject;
import net.aufdemrand.denizencore.objects.properties.Property;
import net.aufdemrand.denizencore.tags.Attribute;

public class TradeHasXp implements Property {

    public static boolean describes(dObject recipe) {
        return recipe instanceof dTrade;
    }

    public static TradeHasXp getFrom(dObject recipe) {
        if (!describes(recipe)) {
            return null;
        }
        return new TradeHasXp((dTrade) recipe);
    }

    public static final String[] handledTags = new String[]{
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
        // @returns Element(Boolean)
        // @mechanism dTrade.has_xp
        // @description
        // Returns whether the trade has an experience reward.
        // -->
        if (attribute.startsWith("has_xp")) {
            return new Element(recipe.getRecipe().hasExperienceReward()).getAttribute(attribute.fulfill(1));
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
