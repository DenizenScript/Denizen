package net.aufdemrand.denizen.tags.core;

import net.aufdemrand.denizen.Denizen;
import net.aufdemrand.denizen.tags.ReplaceableTagEvent;
import net.aufdemrand.denizen.objects.dScript;
import net.aufdemrand.denizen.tags.Attribute;
import net.aufdemrand.denizen.tags.TagManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;


/**
 * Script tag is a starting point for getting attributes from an embedded
 *
 */

public class ScriptTags implements Listener {

    public ScriptTags(Denizen denizen) {
        denizen.getServer().getPluginManager().registerEvents(this, denizen);
        TagManager.registerTagEvents(this);
    }

    @TagManager.TagEvents
    public void scriptTags(ReplaceableTagEvent event) {

        if (!event.matches("script", "s") || event.replaced()) return;

        // Stage the location
        dScript script = null;

        // Check name context for a specified script, or check
        // the ScriptEntry for a 'script' context
        if (event.hasNameContext() && dScript.matches(event.getNameContext()))
            script = dScript.valueOf(event.getNameContext());
        else if (event.getScript() != null)
            script = event.getScript();
        else if (event.getScriptEntry() == null)
            return;
        else if (event.getScriptEntry().getScript() != null)
            script = event.getScriptEntry().getScript();
        else if (event.getScriptEntry().hasObject("script"))
            script = (dScript) event.getScriptEntry().getObject("script");

        // Build and fill attributes
        Attribute attribute = new Attribute(event.raw_tag, event.getScriptEntry());

        // Check if location is null, return null if it is
        if (script == null) { return; }

        // Else, get the attribute from the script
        event.setReplaced(script.getAttribute(attribute.fulfill(1)));

    }
}
