package com.denizenscript.denizen.objects.properties.trade;

import com.denizenscript.denizen.objects.ItemTag;
import com.denizenscript.denizen.objects.TradeTag;
import com.denizenscript.denizencore.objects.Mechanism;

public class TradeResult extends TradeProperty<ItemTag> {

    // <--[property]
    // @object TradeTag
    // @name result
    // @input ItemTag
    // @description
    // Controls what the trade will give the player.
    // -->

    public static boolean describes(TradeTag recipe) {
        return true;
    }

    @Override
    public ItemTag getPropertyValue() {
        return new ItemTag(getRecipe().getResult());
    }

    @Override
    public void setPropertyValue(ItemTag item, Mechanism mechanism) {
        object.setRecipe(TradeTag.duplicateRecipe(item.getItemStack(), getRecipe()));
    }

    @Override
    public String getPropertyId() {
        return "result";
    }

    public static void register() {
        autoRegister("result", TradeResult.class, ItemTag.class, false);
    }
}
