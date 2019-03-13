package net.aufdemrand.denizen.tags.core;

import net.aufdemrand.denizen.Denizen;
import net.aufdemrand.denizen.objects.dEllipsoid;
import net.aufdemrand.denizencore.objects.TagRunnable;
import net.aufdemrand.denizencore.tags.Attribute;
import net.aufdemrand.denizencore.tags.ReplaceableTagEvent;
import net.aufdemrand.denizencore.tags.TagManager;
import net.aufdemrand.denizencore.utilities.CoreUtilities;

public class EllipsoidTags {

    public EllipsoidTags(Denizen denizen) {
        TagManager.registerTagHandler(new TagRunnable.RootForm() {
            @Override
            public void run(ReplaceableTagEvent event) {
                ellipsoidTags(event);
            }
        }, "ellipsoid");
    }

    public void ellipsoidTags(ReplaceableTagEvent event) {

        if (!event.matches("ellipsoid") || event.replaced()) {
            return;
        }

        dEllipsoid ellipsoid = null;

        if (event.hasNameContext()) {
            ellipsoid = dEllipsoid.valueOf(event.getNameContext(), event.getAttributes().context);
        }

        if (ellipsoid == null) {
            return;
        }

        Attribute attribute = event.getAttributes();
        event.setReplacedObject(CoreUtilities.autoAttrib(ellipsoid, attribute.fulfill(1)));

    }
}
