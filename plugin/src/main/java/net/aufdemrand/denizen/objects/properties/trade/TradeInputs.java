package net.aufdemrand.denizen.objects.properties.trade;

import net.aufdemrand.denizen.objects.dItem;
import net.aufdemrand.denizen.objects.dTrade;
import net.aufdemrand.denizen.utilities.debugging.dB;
import net.aufdemrand.denizencore.objects.Mechanism;
import net.aufdemrand.denizencore.objects.dList;
import net.aufdemrand.denizencore.objects.dObject;
import net.aufdemrand.denizencore.objects.properties.Property;
import net.aufdemrand.denizencore.tags.Attribute;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class TradeInputs implements Property {

    public static boolean describes(dObject recipe) {
        return recipe instanceof dTrade;
    }

    public static TradeInputs getFrom(dObject recipe) {
        if (!describes(recipe)) {
            return null;
        }
        return new TradeInputs((dTrade) recipe);
    }

    public static final String[] handledTags = new String[] {
            "inputs"
    };

    public static final String[] handledMechs = new String[] {
            "inputs"
    };

    private dTrade recipe;

    public TradeInputs(dTrade recipe) {
        this.recipe = recipe;
    }

    public String getPropertyString() {
        if (recipe.getRecipe() == null) {
            return null;
        }
        dList ingredients = new dList();
        for (ItemStack item : recipe.getRecipe().getIngredients()) {
            ingredients.addObject(new dItem(item));
        }
        return ingredients.identify();
    }

    public String getPropertyId() {
        return "inputs";
    }

    public String getAttribute(Attribute attribute) {
        if (attribute == null) {
            return null;
        }

        // <--[tag]
        // @attribute <trade@trade.inputs>
        // @returns dList(dItem)
        // @mechanism dTrade.inputs
        // @description
        // Returns the list of items required to make the trade.
        // -->
        if (attribute.startsWith("inputs")) {
            ArrayList<dItem> itemList = new ArrayList<>();
            for (ItemStack item : recipe.getRecipe().getIngredients()) {
                itemList.add(new dItem(item));
            }
            return new dList(itemList).getAttribute(attribute.fulfill(1));
        }

        return null;
    }

    public void adjust(Mechanism mechanism) {

        // <--[mechanism]
        // @object dTrade
        // @name inputs
        // @input dList(dItem)
        // @description
        // Sets the items required to make a successful trade. Use an empty input to make the trade impossible.
        // NOTE: If a more than two items are specified, then only the first two items will be used.
        // @tags
        // <trade@trade.input>
        // -->
        if (mechanism.matches("inputs")) {
            List<ItemStack> ingredients = new ArrayList<>();
            List<dItem> list = mechanism.getValue().asType(dList.class).filter(dItem.class);

            if (!mechanism.hasValue() || list.isEmpty()) {
                recipe.getRecipe().setIngredients(ingredients);
                return;
            }

            for (dItem item : list) {
                ingredients.add(item.getItemStack());
            }

            if (ingredients.size() > 2) {
                dB.echoError("Trade recipe input was given " + list.size() + " items. Only using the first two items!");
                ingredients = ingredients.subList(0, 2);
            }

            recipe.getRecipe().setIngredients(ingredients);
        }
    }
}
