package com.denizenscript.denizen.objects.properties.trade;

import com.denizenscript.denizen.objects.TradeTag;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.properties.ObjectProperty;
import org.bukkit.inventory.MerchantRecipe;

public abstract class TradeProperty<TData extends ObjectTag> extends ObjectProperty<TradeTag, TData> {

    public MerchantRecipe getRecipe() {
        return object.getRecipe();
    }
}
