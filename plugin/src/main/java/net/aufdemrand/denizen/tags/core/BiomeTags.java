package net.aufdemrand.denizen.tags.core;

import net.aufdemrand.denizen.objects.dBiome;
import com.denizenscript.denizencore.objects.TagRunnable;
import com.denizenscript.denizencore.tags.Attribute;
import com.denizenscript.denizencore.tags.ReplaceableTagEvent;
import com.denizenscript.denizencore.tags.TagManager;
import com.denizenscript.denizencore.utilities.CoreUtilities;

public class BiomeTags {

    public BiomeTags() {
        TagManager.registerTagHandler(new TagRunnable.RootForm() {
            @Override
            public void run(ReplaceableTagEvent event) {
                biomeTags(event);
            }
        }, "biome");
    }

    public void biomeTags(ReplaceableTagEvent event) {

        if (!event.matches("biome") || event.replaced()) {
            return;
        }

        dBiome biome = null;

        if (event.hasNameContext()) {
            biome = dBiome.valueOf(event.getNameContext(), event.getAttributes().context);
        }

        if (biome == null) {
            return;
        }

        Attribute attribute = event.getAttributes();
        event.setReplacedObject(CoreUtilities.autoAttrib(biome, attribute.fulfill(1)));

    }
}
