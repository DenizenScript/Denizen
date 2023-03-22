package com.denizenscript.denizen.objects.properties.trade;

import com.denizenscript.denizen.objects.ItemTag;
import com.denizenscript.denizen.objects.TradeTag;
import com.denizenscript.denizencore.objects.properties.PropertyParser;

public class TradeResult extends TradeProperty {

    public static boolean describes(TradeTag recipe) {
        return true;
    }

    @Override
    public ItemTag getPropertyValue() {
        return new ItemTag(getRecipe().getResult());
    }

    @Override
    public String getPropertyId() {
        return "result";
    }

    public static void register() {

        // <--[tag]
        // @attribute <TradeTag.result>
        // @returns ItemTag
        // @mechanism TradeTag.result
        // @description
        // Returns what the trade will give the player.
        // -->
        PropertyParser.registerTag(TradeResult.class, ItemTag.class, "result", (attribute, prop) -> {
            return new ItemTag(prop.getRecipe().getResult());
        });

        // <--[mechanism]
        // @object TradeTag
        // @name result
        // @input ItemTag
        // @description
        // Sets what the trade will give the player.
        // @tags
        // <TradeTag.result>
        // -->
        PropertyParser.registerMechanism(TradeResult.class, ItemTag.class, "result", (prop, mechanism, item) -> {
            prop.object.setRecipe(TradeTag.duplicateRecipe(item.getItemStack(), prop.getRecipe()));
        });
    }
}
