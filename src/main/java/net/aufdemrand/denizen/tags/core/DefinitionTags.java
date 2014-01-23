package net.aufdemrand.denizen.tags.core;

import net.aufdemrand.denizen.Denizen;
import net.aufdemrand.denizen.events.bukkit.ReplaceableTagEvent;
import net.aufdemrand.denizen.objects.ObjectFetcher;
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

        if (!event.matches("definition, def, d")) return;

        if (!event.hasNameContext()) {
            dB.echoError("Invalid definition tag, no context specified!");
            return;
        }

        // Get the definition from the name input
        String def = event.getScriptEntry().getResidingQueue().getDefinition(event.getNameContext());

        // No invalid definitions!
        if (def == null) {
            dB.echoError("Invalid definition name '" + event.getNameContext() + "'.");
            return;
        }

        // <--[tag]
        // @attribute <definition[<name>]>
        // @returns dObject
        // @description
        // Returns a definition from the current queue.
        // The object will be returned as the most-valid type based on the input.
        // -->

        event.setReplaced(ObjectFetcher.pickObjectFor(def)
                .getAttribute(event.getAttributes().fulfill(1)));
    }


}


