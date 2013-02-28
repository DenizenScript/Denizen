package net.aufdemrand.denizen.tags.core;

import net.aufdemrand.denizen.Denizen;
import net.aufdemrand.denizen.events.ReplaceableTagEvent;
import net.aufdemrand.denizen.scripts.ScriptEntry;
import net.aufdemrand.denizen.scripts.ScriptRegistry;
import net.aufdemrand.denizen.scripts.containers.core.TaskScriptContainer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.HashMap;
import java.util.Map;

public class ContextTags implements Listener {

    public ContextTags(Denizen denizen) {
        denizen.getServer().getPluginManager().registerEvents(this, denizen);
    }

    @EventHandler
    public void getEntry(ReplaceableTagEvent event) {
        if (!event.matches("ENTRY") || event.getScriptEntry() == null) return;

        String type = event.getType();
        String subType = "";
        if (event.hasSubType()) subType = event.getSubType();
        ScriptEntry entry = event.getScriptEntry();

        if (type.equalsIgnoreCase("PLAYER")) {
            event.setReplaced(entry.getPlayer().getName());

        } else if (type.equalsIgnoreCase("SCRIPT")) {
            if (subType.equalsIgnoreCase("NAME"))
                event.setReplaced(entry.getScript().getName());

        } else if (type.equalsIgnoreCase("OBJECT")) {
            if (entry.hasObject(subType))
                event.setReplaced(entry.getObject(subType).toString());

        } else if (type.equalsIgnoreCase("SCRIPT")) {
            if (subType.equalsIgnoreCase("QUEUE"))
                event.setReplaced(entry.getResidingQueue().id);
        }

    }

    @EventHandler
    public void getContext(ReplaceableTagEvent event) {
        if (!event.matches("CONTEXT") || event.getScriptEntry() == null) return;

        // getContext requires a Task Script!
        if (!ScriptRegistry.containsScript(event.getScriptEntry().getScript().getName(), TaskScriptContainer.class))
            return;

        TaskScriptContainer script = ScriptRegistry.getScriptContainerAs(
                    event.getScriptEntry().getScript().getName(), TaskScriptContainer.class);


        String type = event.getType();
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
    }



}