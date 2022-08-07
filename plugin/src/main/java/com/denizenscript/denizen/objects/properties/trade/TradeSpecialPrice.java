package com.denizenscript.denizen.objects.properties.trade;

import com.denizenscript.denizen.objects.TradeTag;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.properties.Property;
import com.denizenscript.denizencore.objects.properties.PropertyParser;

public class TradeSpecialPrice implements Property {

    public static boolean describes(ObjectTag recipe) {
        return recipe instanceof TradeTag;
    }

    public static TradeSpecialPrice getFrom(ObjectTag recipe) {
        if (!describes(recipe)) {
            return null;
        }
        return new TradeSpecialPrice((TradeTag) recipe);
    }

    public static final String[] handledMechs = new String[] {
            "special_price"
    };

    private TradeTag recipe;

    public TradeSpecialPrice(TradeTag recipe) {
        this.recipe = recipe;
    }

    public String getPropertyString() {
        if (recipe.getRecipe() == null) {
            return null;
        }
        return String.valueOf(recipe.getRecipe().getSpecialPrice());
    }

    public String getPropertyId() {
        return "special_price";
    }

    public static void registerTags() {

        // <--[tag]
        // @attribute <TradeTag.special_price>
        // @returns ElementTag(Number)
        // @mechanism TradeTag.special_price
        // @description
        // Returns the special price for this trade.
        // -->
        PropertyParser.<TradeSpecialPrice, ElementTag>registerTag(ElementTag.class, "special_price", (attribute, recipe) -> {
            return new ElementTag(recipe.recipe.getRecipe().getSpecialPrice());
        });
    }

    public void adjust(Mechanism mechanism) {

        // <--[mechanism]
        // @object TradeTag
        // @name special_price
        // @input ElementTag(Number)
        // @description
        // Sets the special price for this trade.
        // @tags
        // <TradeTag.special_price>
        // -->
        if (mechanism.matches("special_price") && mechanism.requireInteger()) {
            recipe.getRecipe().setSpecialPrice(mechanism.getValue().asInt());
        }
    }
}
