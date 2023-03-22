package com.denizenscript.denizen.objects.properties.inventory;

import com.denizenscript.denizen.objects.InventoryTag;
import com.denizenscript.denizen.objects.TradeTag;
import com.denizenscript.denizencore.objects.core.ListTag;
import com.denizenscript.denizencore.objects.properties.ObjectProperty;
import com.denizenscript.denizencore.objects.properties.PropertyParser;
import org.bukkit.inventory.MerchantInventory;
import org.bukkit.inventory.MerchantRecipe;

import java.util.ArrayList;

public class InventoryTrades extends ObjectProperty<InventoryTag> {

    public static boolean describes(InventoryTag inventory) {
        return inventory.getInventory() instanceof MerchantInventory;
    }

    @Override
    public ListTag getPropertyValue() {
        ListTag recipes = getTradeRecipes();
        if (recipes.isEmpty()) {
            return null;
        }
        return recipes;
    }

    @Override
    public String getPropertyId() {
        return "trades";
    }

    public ListTag getTradeRecipes() {
        ArrayList<TradeTag> recipes = new ArrayList<>();
        for (MerchantRecipe recipe : ((MerchantInventory) object.getInventory()).getMerchant().getRecipes()) {
            recipes.add(new TradeTag(recipe).duplicate());
        }
        return new ListTag(recipes);
    }

    public static void register() {

        // <--[tag]
        // @attribute <InventoryTag.trades>
        // @returns ListTag(TradeTag)
        // @group properties
        // @mechanism InventoryTag.trades
        // @description
        // Return the list of recipes from a merchant inventory.
        // -->
        PropertyParser.registerTag(InventoryTrades.class, ListTag.class, "trades", (attribute, inventory) -> {
            return inventory.getTradeRecipes();
        });

        // <--[mechanism]
        // @object InventoryTag
        // @name trades
        // @input ListTag(TradeTag)
        // @description
        // Sets the trade recipe list for a merchant inventory.
        // @tags
        // <InventoryTag.trades>
        // -->
        PropertyParser.registerMechanism(InventoryTrades.class, ListTag.class, "trades", (prop, mechanism, param) -> {
            ArrayList<MerchantRecipe> recipes = new ArrayList<>();
            for (TradeTag recipe : param.filter(TradeTag.class, mechanism.context)) {
                recipes.add(recipe.getRecipe());
            }
            ((MerchantInventory) prop.object.getInventory()).getMerchant().setRecipes(recipes);
        });
    }
}
