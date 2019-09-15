package com.denizenscript.denizen.objects.properties.trade;

import com.denizenscript.denizen.utilities.debugging.Debug;
import com.denizenscript.denizen.objects.ItemTag;
import com.denizenscript.denizen.objects.TradeTag;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.core.ListTag;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.properties.Property;
import com.denizenscript.denizencore.tags.Attribute;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class TradeInputs implements Property {

    public static boolean describes(ObjectTag recipe) {
        return recipe instanceof TradeTag;
    }

    public static TradeInputs getFrom(ObjectTag recipe) {
        if (!describes(recipe)) {
            return null;
        }
        return new TradeInputs((TradeTag) recipe);
    }

    public static final String[] handledTags = new String[] {
            "inputs"
    };

    public static final String[] handledMechs = new String[] {
            "inputs"
    };

    private TradeTag recipe;

    public TradeInputs(TradeTag recipe) {
        this.recipe = recipe;
    }

    public String getPropertyString() {
        if (recipe.getRecipe() == null) {
            return null;
        }
        ListTag ingredients = new ListTag();
        for (ItemStack item : recipe.getRecipe().getIngredients()) {
            ingredients.addObject(new ItemTag(item));
        }
        return ingredients.identify();
    }

    public String getPropertyId() {
        return "inputs";
    }

    public ObjectTag getObjectAttribute(Attribute attribute) {
        if (attribute == null) {
            return null;
        }

        // <--[tag]
        // @attribute <TradeTag.inputs>
        // @returns ListTag(ItemTag)
        // @mechanism TradeTag.inputs
        // @description
        // Returns the list of items required to make the trade.
        // -->
        if (attribute.startsWith("inputs")) {
            ArrayList<ItemTag> itemList = new ArrayList<>();
            for (ItemStack item : recipe.getRecipe().getIngredients()) {
                itemList.add(new ItemTag(item));
            }
            return new ListTag(itemList).getObjectAttribute(attribute.fulfill(1));
        }

        return null;
    }

    public void adjust(Mechanism mechanism) {

        // <--[mechanism]
        // @object TradeTag
        // @name inputs
        // @input ListTag(ItemTag)
        // @description
        // Sets the items required to make a successful trade. Use an empty input to make the trade impossible.
        // NOTE: If a more than two items are specified, then only the first two items will be used.
        // @tags
        // <TradeTag.input>
        // -->
        if (mechanism.matches("inputs")) {
            List<ItemStack> ingredients = new ArrayList<>();
            List<ItemTag> list = mechanism.valueAsType(ListTag.class).filter(ItemTag.class, mechanism.context);

            if (!mechanism.hasValue() || list.isEmpty()) {
                recipe.getRecipe().setIngredients(ingredients);
                return;
            }

            for (ItemTag item : list) {
                ingredients.add(item.getItemStack());
            }

            if (ingredients.size() > 2) {
                Debug.echoError("Trade recipe input was given " + list.size() + " items. Only using the first two items!");
                ingredients = ingredients.subList(0, 2);
            }

            recipe.getRecipe().setIngredients(ingredients);
        }
    }
}
