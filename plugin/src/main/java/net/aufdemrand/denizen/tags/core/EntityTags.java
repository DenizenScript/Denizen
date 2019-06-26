package net.aufdemrand.denizen.tags.core;

import net.aufdemrand.denizen.objects.dEntity;
import net.aufdemrand.denizencore.objects.TagRunnable;
import net.aufdemrand.denizencore.tags.Attribute;
import net.aufdemrand.denizencore.tags.ReplaceableTagEvent;
import net.aufdemrand.denizencore.tags.TagManager;
import net.aufdemrand.denizencore.utilities.CoreUtilities;

public class EntityTags {

    public EntityTags() {
        TagManager.registerTagHandler(new TagRunnable.RootForm() {
            @Override
            public void run(ReplaceableTagEvent event) {
                entityTags(event);
            }
        }, "entity");
    }

    //////////
    //  ReplaceableTagEvent handler
    ////////

    public void entityTags(ReplaceableTagEvent event) {

        if (!event.matches("entity") || event.replaced()) {
            return;
        }

        dEntity entity = null;

        if (event.hasNameContext()) {
            entity = dEntity.valueOf(event.getNameContext(), event.getAttributes().context);
        }

        if (entity == null) {
            return;
        }

        Attribute attribute = event.getAttributes();
        event.setReplacedObject(CoreUtilities.autoAttrib(entity, attribute.fulfill(1)));

    }
}
