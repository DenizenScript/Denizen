package net.aufdemrand.denizen.tags.core;

import net.aufdemrand.denizen.Denizen;
import net.aufdemrand.denizen.events.ReplaceableTagEvent;
import net.aufdemrand.denizen.scripts.ScriptEntry;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.HashMap;

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
        }
    }

    @EventHandler
    public void getContext(ReplaceableTagEvent event) {
        if (!event.matches("CONTEXT") || event.getScriptEntry() == null) return;

        String type = event.getType();
        ScriptEntry entry = event.getScriptEntry();

        if (entry.hasObject("INHERITED-CONTEXT")) {
            HashMap<String, String> context = (HashMap<String, String>) entry.getObject("INHERITED-CONTEXT");
            if (context == null) return;
            if (context.containsKey(type.toUpperCase())) {
                event.setReplaced(context.get(type.toUpperCase()));
            }
        }
    }



}