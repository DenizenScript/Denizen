package net.aufdemrand.denizen.tags.core;

import net.aufdemrand.denizen.objects.dTrade;
import net.aufdemrand.denizencore.objects.TagRunnable;
import net.aufdemrand.denizencore.tags.Attribute;
import net.aufdemrand.denizencore.tags.ReplaceableTagEvent;
import net.aufdemrand.denizencore.tags.TagManager;
import net.aufdemrand.denizencore.utilities.CoreUtilities;

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
