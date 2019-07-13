package net.aufdemrand.denizen.objects.properties.trade;

import net.aufdemrand.denizen.objects.dItem;
import net.aufdemrand.denizen.objects.dTrade;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.dObject;
import com.denizenscript.denizencore.objects.properties.Property;
import com.denizenscript.denizencore.tags.Attribute;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.MerchantRecipe;

public class TradeResult implements Property {

    public static boolean describes(dObject recipe) {
        return recipe instanceof dTrade;
    }

    public static TradeResult getFrom(dObject recipe) {
        if (!describes(recipe)) {
            return null;
        }
        return new TradeResult((dTrade) recipe);
    }

    public static final String[] handledTags = new String[] {
            "result"
    };

    public static final String[] handledMechs = new String[] {
            "result"
    };

    private dTrade recipe;

    public TradeResult(dTrade recipe) {
        this.recipe = recipe;
    }

    public String getPropertyString() {
        if (recipe.getRecipe() == null) {
            return null;
        }
        return (new dItem(recipe.getRecipe().getResult())).identify();
    }

    public String getPropertyId() {
        return "result";
    }

    public String getAttribute(Attribute attribute) {
        if (attribute == null) {
            return null;
        }

        // <--[tag]
        // @attribute <trade@trade.result>
        // @returns dItem
        // @mechanism dTrade.result
        // @description
        // Returns what the trade will give the player.
        // -->
        if (attribute.startsWith("result")) {
            return new dItem(recipe.getRecipe().getResult()).getAttribute(attribute.fulfill(1));
        }

        return null;
    }

    public void adjust(Mechanism mechanism) {

        // <--[mechanism]
        // @object dTrade
        // @name result
        // @input dItem
        // @description
        // Sets what the trade will give the player.
        // @tags
        // <trade@trade.result>
        // -->
        if (mechanism.matches("result") && mechanism.requireObject(dItem.class)) {
            ItemStack item = mechanism.valueAsType(dItem.class).getItemStack();
            MerchantRecipe oldRecipe = recipe.getRecipe();

            MerchantRecipe newRecipe = new MerchantRecipe(item, oldRecipe.getUses(), oldRecipe.getMaxUses(), oldRecipe.hasExperienceReward());
            newRecipe.setIngredients(oldRecipe.getIngredients());

            recipe.setRecipe(newRecipe);
        }
    }
}
