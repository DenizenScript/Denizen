package net.aufdemrand.denizen.tags.core;

import net.aufdemrand.denizen.objects.dCuboid;
import com.denizenscript.denizencore.objects.TagRunnable;
import com.denizenscript.denizencore.tags.Attribute;
import com.denizenscript.denizencore.tags.ReplaceableTagEvent;
import com.denizenscript.denizencore.tags.TagManager;
import com.denizenscript.denizencore.utilities.CoreUtilities;


public class CuboidTags {

    public CuboidTags() {
        TagManager.registerTagHandler(new TagRunnable.RootForm() {
            @Override
            public void run(ReplaceableTagEvent event) {
                cuboidTags(event);
            }
        }, "cuboid");
    }

    public void cuboidTags(ReplaceableTagEvent event) {

        if (!event.matches("cuboid") || event.replaced()) {
            return;
        }

        dCuboid cuboid = null;

        if (event.hasNameContext()) {
            cuboid = dCuboid.valueOf(event.getNameContext(), event.getAttributes().context);
        }

        // Check if cuboid is null, return if it is
        if (cuboid == null) {
            return;
        }

        // Build and fill attributes
        Attribute attribute = event.getAttributes();
        event.setReplacedObject(CoreUtilities.autoAttrib(cuboid, attribute.fulfill(1)));

    }
}
