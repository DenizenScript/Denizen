package com.denizenscript.denizen.objects.properties.trade;

import com.denizenscript.denizen.objects.TradeTag;
import com.denizenscript.denizencore.objects.properties.ObjectProperty;
import org.bukkit.inventory.MerchantRecipe;

public abstract class TradeProperty extends ObjectProperty<TradeTag> {

    public MerchantRecipe getRecipe() {
        return object.getRecipe();
    }
}
