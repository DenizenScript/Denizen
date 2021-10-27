package com.denizenscript.denizen.tags.core;

import com.denizenscript.denizen.objects.TradeTag;
import com.denizenscript.denizencore.tags.TagManager;

public class TradeTagBase {

    public TradeTagBase() {

        // <--[tag]
        // @attribute <trade[<trade>]>
        // @returns TradeTag
        // @description
        // Returns a trade object constructed from the input value.
        // Refer to <@link objecttype TradeTag>.
        // -->
        TagManager.registerStaticTagBaseHandler(TradeTag.class, "trade", (attribute) -> {
            if (!attribute.hasContext(1)) {
                attribute.echoError("Trade tag base must have input.");
                return null;
            }
            return TradeTag.valueOf(attribute.getContext(1), attribute.context);
        });
    }
}
