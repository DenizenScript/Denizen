package net.aufdemrand.denizen.tags.core;

import net.aufdemrand.denizen.objects.dColor;
import com.denizenscript.denizencore.objects.TagRunnable;
import com.denizenscript.denizencore.tags.Attribute;
import com.denizenscript.denizencore.tags.ReplaceableTagEvent;
import com.denizenscript.denizencore.tags.TagManager;
import com.denizenscript.denizencore.utilities.CoreUtilities;

public class ColorTags {

    public ColorTags() {
        TagManager.registerTagHandler(new TagRunnable.RootForm() {
            @Override
            public void run(ReplaceableTagEvent event) {
                colorTags(event);
            }
        }, "color");
    }

    public void colorTags(ReplaceableTagEvent event) {

        if (!event.matches("color") || event.replaced()) {
            return;
        }

        dColor color = null;

        if (event.hasNameContext()) {
            color = dColor.valueOf(event.getNameContext(), event.getAttributes().context);
        }

        if (color == null) {
            return;
        }

        Attribute attribute = event.getAttributes();
        event.setReplacedObject(CoreUtilities.autoAttrib(color, attribute.fulfill(1)));

    }
}
