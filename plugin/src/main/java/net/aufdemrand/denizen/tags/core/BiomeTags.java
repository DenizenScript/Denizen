package net.aufdemrand.denizen.tags.core;

import net.aufdemrand.denizen.Denizen;
import net.aufdemrand.denizen.objects.dBiome;
import net.aufdemrand.denizencore.objects.TagRunnable;
import net.aufdemrand.denizencore.tags.Attribute;
import net.aufdemrand.denizencore.tags.ReplaceableTagEvent;
import net.aufdemrand.denizencore.tags.TagManager;
import net.aufdemrand.denizencore.utilities.CoreUtilities;

public class BiomeTags {

    public BiomeTags(Denizen denizen) {
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
