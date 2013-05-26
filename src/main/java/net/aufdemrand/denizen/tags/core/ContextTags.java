package net.aufdemrand.denizen.tags.core;

import net.aufdemrand.denizen.Denizen;
import net.aufdemrand.denizen.events.ReplaceableTagEvent;
import net.aufdemrand.denizen.interfaces.dScriptArgument;
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

    @EventHandler
    public void getEntry(ReplaceableTagEvent event) {
        if (!event.matches("SCRIPT") || event.getScriptEntry() == null) return;

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
            event.setReplaced(script.getType());
        }

        else if (type.equalsIgnoreCase("SPEED")) {
            if (script.contains("SPEED"))
                event.setReplaced(script.getString("SPEED"));
        }

        else if (type.equalsIgnoreCase("NAME")) {
            event.setReplaced(script.getName());
        }
    }


    @EventHandler
    public void getContext(ReplaceableTagEvent event) {
        if (!event.matches("CONTEXT") || event.getScriptEntry() == null) return;

        String type = event.getType();

        // First check for entry object context
        if (event.getScriptEntry().hasObject(type)) {

            if (event.getScriptEntry().getObject(type) instanceof dScriptArgument) {
                Attribute attribute = new Attribute(event.raw_tag, event.getScriptEntry());
                event.setReplaced(((dScriptArgument) event.getScriptEntry().getObject(type)).getAttribute(attribute.fulfill(2)));
            } else
                event.setReplaced(event.getScriptEntry().getObject(type).toString());

            return;
        }

        // Next, try to replace with task-script-defined context
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
    }



}