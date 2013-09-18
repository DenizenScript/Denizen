package net.aufdemrand.denizen.tags.core;

import net.aufdemrand.denizen.Denizen;
import net.aufdemrand.denizen.events.ReplaceableTagEvent;
import net.aufdemrand.denizen.scripts.ScriptEntry;
import net.aufdemrand.denizen.scripts.ScriptRegistry;
import net.aufdemrand.denizen.scripts.containers.ScriptContainer;
import net.aufdemrand.denizen.scripts.containers.core.TaskScriptContainer;
import net.aufdemrand.denizen.tags.Attribute;
import net.aufdemrand.denizen.utilities.debugging.dB;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.HashMap;
import java.util.Map;

public class ContextTags implements Listener {

    public ContextTags(Denizen denizen) {
        denizen.getServer().getPluginManager().registerEvents(this, denizen);
    }

    // Script tags!
    @EventHandler
    public void scriptTags(ReplaceableTagEvent event) {
        if (!event.matches("script, s") || event.getScriptEntry() == null) return;

        // Get script
        ScriptContainer script = event.getScriptEntry().getScript().getContainer();
        // Get type/subtype/specifier
        String type = event.getType();

        String sub_type = "";
        if (event.hasSubType()) sub_type = event.getSubType();
        String specifier = "";
        if (event.hasSpecifier()) specifier = event.getSpecifier();

        // User is attempting to specify a different scriptcontainer
        if (event.hasTypeContext())
            script = ScriptRegistry.getScriptContainer(event.getTypeContext());

        // Requirements
        if (type.equalsIgnoreCase("REQUIREMENTS")) {
            if (sub_type.equalsIgnoreCase("CHECK"))
                event.setReplaced(String.valueOf(event.getScriptEntry().getScript().getContainer()
                        .checkBaseRequirements(event.getPlayer(), event.getNPC())));
        }

        else if (type.equalsIgnoreCase("TYPE")) {
            event.setReplaced(script.getContainerType());
        }

        else if (type.equalsIgnoreCase("SPEED")) {
            if (script.contains("SPEED"))
                event.setReplaced(script.getString("SPEED"));
        }

        else if (type.equalsIgnoreCase("NAME")) {
            event.setReplaced(script.getName());
        }
    }

    // Get scriptqueue context!
    @EventHandler
    public void contextTags(ReplaceableTagEvent event) {
        if (!event.matches("context, c") || event.getScriptEntry() == null) return;

        String type = event.getType();

        // First, check queue object context.
        if (event.getScriptEntry().getResidingQueue().hasContext(type)) {
            Attribute attribute = new Attribute(event.raw_tag, event.getScriptEntry());
            event.setReplaced(event.getScriptEntry().getResidingQueue().getContext(type).getAttribute(attribute.fulfill(2)));
        }

        // Next, try to replace with task-script-defined context
        // NOTE: (DEPRECATED -- new RUN command uses definitions system instead)
        if (!ScriptRegistry.containsScript(event.getScriptEntry().getScript().getName(), TaskScriptContainer.class))
            return;

        TaskScriptContainer script = ScriptRegistry.getScriptContainerAs(
                event.getScriptEntry().getScript().getName(), TaskScriptContainer.class);

        ScriptEntry entry = event.getScriptEntry();

        if (entry.hasObject("CONTEXT")) {
            // Get context
            Map<String, String> context = (HashMap<String, String>) entry.getObject("CONTEXT");
            // Build IDs
            Map<String, Integer> id = script.getContextMap();
            if (context.containsKey( String.valueOf(id.get(type.toUpperCase())))) {
                event.setReplaced(context.get(String.valueOf(id.get(type.toUpperCase()))));
            }
        }

        else event.setReplaced("null");

    }

    // Get a saved script entry!
    @EventHandler
    public void savedEntryTags(ReplaceableTagEvent event) {
        if (!event.matches("entry, e") || event.getScriptEntry() == null) return;

        // <e[entry_id].entity.blah.blah>

        if (event.getScriptEntry().getResidingQueue() != null &&
                event.getScriptEntry().getResidingQueue().getHeldScriptEntry(event.getNameContext()) != null) {

            // Get the entry_id from name context
            String id = event.getNameContext();

            Attribute attribute = new Attribute(event.raw_tag, event.getScriptEntry());
            ScriptEntry held = event.getScriptEntry().getResidingQueue().getHeldScriptEntry(id);
            if (held == null) { // Check if the ID is bad
                dB.echoError("Bad saved entry ID " + id);
            }
            else {
                if (!held.hasObject(attribute.getAttribute(2)) // Check if there's no such object
                        || held.getdObject(attribute.getAttribute(2)) == null) { // ... Check if there is such an object
                    dB.echoError("Bad saved entry object " + attribute.getAttribute(2)); // but it's not a dObject...
                }
                else { // Okay, now it's safe!
                    event.setReplaced(held.getdObject(attribute.getAttribute(2)).getAttribute(attribute.fulfill(2)));
                }
            }
        }

        else event.setReplaced("null");
    }

}
