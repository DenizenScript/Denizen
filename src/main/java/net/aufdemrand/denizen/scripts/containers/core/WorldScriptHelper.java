package net.aufdemrand.denizen.scripts.containers.core;

import net.aufdemrand.denizen.Settings;
import net.aufdemrand.denizen.npc.dNPC;
import net.aufdemrand.denizen.scripts.ScriptBuilder;
import net.aufdemrand.denizen.scripts.ScriptEntry;
import net.aufdemrand.denizen.scripts.ScriptQueue;
import net.aufdemrand.denizen.scripts.commands.core.DetermineCommand;
import net.aufdemrand.denizen.tags.TagManager;
import net.aufdemrand.denizen.utilities.DenizenAPI;
import net.aufdemrand.denizen.utilities.arguments.Item;
import net.aufdemrand.denizen.utilities.arguments.Location;
import net.aufdemrand.denizen.utilities.arguments.aH;
import net.aufdemrand.denizen.utilities.arguments.dList;
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
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.*;

import java.util.Arrays;
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

    public String doEvent(String eventName, dNPC npc, Player player, Map<String, String> context) {

        String determination = "none";

        for (WorldScriptContainer script : world_scripts.values()) {

            if (script == null) continue;
            if (!script.contains("EVENTS.ON " + eventName.toUpperCase())) continue;

            // Fetch script from Event
            List<ScriptEntry> entries = script.getEntries(player, npc, "events.on " + eventName);
            if (entries.isEmpty()) continue;

            dB.report("Event",
                    aH.debugObj("Type", "On " + eventName)
                            + script.getAsScriptArg().debug()
                            + (npc != null ? aH.debugObj("NPC", npc.toString()) : "")
                            + (player != null ? aH.debugObj("Player", player.getName()) : "")
                            + (context != null ? aH.debugObj("Context", context.toString()) : ""));

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

            if (DetermineCommand.hasOutcome(id))
                determination =  DetermineCommand.getOutcome(id);
        }

        return determination;
    }


    @EventHandler
    public void commandEvent(PlayerCommandPreprocessEvent event) {
        Map<String, String> context = new HashMap<String, String>();

        // Well, this is ugly :(
        // Fill tags in any arguments
        dList args = new dList(Arrays.asList(aH.buildArgs(TagManager.tag(event.getPlayer(), null,
                (event.getMessage().split(" ").length > 1 ? event.getMessage().split(" ", 2)[1] : "")))));

        String command = event.getMessage().split(" ")[0].replace("/", "").toUpperCase();

        // Fill context
        context.put("args", args.dScriptArgValue());
        context.put("command", command);
        context.put("raw_args", (event.getMessage().split(" ").length > 1 ? event.getMessage().split(" ", 2)[1] : ""));
        String determination;

        // Run any event scripts and get the determination.
        determination = doEvent(command + " command", null, event.getPlayer(), context).toUpperCase();

        // If a script has determined fulfilled, cancel this event so the player doesn't
        // receive the default 'Invalid command' gibberish from bukkit.
        if (determination.equals("FULFILLED") || determination.equals("CANCELLED"))
            event.setCancelled(true);

        // Run any event scripts and get the determination.
        determination = doEvent("command", null, event.getPlayer(), context).toUpperCase();

        // If a script has determined fulfilled, cancel this event so the player doesn't
        // receive the default 'Invalid command' gibberish from bukkit.
        if (determination.equals("FULFILLED") || determination.equals("CANCELLED"))
            event.setCancelled(true);
    }


    @EventHandler
    public void loginEvent(PlayerLoginEvent event) {
        Map<String, String> context = new HashMap<String, String>();
        context.put("hostname", event.getHostname());

        String determination = doEvent("player login", null, event.getPlayer(), context).toUpperCase();

        // Handle determine kicked
        if (determination.toUpperCase().startsWith("KICKED"))
            event.disallow(PlayerLoginEvent.Result.KICK_OTHER, aH.getStringFrom(determination));
    }

    @EventHandler
    public void loginEvent(PlayerQuitEvent event) {
        Map<String, String> context = new HashMap<String, String>();
        context.put("message", event.getQuitMessage());

        String determination = doEvent("player quit", null, event.getPlayer(), context).toUpperCase();

        // Handle determine message
        if (determination.toUpperCase().startsWith("MESSAGE"))
            event.setQuitMessage(aH.getStringFrom(determination));

    }

    @EventHandler
    public void joinEvent(PlayerJoinEvent event) {
        Map<String, String> context = new HashMap<String, String>();
        context.put("message", event.getJoinMessage());

        String determination = doEvent("player join", null, event.getPlayer(), context);

        // Handle message
        if (determination.toUpperCase().startsWith("MESSAGE"))
            event.setJoinMessage(aH.getStringFrom(determination));
    }

    @EventHandler
    public void walkOnLocationEvent(PlayerMoveEvent event) {
        if (event.getFrom().getBlock().equals(event.getTo().getBlock())) return;

        String name = Location.isSavedLocation(event.getPlayer().getLocation());

        if (name != null) {
            Map<String, String> context = new HashMap<String, String>();
            context.put("notable_name", name);

            String determination;

            determination = doEvent("walked over " + name, null, event.getPlayer(), context);
            if (determination.toUpperCase().startsWith("FROZEN"))
                event.setCancelled(true);

            determination = doEvent("walked over notable location", null, event.getPlayer(), context);
            if (determination.toUpperCase().startsWith("FROZEN"))
                event.setCancelled(true);
        }

    }

    public void serverStartEvent() {
        // Start the 'timeEvent'
        Bukkit.getScheduler().scheduleSyncRepeatingTask(DenizenAPI.getCurrentInstance(),
                new Runnable() {
                    @Override
                    public void run() {
                        timeEvent();
                    }
                }, Settings.WorldScriptTimeEventResolution().getTicks(), Settings.WorldScriptTimeEventResolution().getTicks());

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
                Map<String, String> context = new HashMap<String, String>();
                context.put("time", String.valueOf(hour));
                context.put("world", world.getName());
                doEvent("time change in " + world.getName(), null, null, context);
                doEvent(hour + ":00 in " + world.getName(), null, null, context);
                current_time.put(world.getName(), hour);
            }
        }
    }

    @EventHandler
    public void playerInteract(PlayerInteractEvent event) {

        String determination;

        if (event.getAction() == Action.LEFT_CLICK_AIR) {
            Map<String, String> context = new HashMap<String, String>();

            if (event.getItem() != null ) {
                context.put("item_in_hand", new Item(event.getItem()).dScriptArgValue());

                determination = doEvent("player swings " + new Item(event.getItem()).dScriptArgValue() + " in air", null, event.getPlayer(), context);
                if (determination.toUpperCase().startsWith("CANCELLED"))
                    event.setCancelled(true);

                determination = doEvent("player swings item in air", null, event.getPlayer(), context);
                if (determination.toUpperCase().startsWith("CANCELLED"))
                    event.setCancelled(true);
            }

            determination = doEvent("player swings arm in air", null, event.getPlayer(), context);
            if (determination.toUpperCase().startsWith("CANCELLED"))
                event.setCancelled(true);
        }

        else if (event.getAction() == Action.LEFT_CLICK_BLOCK) {
            Map<String, String> context = new HashMap<String, String>();

            context.put("location clicked", new Location(event.getClickedBlock().getLocation()).dScriptArgValue());
            if (event.getItem() != null ) {
                context.put("item_in_hand", new Item(event.getItem()).dScriptArgValue());

                determination = doEvent("player hits block with " + new Item(event.getItem()).dScriptArgValue(), null, event.getPlayer(), context);
                if (determination.toUpperCase().startsWith("CANCELLED"))
                    event.setCancelled(true);

                determination = doEvent("player hits block with item", null, event.getPlayer(), context);
                if (determination.toUpperCase().startsWith("CANCELLED"))
                    event.setCancelled(true);
            }

            determination = doEvent("player hits block", null, event.getPlayer(), context);

            if (determination.toUpperCase().startsWith("CANCELLED"))
                event.setCancelled(true);
        }

        else if (event.getAction() == Action.PHYSICAL) {
            Map<String, String> context = new HashMap<String, String>();

            context.put("interact location", new Location(event.getClickedBlock().getLocation()).dScriptArgValue());
            determination = doEvent("player interacts with block", null, event.getPlayer(), context);
            if (determination.toUpperCase().startsWith("CANCELLED"))
                event.setCancelled(true);
        }

    }



    @EventHandler
    public void playerHit(EntityDamageEvent event) {

        if (event.getEntity() instanceof Player
                && !CitizensAPI.getNPCRegistry().isNPC(event.getEntity())) {
            Map<String, String> context = new HashMap<String, String>();
            context.put("cause", event.getCause().toString());

            String determination;

            determination = doEvent("player damaged", null, (Player) event.getEntity(), context);

            if (determination.toUpperCase().startsWith("CANCELLED"))
                event.setCancelled(true);
            if (aH.matchesValueArg("DAMAGE", determination, aH.ArgumentType.Integer))
                event.setDamage(aH.getIntegerFrom(determination));

            determination = doEvent("player damaged by " + event.getCause().toString(), null, (Player) event.getEntity(), context);

            if (determination.toUpperCase().startsWith("CANCELLED"))
                event.setCancelled(true);
            if (aH.matchesValueArg("DAMAGE", determination, aH.ArgumentType.Integer))
                event.setDamage(aH.getIntegerFrom(determination));
        }
    }

    @EventHandler
    public void playerHitByEntity(EntityDamageByEntityEvent event) {

        String determination;

        if (CitizensAPI.getNPCRegistry().isNPC(event.getEntity())
                && event.getDamager() instanceof Player) {
            Map<String, String> context = new HashMap<String, String>();
            context.put("cause", event.getCause().toString());

            determination = doEvent("player damages npc",
                    DenizenAPI.getDenizenNPC(CitizensAPI.getNPCRegistry().getNPC(event.getEntity())),
                    (Player) event.getDamager(),
                    context);
            if (determination.toUpperCase().startsWith("CANCELLED"))
                event.setCancelled(true);
            if (aH.matchesValueArg("DAMAGE", determination, aH.ArgumentType.Integer))
                event.setDamage(aH.getIntegerFrom(determination));
        }

        if (event.getEntity() instanceof Player) {
            Map<String, String> context = new HashMap<String, String>();
            context.put("cause", event.getCause().toString());
            context.put("damaging entity", event.getDamager().getType().toString());

            determination = doEvent("player damaged by entity", null, (Player) event.getEntity(), context);
            if (determination.toUpperCase().startsWith("CANCELLED"))
                event.setCancelled(true);
            if (aH.matchesValueArg("DAMAGE", determination, aH.ArgumentType.Integer))
                event.setDamage(aH.getIntegerFrom(determination));

            if (CitizensAPI.getNPCRegistry().isNPC(event.getDamager())) {
                context.put("damager", String.valueOf(CitizensAPI.getNPCRegistry().getNPC(event.getDamager()).getId()));

                determination = doEvent("player damaged by npc", null, (Player) event.getEntity(), context);
                if (determination.toUpperCase().startsWith("CANCELLED"))
                    event.setCancelled(true);
                if (aH.matchesValueArg("DAMAGE", determination, aH.ArgumentType.Integer))
                    event.setDamage(aH.getIntegerFrom(determination));

            } else if (event.getDamager() instanceof Player) {
                context.put("damager", ((Player) event.getDamager()).getName());
                determination = doEvent("player damaged by player", null, (Player) event.getEntity(), context);

                if (determination.toUpperCase().startsWith("CANCELLED"))
                    event.setCancelled(true);
                if (aH.matchesValueArg("DAMAGE", determination, aH.ArgumentType.Integer))
                    event.setDamage(aH.getIntegerFrom(determination));

            } else {
                determination = doEvent("player damaged by " + event.getDamager().getType().toString(), null, (Player) event.getEntity(), context);

                if (determination.toUpperCase().startsWith("CANCELLED"))
                    event.setCancelled(true);
                if (aH.matchesValueArg("DAMAGE", determination, aH.ArgumentType.Integer))
                    event.setDamage(aH.getIntegerFrom(determination));
            }

        }
    }


    @EventHandler
    public void playerEat(EntityRegainHealthEvent event) {

        if (event.getEntity() instanceof  Player
                && !CitizensAPI.getNPCRegistry().isNPC(event.getEntity())) {
            Map<String, String> context = new HashMap<String, String>();
            context.put("reason", event.getRegainReason().toString());
            context.put("amount", String.valueOf(event.getAmount()));

            String determination = doEvent("player regains health", null, (Player) event.getEntity(), context);

            if (determination.toUpperCase().startsWith("CANCELLED"))
                event.setCancelled(true);
            if (aH.matchesValueArg("AMOUNT", determination, aH.ArgumentType.Integer))
                event.setAmount(aH.getIntegerFrom(determination));

        }

    }


    @EventHandler
    public void playerDeath(PlayerDeathEvent event) {
        Map<String, String> context = new HashMap<String, String>();
        context.put("message", event.getDeathMessage());

        String determination = doEvent("player death", null, event.getEntity(), context);

        // Handle message
        if (determination.toUpperCase().startsWith("MESSAGE"))
            event.setDeathMessage(aH.getStringFrom(determination));
    }


}
