package com.denizenscript.denizen.objects.properties.trade;

import com.denizenscript.denizen.objects.TradeTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.properties.Property;
import com.denizenscript.denizencore.objects.properties.PropertyParser;

public class TradeHasXp implements Property {

    public static boolean describes(ObjectTag recipe) {
        return recipe instanceof TradeTag;
    }

    public static TradeHasXp getFrom(ObjectTag recipe) {
        if (!describes(recipe)) {
            return null;
        }
        return new TradeHasXp((TradeTag) recipe);
    }

    public static final String[] handledMechs = new String[] {
            "has_xp"
    };

    public TradeTag recipe;

    public TradeHasXp(TradeTag recipe) {
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

    public static void register() {

        // <--[tag]
        // @attribute <TradeTag.has_xp>
        // @returns ElementTag(Boolean)
        // @mechanism TradeTag.has_xp
        // @description
        // Returns whether the trade has an experience reward.
        // -->
        PropertyParser.registerTag(TradeHasXp.class, ElementTag.class, "has_xp", (attribute, recipe) -> {
            return new ElementTag(recipe.recipe.getRecipe().hasExperienceReward());
        });
    }

    public void adjust(Mechanism mechanism) {

        // <--[mechanism]
        // @object TradeTag
        // @name has_xp
        // @input ElementTag(Boolean)
        // @description
        // Sets whether this trade will reward XP upon successful trading.
        // @tags
        // <TradeTag.has_xp>
        // -->
        if (mechanism.matches("has_xp") && mechanism.requireBoolean()) {
            recipe.getRecipe().setExperienceReward(mechanism.getValue().asBoolean());
        }
    }
}
