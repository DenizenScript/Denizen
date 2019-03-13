package net.aufdemrand.denizen.tags.core;

import net.aufdemrand.denizen.Denizen;
import net.aufdemrand.denizen.objects.dMaterial;
import net.aufdemrand.denizencore.objects.TagRunnable;
import net.aufdemrand.denizencore.tags.Attribute;
import net.aufdemrand.denizencore.tags.ReplaceableTagEvent;
import net.aufdemrand.denizencore.tags.TagManager;
import net.aufdemrand.denizencore.utilities.CoreUtilities;

public class MaterialTags {

    public MaterialTags(Denizen denizen) {
        TagManager.registerTagHandler(new TagRunnable.RootForm() {
            @Override
            public void run(ReplaceableTagEvent event) {
                materialTags(event);
            }
        }, "material");
    }

    public void materialTags(ReplaceableTagEvent event) {

        if (!event.matches("material") || event.replaced()) {
            return;
        }

        dMaterial material = null;

        if (event.hasNameContext()) {
            material = dMaterial.valueOf(event.getNameContext(), event.getAttributes().context);
        }

        if (material == null) {
            return;
        }

        Attribute attribute = event.getAttributes();
        event.setReplacedObject(CoreUtilities.autoAttrib(material, attribute.fulfill(1)));

    }
}
