package net.aufdemrand.denizen.tags.core;

import net.aufdemrand.denizen.Denizen;
import net.aufdemrand.denizen.events.bukkit.ReplaceableTagEvent;
import net.aufdemrand.denizen.objects.Element;
import net.aufdemrand.denizen.objects.ObjectFetcher;
import net.aufdemrand.denizen.tags.Attribute;
import net.aufdemrand.denizen.utilities.debugging.dB;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class DefinitionTags implements Listener {

    public DefinitionTags(Denizen denizen) {
        denizen.getServer().getPluginManager().registerEvents(this, denizen);
    }


    //////////
    //  ReplaceableTagEvent handler
    ////////

    @EventHandler
    public void definitionTag(ReplaceableTagEvent event) {

        if (!event.matches("definition", "def", "d")) return;

        if (!event.hasNameContext()) {
            dB.echoError("Invalid definition tag, no context specified!");
            return;
        }

        // <--[tag]
        // @attribute <definition[<name>]>
        // @returns dObject
        // @description
        // Returns a definition from the current queue.
        // The object will be returned as the most-valid type based on the input.
        // -->
        // Get the definition from the name input
        String defName = event.getNameContext();
        if (event.getScriptEntry() == null) {
            dB.echoError("No definitions available outside of a queue.");
            return;
        }
        String def = event.getScriptEntry().getResidingQueue().getDefinition(defName);

        Attribute atttribute = event.getAttributes().fulfill(1);

        // <--[tag]
        // @attribute <definition[<name>].exists>
        // @returns Element(Boolean)
        // @description
        // Returns whether a definition exists for the given definition name.
        // -->
        if (atttribute.startsWith("exists")) {
            if (def == null)
                event.setReplaced(Element.FALSE.getAttribute(atttribute.fulfill(1)));
            else
                event.setReplaced(Element.TRUE.getAttribute(atttribute.fulfill(1)));
            return;
        }

        // No invalid definitions!
        if (def == null) {
            if (!event.hasAlternative())
            dB.echoError("Invalid definition name '" + defName + "'.");
            return;
        }


        event.setReplaced(ObjectFetcher.pickObjectFor(def)
                .getAttribute(atttribute));
    }
}


