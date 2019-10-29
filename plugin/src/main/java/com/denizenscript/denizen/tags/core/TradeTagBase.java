package com.denizenscript.denizen.tags.core;

import com.denizenscript.denizen.objects.TradeTag;
import com.denizenscript.denizencore.tags.TagRunnable;
import com.denizenscript.denizencore.tags.Attribute;
import com.denizenscript.denizencore.tags.ReplaceableTagEvent;
import com.denizenscript.denizencore.tags.TagManager;
import com.denizenscript.denizencore.utilities.CoreUtilities;

public class TradeTagBase {

    public TradeTagBase() {

        // <--[tag]
        // @attribute <trade[<trade>]>
        // @returns TradeTag
        // @description
        // Returns a trade object constructed from the input value.
        // -->
        TagManager.registerTagHandler(new TagRunnable.RootForm() {
            @Override
            public void run(ReplaceableTagEvent event) {
                tradeTags(event);
            }
        }, "trade");
    }

    public void tradeTags(ReplaceableTagEvent event) {

        if (!event.matches("trade") || event.replaced()) {
            return;
        }

        TradeTag trade = null;

        if (event.hasNameContext()) {
            trade = TradeTag.valueOf(event.getNameContext(), event.getAttributes().context);
        }

        if (trade == null) {
            return;
        }

        Attribute attribute = event.getAttributes();
        event.setReplacedObject(CoreUtilities.autoAttrib(trade, attribute.fulfill(1)));

    }
}
