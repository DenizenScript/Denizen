package net.aufdemrand.denizen.tags.core;

import net.aufdemrand.denizen.Denizen;
import net.aufdemrand.denizen.objects.dEntity;
import net.aufdemrand.denizen.utilities.debugging.dB;
import net.aufdemrand.denizencore.tags.Attribute;
import net.aufdemrand.denizencore.tags.ReplaceableTagEvent;
import net.aufdemrand.denizencore.tags.TagManager;

public class EntityTags {

    public EntityTags(Denizen denizen) {
        TagManager.registerTagEvents(this);
    }

    //////////
    //  ReplaceableTagEvent handler
    ////////

    @TagManager.TagEvents
    public void entityTags(ReplaceableTagEvent event) {

        if (!event.matches("entity") || event.replaced()) {
            return;
        }

        // Build a new attribute out of the raw_tag supplied in the script to be fulfilled
        Attribute attribute = event.getAttributes();

        dEntity e = null;

        // Entity tag may specify a new entity in the <entity[context]...> portion of the tag.
        if (attribute.hasContext(1)) {
            // Check if this is a valid entity and update the dEntity object reference.
            if (attribute.getIntContext(1) >= 1) {
                e = dEntity.valueOf("e@" + attribute.getContext(1)); // TODO: Is the e@ needed here? If so, why? Should it be?
            }
        }

        if (e == null || !e.isValid()) {
            if (!event.hasAlternative()) {
                dB.echoError("Invalid or missing entity for tag <" + event.raw_tag + ">!");
            }
            return;
        }

        event.setReplaced(e.getAttribute(attribute.fulfill(1)));
    }
}
