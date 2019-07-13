package net.aufdemrand.denizen.tags.core;

import net.aufdemrand.denizen.objects.dEllipsoid;
import com.denizenscript.denizencore.objects.TagRunnable;
import com.denizenscript.denizencore.tags.Attribute;
import com.denizenscript.denizencore.tags.ReplaceableTagEvent;
import com.denizenscript.denizencore.tags.TagManager;
import com.denizenscript.denizencore.utilities.CoreUtilities;

public class EllipsoidTags {

    public EllipsoidTags() {
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
