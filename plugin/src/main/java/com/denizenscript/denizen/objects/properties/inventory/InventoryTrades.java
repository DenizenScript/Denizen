package com.denizenscript.denizen.objects.properties.inventory;

import com.denizenscript.denizen.objects.InventoryTag;
import com.denizenscript.denizen.objects.TradeTag;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.core.ListTag;
import com.denizenscript.denizencore.objects.properties.ObjectProperty;
import org.bukkit.inventory.MerchantInventory;
import org.bukkit.inventory.MerchantRecipe;

import java.util.ArrayList;

public class InventoryTrades extends ObjectProperty<InventoryTag, ListTag> {

    // <--[property]
    // @object InventoryTag
    // @name trades
    // @input ListTag(TradeTag)
    // @description
    // Controls the trade recipe list for a merchant inventory.
    // -->

    public static boolean describes(InventoryTag inventory) {
        return inventory.getInventory() instanceof MerchantInventory;
    }

    @Override
    public boolean isDefaultValue(ListTag list) {
        return list.isEmpty();
    }

    @Override
    public ListTag getPropertyValue() {
        ArrayList<TradeTag> recipes = new ArrayList<>();
        for (MerchantRecipe recipe : ((MerchantInventory) object.getInventory()).getMerchant().getRecipes()) {
            recipes.add(new TradeTag(recipe).duplicate());
        }
        return new ListTag(recipes);
    }

    @Override
    public void setPropertyValue(ListTag list, Mechanism mechanism) {
        ArrayList<MerchantRecipe> recipes = new ArrayList<>();
        for (TradeTag recipe : list.filter(TradeTag.class, mechanism.context)) {
            recipes.add(recipe.getRecipe());
        }
        ((MerchantInventory) object.getInventory()).getMerchant().setRecipes(recipes);
    }

    @Override
    public String getPropertyId() {
        return "trades";
    }

    public static void register() {
        autoRegister("trades", InventoryTrades.class, ListTag.class, false);
    }
}
