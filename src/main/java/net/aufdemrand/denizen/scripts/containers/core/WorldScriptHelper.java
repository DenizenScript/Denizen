package net.aufdemrand.denizen.scripts.containers.core;

import net.aufdemrand.denizen.npc.dNPC;
import net.aufdemrand.denizen.scripts.ScriptBuilder;
import net.aufdemrand.denizen.scripts.ScriptEntry;
import net.aufdemrand.denizen.scripts.ScriptQueue;
import net.aufdemrand.denizen.scripts.commands.core.DetermineCommand;
import net.aufdemrand.denizen.utilities.DenizenAPI;
import net.aufdemrand.denizen.utilities.arguments.Item;
import net.aufdemrand.denizen.utilities.arguments.Location;
import net.aufdemrand.denizen.utilities.arguments.aH;
import net.aufdemrand.denizen.utilities.debugging.dB;
import net.citizensnpcs.api.CitizensAPI;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.player.*;

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
            if (!script.contains("EVENTS.ON " + eventName.toUpperCase())) continue;

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

            if (context != null) {
                for (Map.Entry<String, String> entry : context.entrySet()) {
                    ScriptBuilder.addObjectToEntries(entries, entry.getKey(), entry.getValue());
                }
            }

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
        context.put("args", (event.getMessage().split(" ").length > 1 ? event.getMessage().split(" ", 2)[1] : ""));
        if (doEvent(event.getMessage().split(" ")[0].replace("/", "") + " command", null, event.getPlayer(), context)) {
            event.setCancelled(true);
        }
    }


    @EventHandler
    public void loginEvent(PlayerLoginEvent event) {
        Map<String, String> context = new HashMap<String, String>();
        context.put("ip", event.getHostname());

        doEvent("player login", null, event.getPlayer(), context);
    }

    @EventHandler
    public void loginEvent(PlayerQuitEvent event) {
        Map<String, String> context = new HashMap<String, String>();
        context.put("message", event.getQuitMessage());

        doEvent("player quit", null, event.getPlayer(), context);
    }


    @EventHandler
    public void walkOnLocationEvent(PlayerMoveEvent event) {
        if (event.getFrom().getBlock().equals(event.getTo().getBlock())) return;

        String name = Location.isSavedLocation(event.getPlayer().getLocation());
        if (name != null)
            doEvent("walked over " + name, null, event.getPlayer(), null);
    }

    public void serverStartEvent() {
        // Start the 'timeEvent'
        Bukkit.getScheduler().scheduleSyncRepeatingTask(DenizenAPI.getCurrentInstance(),
                new Runnable() {
                    @Override
                    public void run() {
                        timeEvent();
                    }
                }, 250, 250);

        // Fire the 'Server Start' event
        doEvent("server start", null, null, null);
    }

    private Map<String, Integer> current_time = new HashMap<String, Integer>();

    public void timeEvent() {

        for (World world : Bukkit.getWorlds()) {
            int hour = Double.valueOf(world.getTime() / 1000).intValue();
            hour = hour + 6;
            // Get the hour
            if (hour >= 24) hour = hour - 24;

            if (!current_time.containsKey(world.getName())
                    || current_time.get(world.getName()) != hour) {
                doEvent(hour + ":00 in " + world.getName(), null, null, null);
                current_time.put(world.getName(), hour);
            }
        }
    }

    @EventHandler
    public void playerInteract(PlayerInteractEvent event) {

        if (event.getAction() == Action.LEFT_CLICK_AIR) {
            Map<String, String> context = new HashMap<String, String>();

            if (event.getItem() != null ) {
                context.put("item_in_hand", new Item(event.getItem()).dScriptArgValue());

                if (doEvent("player swings " + new Item(event.getItem()).dScriptArgValue() + " in air", null, event.getPlayer(), context))
                    event.setCancelled(true);
                if (doEvent("player swings item in air", null, event.getPlayer(), context))
                    event.setCancelled(true);
            }

            if (doEvent("player swings arm in air", null, event.getPlayer(), context))
                event.setCancelled(true);
        }

        else if (event.getAction() == Action.LEFT_CLICK_BLOCK) {
            Map<String, String> context = new HashMap<String, String>();

            context.put("location clicked", new Location(event.getClickedBlock().getLocation()).dScriptArgValue());
            if (event.getItem() != null ) {
                context.put("item_in_hand", new Item(event.getItem()).dScriptArgValue());

                if (doEvent("player hits block with " + new Item(event.getItem()).dScriptArgValue(), null, event.getPlayer(), context))
                    event.setCancelled(true);
                if (doEvent("player hits block with item", null, event.getPlayer(), context))
                    event.setCancelled(true);
            }
            if (doEvent("player hits block", null, event.getPlayer(), context))
                event.setCancelled(true);
        }

        else if (event.getAction() == Action.PHYSICAL) {
            Map<String, String> context = new HashMap<String, String>();

            context.put("interact location", new Location(event.getClickedBlock().getLocation()).dScriptArgValue());

            if (doEvent("player interacts with block", null, event.getPlayer(), context))
                event.setCancelled(true);
        }

    }



    @EventHandler
    public void playerHit(EntityDamageEvent event) {

        if (event.getEntity() instanceof Player
                && !CitizensAPI.getNPCRegistry().isNPC(event.getEntity())) {
            Map<String, String> context = new HashMap<String, String>();
            context.put("cause", event.getCause().toString());
            if (doEvent("player damaged", null, (Player) event.getEntity(), context))
                event.setCancelled(true);
            if (doEvent("player damaged by " + event.getCause().toString(), null, (Player) event.getEntity(), context))
                event.setCancelled(true);
        }
    }

    @EventHandler
    public void playerHitByEntity(EntityDamageByEntityEvent event) {
 
         if (CitizensAPI.getNPCRegistry().isNPC(event.getEntity())
             && event.getDamager() instanceof Player) {
            Map<String, String> context = new HashMap<String, String>();
            context.put("cause", event.getCause().toString());
            if (doEvent("player damages npc", CitizensAPI.getNPCRegistry.getNPC(event.getEntity()), 
                            (Player) event.getEntity(), context))
                event.setCancelled(true);

         } else if (event.getEntity() instanceof Player) {
            Map<String, String> context = new HashMap<String, String>();
            context.put("cause", event.getCause().toString());
            context.put("damaging entity", event.getDamager().getType().toString());
            if (doEvent("player damaged by entity", null, (Player) event.getEntity(), context))
                event.setCancelled(true);

            if (CitizensAPI.getNPCRegistry().isNPC(event.getDamager())) {
                context.put("damager", String.valueOf(CitizensAPI.getNPCRegistry().getNPC(event.getDamager()).getId()));
                if (doEvent("player damaged by npc", null, (Player) event.getEntity(), context))
                    event.setCancelled(true);

            } else if (event.getDamager() instanceof Player) {
                context.put("damager", ((Player) event.getDamager()).getName());
                if (doEvent("player damaged by player", null, (Player) event.getEntity(), context))
                    event.setCancelled(true);
            } else {
                if (doEvent("player damaged by " + event.getDamager().getType().toString(), null, (Player) event.getEntity(), context))
                    event.setCancelled(true);
            }

        } 
    }
    

    @EventHandler
    public void playerEat(EntityRegainHealthEvent event) {

        if (event.getEntity() instanceof  Player
                && !CitizensAPI.getNPCRegistry().isNPC(event.getEntity())) {
            Map<String, String> context = new HashMap<String, String>();
            context.put("reason", event.getRegainReason().toString());

            if (doEvent("player regains health", null, (Player) event.getEntity(), context))
                event.setCancelled(true);
        }

    }



}
