package net.aufdemrand.denizen.objects.properties.trade;

import net.aufdemrand.denizen.objects.dTrade;
import net.aufdemrand.denizencore.objects.Element;
import net.aufdemrand.denizencore.objects.Mechanism;
import net.aufdemrand.denizencore.objects.dObject;
import net.aufdemrand.denizencore.objects.properties.Property;
import net.aufdemrand.denizencore.tags.Attribute;

public class TradeMaxUses implements Property {

    public static boolean describes(dObject recipe) {
        return recipe instanceof dTrade;
    }

    public static TradeMaxUses getFrom(dObject recipe) {
        if (!describes(recipe)) {
            return null;
        }
        return new TradeMaxUses((dTrade) recipe);
    }

    public static final String[] handledTags = new String[]{
            "max_uses"
    };

    public static final String[] handledMechs = new String[] {
            "max_uses"
    };

    private dTrade recipe;

    public TradeMaxUses(dTrade recipe) {
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

    public String getAttribute(Attribute attribute) {
        if (attribute == null) {
            return null;
        }

        // <--[tag]
        // @attribute <trade@trade.max_uses>
        // @returns Element(Number)
        // @mechanism dTrade.max_uses
        // @description
        // Returns the maximum amount of times that the trade can be used.
        // -->
        if (attribute.startsWith("max_uses")) {
            return new Element(recipe.getRecipe().getMaxUses()).getAttribute(attribute.fulfill(1));
        }

        return null;
    }

    public void adjust(Mechanism mechanism) {

        // <--[mechanism]
        // @object dTrade
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
