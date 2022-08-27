package com.denizenscript.denizen.objects.properties.trade;

import com.denizenscript.denizen.objects.TradeTag;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.properties.Property;
import com.denizenscript.denizencore.objects.properties.PropertyParser;

public class TradeVillagerXP implements Property {

    public static boolean describes(ObjectTag recipe) {
        return recipe instanceof TradeTag;
    }

    public static TradeVillagerXP getFrom(ObjectTag recipe) {
        if (!describes(recipe)) {
            return null;
        }
        return new TradeVillagerXP((TradeTag) recipe);
    }

    public static final String[] handledMechs = new String[] {
            "villager_xp"
    };

    private TradeTag recipe;

    public TradeVillagerXP(TradeTag recipe) {
        this.recipe = recipe;
    }

    public String getPropertyString() {
        if (recipe.getRecipe() == null) {
            return null;
        }
        return String.valueOf(recipe.getRecipe().getVillagerExperience());
    }

    public String getPropertyId() {
        return "villager_xp";
    }

    public static void registerTags() {

        // <--[tag]
        // @attribute <TradeTag.villager_xp>
        // @returns ElementTag(Number)
        // @mechanism TradeTag.villager_xp
        // @description
        // Returns the amount of experience a villager gains from this trade.
        // -->
        PropertyParser.registerTag(TradeVillagerXP.class, ElementTag.class, "villager_xp", (attribute, recipe) -> {
            return new ElementTag(recipe.recipe.getRecipe().getVillagerExperience());
        });
    }

    public void adjust(Mechanism mechanism) {

        // <--[mechanism]
        // @object TradeTag
        // @name villager_xp
        // @input ElementTag(Number)
        // @description
        // Sets the amount of experience a villager gains from this trade.
        // @tags
        // <TradeTag.villager_xp>
        // -->
        if (mechanism.matches("villager_xp") && mechanism.requireInteger()) {
            recipe.getRecipe().setVillagerExperience(mechanism.getValue().asInt());
        }
    }
}
