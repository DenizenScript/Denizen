package com.denizenscript.denizen.tags.core;

import com.denizenscript.denizen.objects.dTrade;
import com.denizenscript.denizencore.objects.TagRunnable;
import com.denizenscript.denizencore.tags.Attribute;
import com.denizenscript.denizencore.tags.ReplaceableTagEvent;
import com.denizenscript.denizencore.tags.TagManager;
import com.denizenscript.denizencore.utilities.CoreUtilities;

public class TradeTags {

    public TradeTags() {
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

        dTrade trade = null;

        if (event.hasNameContext()) {
            trade = dTrade.valueOf(event.getNameContext(), event.getAttributes().context);
        }

        if (trade == null) {
            return;
        }

        Attribute attribute = event.getAttributes();
        event.setReplacedObject(CoreUtilities.autoAttrib(trade, attribute.fulfill(1)));

    }
}
