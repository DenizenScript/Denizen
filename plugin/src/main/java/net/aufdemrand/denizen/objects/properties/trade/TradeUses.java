package net.aufdemrand.denizen.objects.properties.trade;

import net.aufdemrand.denizen.objects.dTrade;
import net.aufdemrand.denizencore.objects.Element;
import net.aufdemrand.denizencore.objects.Mechanism;
import net.aufdemrand.denizencore.objects.dObject;
import net.aufdemrand.denizencore.objects.properties.Property;
import net.aufdemrand.denizencore.tags.Attribute;

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
