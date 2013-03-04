package net.aufdemrand.denizen.scripts.containers.core;

import net.aufdemrand.denizen.npc.actions.ActionHandler;
import net.aufdemrand.denizen.npc.dNPC;
import net.aufdemrand.denizen.scripts.ScriptBuilder;
import net.aufdemrand.denizen.scripts.ScriptEntry;
import net.aufdemrand.denizen.scripts.ScriptQueue;
import net.aufdemrand.denizen.scripts.commands.core.DetermineCommand;
import net.aufdemrand.denizen.scripts.containers.ScriptContainer;
import net.aufdemrand.denizen.tags.TagManager;
import net.aufdemrand.denizen.utilities.DenizenAPI;
import net.aufdemrand.denizen.utilities.arguments.Location;
import net.aufdemrand.denizen.utilities.arguments.aH;
import net.aufdemrand.denizen.utilities.debugging.dB;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerEvent;
import org.bukkit.event.player.PlayerMoveEvent;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class WorldScriptHelper implements Listener {

    public static Map<String, WorldScriptContainer> world_scripts = new ConcurrentHashMap<String, WorldScriptContainer>();

    public WorldScriptHelper() {
        DenizenAPI.getCurrentInstance().getServer().getPluginManager()
                .registerEvents(this, DenizenAPI.getCurrentInstance());
    }

    public boolean doEvent(String eventName, dNPC npc, Player player, Map<String, String> context) {

        boolean cancelled = false;
        for (WorldScriptContainer script : world_scripts.values()) {

            if (script == null) continue;
            // if (!script.contains("EVENTS.ON " + eventName.toUpperCase())) return false;

            dB.report("Event",
                    aH.debugObj("Type", "On " + eventName)
                            + script.getAsScriptArg().debug()
                            + (npc != null ? aH.debugObj("NPC", npc.toString()) : "")
                            + (player != null ? aH.debugObj("Player", player.getName()) : "")
                            + (context != null ? aH.debugObj("Context", context.toString()) : ""));

            // Fetch script from Event
            List<ScriptEntry> entries = script.getEntries(player, npc, "events.on " + eventName);
            if (entries.isEmpty()) continue;

            dB.echoDebug(dB.DebugElement.Header, "Building event 'On " + eventName.toUpperCase() + "' for " + script.getName());

            if (context != null)
                for (Map.Entry<String, String> entry : context.entrySet())
                    ScriptBuilder.addObjectToEntries(entries, entry.getKey(), entry.getValue());

            // Create new ID -- this is what we will look for when determining an outcome
            long id = DetermineCommand.getNewId();

            // Add the reqId to each of the entries
            ScriptBuilder.addObjectToEntries(entries, "ReqId", id);
            ScriptQueue._getInstantQueue(ScriptQueue._getNextId()).addEntries(entries).start();

            if (DetermineCommand.outcomes.containsKey(id)
                    && DetermineCommand.outcomes.get(id).equalsIgnoreCase("CANCELLED")) {
                cancelled = true;
            }
        }
        return cancelled;
    }


    @EventHandler
    public void commandEvent(PlayerCommandPreprocessEvent event) {
        Map<String, String> context = new HashMap<String, String>();
        context.put("arg", (event.getMessage().split(" ").length > 1 ? event.getMessage().split(" ", 2)[1] : ""));
        if (doEvent(event.getMessage().split(" ")[0].replace("/", "") + " command", null, event.getPlayer(), context))
            event.setCancelled(true);
    }


    @EventHandler
    public void somethingElse(PlayerMoveEvent event) {
        if (event.getFrom().getBlock().equals(event.getTo().getBlock())) return;

        String name = Location.isSavedLocation(event.getPlayer().getLocation());
        if (name != null)
            doEvent("walked over " + name, null, event.getPlayer(), null);

    }

}
