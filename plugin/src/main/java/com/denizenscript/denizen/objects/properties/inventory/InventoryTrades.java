package com.denizenscript.denizen.objects.properties.inventory;

import com.denizenscript.denizen.objects.InventoryTag;
import com.denizenscript.denizen.objects.TradeTag;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.core.ListTag;
import com.denizenscript.denizencore.objects.properties.Property;
import com.denizenscript.denizencore.objects.properties.PropertyParser;
import org.bukkit.inventory.MerchantInventory;
import org.bukkit.inventory.MerchantRecipe;

import java.util.ArrayList;

public class InventoryTrades implements Property {

    public static boolean describes(ObjectTag inventory) {
        return inventory instanceof InventoryTag && ((InventoryTag) inventory).getInventory() instanceof MerchantInventory;
    }

    public static InventoryTrades getFrom(ObjectTag inventory) {
        if (!describes(inventory)) {
            return null;
        }
        return new InventoryTrades((InventoryTag) inventory);
    }

    public static final String[] handledMechs = new String[] {
            "trades"
    };

    InventoryTag inventory;

    public InventoryTrades(InventoryTag inventory) {
        this.inventory = inventory;
    }
    public ListTag getTradeRecipes() {
        ArrayList<TradeTag> recipes = new ArrayList<>();
        for (MerchantRecipe recipe : ((MerchantInventory) inventory.getInventory()).getMerchant().getRecipes()) {
            recipes.add(new TradeTag(recipe).duplicate());
        }
        return new ListTag(recipes);
    }

    @Override
    public String getPropertyString() {
        ListTag recipes = getTradeRecipes();
        if (recipes.isEmpty()) {
            return null;
        }
        return recipes.identify();
    }

    @Override
    public String getPropertyId() {
        return "trades";
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
    }

    @Override
    public void adjust(Mechanism mechanism) {

        // <--[mechanism]
        // @object InventoryTag
        // @name trades
        // @input ListTag(TradeTag)
        // @description
        // Sets the trade recipe list for a merchant inventory.
        // @tags
        // <InventoryTag.trades>
        // -->
        if (mechanism.matches("trades") && mechanism.requireInteger()) {
            ArrayList<MerchantRecipe> recipes = new ArrayList<>();
            for (TradeTag recipe : mechanism.valueAsType(ListTag.class).filter(TradeTag.class, mechanism.context)) {
                recipes.add(recipe.getRecipe());
            }
            ((MerchantInventory) inventory.getInventory()).getMerchant().setRecipes(recipes);
        }
    }
}
