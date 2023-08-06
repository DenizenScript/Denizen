package com.denizenscript.denizen.objects.properties.trade;

import com.denizenscript.denizen.objects.ItemTag;
import com.denizenscript.denizen.objects.TradeTag;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.core.ListTag;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class TradeInputs extends TradeProperty<ListTag> {

    // <--[property]
    // @object TradeTag
    // @name inputs
    // @input ListTag(ItemTag)
    // @description
    // Controls the items required to make a successful trade. Use an empty input to make the trade impossible.
    // NOTE: If more than two items are specified, then only the first two items will be used.
    // -->

    public static boolean describes(TradeTag recipe) {
        return true;
    }

    @Override
    public ListTag getPropertyValue() {
        return getIngredientsList();
    }

    @Override
    public void setPropertyValue(ListTag inList, Mechanism mechanism) {
        List<ItemStack> ingredients = new ArrayList<>();
        List<ItemTag> list = inList.filter(ItemTag.class, mechanism.context);
        if (!mechanism.hasValue() || list.isEmpty()) {
            getRecipe().setIngredients(ingredients);
            return;
        }
        for (ItemTag item : list) {
            ingredients.add(item.getItemStack());
        }
        if (ingredients.size() > 2) {
            mechanism.echoError("Trade recipe input was given " + list.size() + " items. Only using the first two items!");
            ingredients = ingredients.subList(0, 2);
        }
        getRecipe().setIngredients(ingredients);
    }

    @Override
    public String getPropertyId() {
        return "inputs";
    }

    public ListTag getIngredientsList() {
        ListTag result = new ListTag();
        for (ItemStack item : getRecipe().getIngredients()) {
            result.addObject(new ItemTag(item));
        }
        return result;
    }

    public static void register() {
        autoRegister("inputs", TradeInputs.class, ListTag.class, false);
    }
}
