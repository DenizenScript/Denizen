package net.aufdemrand.denizen.scripts.containers.core;

import net.aufdemrand.denizen.Settings;
import net.aufdemrand.denizen.objects.*;
import net.aufdemrand.denizen.scripts.ScriptBuilder;
import net.aufdemrand.denizen.scripts.ScriptEntry;
import net.aufdemrand.denizen.scripts.ScriptQueue;
import net.aufdemrand.denizen.scripts.commands.core.DetermineCommand;
import net.aufdemrand.denizen.tags.TagManager;
import net.aufdemrand.denizen.utilities.DenizenAPI;
import net.aufdemrand.denizen.utilities.debugging.dB;
import net.citizensnpcs.api.CitizensAPI;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.BlockRedstoneEvent;
import org.bukkit.event.entity.EntityCombustEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.entity.EntityTeleportEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.*;
import org.bukkit.event.server.ServerCommandEvent;
import org.bukkit.event.weather.WeatherChangeEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;

public class WorldScriptHelper implements Listener {

    public static Map<String, WorldScriptContainer> world_scripts = new ConcurrentHashMap<String, WorldScriptContainer>(8, 0.9f, 1);

    public WorldScriptHelper() {
        DenizenAPI.getCurrentInstance().getServer().getPluginManager()
                .registerEvents(this, DenizenAPI.getCurrentInstance());
    }

    public String doEvent(String eventName, dNPC npc, Player player, Map<String, Object> context) {

        String determination = "none";

        for (WorldScriptContainer script : world_scripts.values()) {

            if (script == null) continue;
            if (!script.contains("EVENTS.ON " + eventName.toUpperCase())) continue;

            // Fetch script from Event
            List<ScriptEntry> entries = script.getEntries(new dPlayer(player), npc, "events.on " + eventName);
            if (entries.isEmpty()) continue;

            dB.report("Event",
                    aH.debugObj("Type", "On " + eventName)
                            + script.getAsScriptArg().debug()
                            + (npc != null ? aH.debugObj("NPC", npc.toString()) : "")
                            + (player != null ? aH.debugObj("Player", player.getName()) : "")
                            + (context != null ? aH.debugObj("Context", context.toString()) : ""));

            dB.echoDebug(dB.DebugElement.Header, "Building event 'On " + eventName.toUpperCase() + "' for " + script.getName());

            if (context != null) {
                for (Map.Entry<String, Object> entry : context.entrySet()) {
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
    
    
    /////////////////////
    //   CUSTOM EVENTS
    /////////////////
    
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
                Map<String, Object> context = new HashMap<String, Object>();
                context.put("time", String.valueOf(hour));
                context.put("world", world.getName());
                doEvent("time change in " + world.getName(), null, null, context);
                doEvent(hour + ":00 in " + world.getName(), null, null, context);
                current_time.put(world.getName(), hour);
            }
        }
    }
    
    
    /////////////////////
    //   SERVER EVENTS
    /////////////////

    @EventHandler
    public void serverCommandEvent(ServerCommandEvent event) {
        Map<String, Object> context = new HashMap<String, Object>();
        
        // Well, this is ugly :(
        // Fill tags in any arguments

        List<String> args = Arrays.asList(
                aH.buildArgs(
                        TagManager.tag(null, null,
                                (event.getCommand().split(" ").length > 1 ? event.getCommand().split(" ", 2)[1] : ""))));

        dList args_list = new dList(args);

        String command = event.getCommand().split(" ")[0].replace("/", "").toUpperCase();

        // Fill context
        context.put("args", args_list);
        context.put("command", command);
        context.put("raw_args", (event.getCommand().split(" ").length > 1 ? event.getCommand().split(" ", 2)[1] : ""));

        doEvent(command + " command", null, null, context).toUpperCase();

        doEvent("command", null, null, context).toUpperCase();
    }
    
    
    /////////////////////
    //   PLAYER EVENTS
    /////////////////

    @EventHandler(priority = EventPriority.LOWEST)
    public void playerChat(final AsyncPlayerChatEvent event) {
        final Map<String, Object> context = new HashMap<String, Object>();
        context.put("message", event.getMessage());

        Callable<String> call = new Callable<String>() {
            public String call() {
                return doEvent("player chats", null, event.getPlayer(), context);
            }
        };
        String determination = null;
        try {
            determination = event.isAsynchronous() ? Bukkit.getScheduler().callSyncMethod(DenizenAPI.getCurrentInstance(), call).get() : call.call();
        } catch (InterruptedException e) {
        	// TODO: Need to find a way to fix this eventually
            // e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (determination == null)
            return;
        if (determination.toUpperCase().startsWith("CANCELLED"))
            event.setCancelled(true);
        if (determination.toUpperCase().startsWith("MESSAGE"))
            event.setMessage(aH.getStringFrom(determination));
    }
    
    @EventHandler
    public void bedEnterEvent(PlayerBedEnterEvent event) {
        Map<String, Object> context = new HashMap<String, Object>();
        context.put("location", new dLocation(event.getBed().getLocation()));

        String determination = doEvent("player enters bed", null, event.getPlayer(), context);

        if (determination.toUpperCase().startsWith("CANCELLED"))
            event.setCancelled(true);
    }
    
    @EventHandler
    public void bedLeaveEvent(PlayerBedLeaveEvent event) {
        Map<String, Object> context = new HashMap<String, Object>();
        context.put("location", new dLocation(event.getBed().getLocation()));

        doEvent("player leaves bed", null, event.getPlayer(), context);
    }
    
    @EventHandler
    public void playerBucketEmpty(PlayerBucketEmptyEvent event) {
        Map<String, Object> context = new HashMap<String, Object>();
        context.put("bucket_type", event.getBucket().name());
        context.put("bucket", new dItem(event.getItemStack()));
        context.put("clicked_location", new dLocation(event.getBlockClicked().getLocation()));

        String determination = doEvent("empty bucket", null, event.getPlayer(), context);

        // Handle message
        if (determination.toUpperCase().startsWith("CANCELLED"))
            event.setCancelled(true);
        if (determination.toUpperCase().startsWith("ITEM_IN_HAND")) {
            ItemStack is = dItem.valueOf(aH.getStringFrom(determination)).getItemStack();
            event.setItemStack( is != null ? is : new ItemStack(Material.AIR));
        }

    }

    @EventHandler
    public void playerBucketFill(PlayerBucketFillEvent event) {
        Map<String, Object> context = new HashMap<String, Object>();
        context.put("bucket_type", event.getBucket().name());
        context.put("bucket", new dItem(event.getItemStack()));
        context.put("clicked_location", new dLocation(event.getBlockClicked().getLocation()));

        String determination = doEvent("fill bucket", null, event.getPlayer(), context);

        // Handle message
        if (determination.toUpperCase().startsWith("CANCELLED"))
            event.setCancelled(true);
        if (determination.toUpperCase().startsWith("ITEM_IN_HAND")) {
            ItemStack is = dItem.valueOf(aH.getStringFrom(determination)).getItemStack();
            event.setItemStack( is != null ? is : new ItemStack(Material.AIR));
        }
    }
    
    @EventHandler
    public void playerCommandEvent(PlayerCommandPreprocessEvent event) {
        Map<String, Object> context = new HashMap<String, Object>();

        // Well, this is ugly :(
        // Fill tags in any arguments

        dPlayer player = dPlayer.valueOf(event.getPlayer().getName());

        List<String> args = Arrays.asList(
                aH.buildArgs(
                        TagManager.tag(player, null,
                                (event.getMessage().split(" ").length > 1 ? event.getMessage().split(" ", 2)[1] : ""))));

        dList args_list = new dList(args);

        String command = event.getMessage().split(" ")[0].replace("/", "").toUpperCase();

        // Fill context
        context.put("args", args_list);
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
    public void playerDeath(PlayerDeathEvent event) {
        Map<String, Object> context = new HashMap<String, Object>();
        context.put("message", event.getDeathMessage());

        String determination = doEvent("player death", null, event.getEntity(), context);

        // Handle message
        if (determination.toUpperCase().startsWith("MESSAGE"))
            event.setDeathMessage(aH.getStringFrom(determination));
    }
    
    @EventHandler
    public void playerInteract(PlayerInteractEvent event) {

        String determination;
        Map<String, Object> context = new HashMap<String, Object>();

        if (event.getItem() != null ) {
            context.put("item_in_hand", new dItem(event.getItem()));

            // TODO: Test this

            determination = doEvent("player swings " + new dItem(event.getItem()).identify().split(":")[0], null, event.getPlayer(), context);
            if (determination.toUpperCase().startsWith("CANCELLED"))
                event.setCancelled(true);

            determination = doEvent("player swings item", null, event.getPlayer(), context);
            if (determination.toUpperCase().startsWith("CANCELLED"))
                event.setCancelled(true);
        }

        determination = doEvent("player swings arm", null, event.getPlayer(), context);
        if (determination.toUpperCase().startsWith("CANCELLED"))
            event.setCancelled(true);


        if (event.getAction() == Action.LEFT_CLICK_AIR) {
            if (event.getItem() != null ) {

                determination = doEvent("player swings "
                        + new dItem(event.getItem()).identify().split(":")[0]
                        + " in air", null, event.getPlayer(), context);
                if (determination.toUpperCase().startsWith("CANCELLED"))
                    event.setCancelled(true);

                determination = doEvent("player swings item in air", null, event.getPlayer(), context);
                if (determination.toUpperCase().startsWith("CANCELLED"))
                    event.setCancelled(true);
            }
            
            determination = doEvent("player left clicks", null, event.getPlayer(), context);
            if (determination.toUpperCase().startsWith("CANCELLED"))
                event.setCancelled(true);

            determination = doEvent("player swings arm in air", null, event.getPlayer(), context);
            if (determination.toUpperCase().startsWith("CANCELLED"))
                event.setCancelled(true);
        }

        else if (event.getAction() == Action.LEFT_CLICK_BLOCK) {
            context.put("location clicked", new dLocation(event.getClickedBlock().getLocation()));
            if (event.getItem() != null ) {

                determination = doEvent("player hits block with "
                        + new dItem(event.getItem()).identify().split(":")[0], null, event.getPlayer(), context);
                if (determination.toUpperCase().startsWith("CANCELLED"))
                    event.setCancelled(true);

                determination = doEvent("player hits block with item", null, event.getPlayer(), context);
                if (determination.toUpperCase().startsWith("CANCELLED"))
                    event.setCancelled(true);
            }
            
            determination = doEvent("player left clicks", null, event.getPlayer(), context);
            if (determination.toUpperCase().startsWith("CANCELLED"))
                event.setCancelled(true);

            determination = doEvent("player hits block", null, event.getPlayer(), context);

            if (determination.toUpperCase().startsWith("CANCELLED"))
                event.setCancelled(true);
        }
        
        if (event.getAction() == Action.RIGHT_CLICK_AIR) {

            if (event.getItem() != null ) {
                context.put("item_in_hand", new dItem(event.getItem()).identify().split(":")[0]);

                determination = doEvent("player uses " + new dItem(event.getItem()).identify().split(":")[0] + " in air", null, event.getPlayer(), context);
                if (determination.toUpperCase().startsWith("CANCELLED"))
                    event.setCancelled(true);

                determination = doEvent("player uses item in air", null, event.getPlayer(), context);
                if (determination.toUpperCase().startsWith("CANCELLED"))
                    event.setCancelled(true);
            }

            determination = doEvent("player right clicks", null, event.getPlayer(), context);
            if (determination.toUpperCase().startsWith("CANCELLED"))
                event.setCancelled(true);

            determination = doEvent("player right clicks air", null, event.getPlayer(), context);
            if (determination.toUpperCase().startsWith("CANCELLED"))
                event.setCancelled(true);
        }

        else if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {

            context.put("location clicked", new dLocation(event.getClickedBlock().getLocation()).identify());
            if (event.getItem() != null ) {
                context.put("item_in_hand", new dItem(event.getItem()).identify());

                determination = doEvent("player uses " + new dItem(event.getItem()).identify() + " on block", null, event.getPlayer(), context);
                if (determination.toUpperCase().startsWith("CANCELLED"))
                    event.setCancelled(true);

                determination = doEvent("player uses item on block", null, event.getPlayer(), context);
                if (determination.toUpperCase().startsWith("CANCELLED"))
                    event.setCancelled(true);
            }

            determination = doEvent("player right clicks", null, event.getPlayer(), context);

            if (determination.toUpperCase().startsWith("CANCELLED"))
                event.setCancelled(true);

            determination = doEvent("player right clicks block", null, event.getPlayer(), context);

            if (determination.toUpperCase().startsWith("CANCELLED"))
                event.setCancelled(true);
        }

        else if (event.getAction() == Action.PHYSICAL) {
            context.put("interact location", new dLocation(event.getClickedBlock().getLocation()));
            determination = doEvent("player interacts with block", null, event.getPlayer(), context);
            if (determination.toUpperCase().startsWith("CANCELLED"))
                event.setCancelled(true);
        }

    }
    
    @EventHandler
    public void playerInteractEntity(PlayerInteractEntityEvent event) {

    	Entity entity = event.getRightClicked();
    	
        String determination;
        Map<String, Object> context = new HashMap<String, Object>();
        context.put("location", new dLocation(event.getRightClicked().getLocation()));
        context.put("entity", new dEntity(entity));

        determination = doEvent("player right clicks " + entity.getType().name(), null, event.getPlayer(), context);
        
        if (determination.toUpperCase().startsWith("CANCELLED"))
        	event.setCancelled(true);
        
        if (event.getRightClicked() instanceof Player) {
        	
        	context.put("target", dPlayer.valueOf(((Player) entity).getName()));
        	
        	determination = doEvent("player right clicks " + ((Player) entity).getName(), null, event.getPlayer(), context);
        	
            if (determination.toUpperCase().startsWith("CANCELLED"))
            	event.setCancelled(true);
        }
    }
    
    @EventHandler
    public void playerItemConsume(PlayerItemConsumeEvent event) {
        
        ItemStack item = event.getItem(); 
    	
        String determination;
    	Map<String, Object> context = new HashMap<String, Object>();
        context.put("item", new dItem(item));
        
        String id = String.valueOf(item.getTypeId());
        String material = item.getType().name();
        String data = String.valueOf(item.getData().getData());
        String display = String.valueOf(item.getItemMeta().getDisplayName());
        
        determination = doEvent("player consumes item", null, event.getPlayer(), context);
        
        if (determination.toUpperCase().startsWith("CANCELLED"))
            event.setCancelled(true);
        
        determination = doEvent("player consumes " + id, null, event.getPlayer(), context);
        
        if (determination.toUpperCase().startsWith("CANCELLED"))
            event.setCancelled(true);
        
        determination = doEvent("player consumes " + id + ":" + data, null, event.getPlayer(), context);
        
        if (determination.toUpperCase().startsWith("CANCELLED"))
            event.setCancelled(true);
        
        determination = doEvent("player consumes " + material, null, event.getPlayer(), context);
        
        if (determination.toUpperCase().startsWith("CANCELLED"))
            event.setCancelled(true);
        
        determination = doEvent("player consumes " + material + ":" + data, null, event.getPlayer(), context);
        
        if (determination.toUpperCase().startsWith("CANCELLED"))
            event.setCancelled(true);
        
        if (display != null) {
        	
            determination = doEvent("player consumes " + display, null, event.getPlayer(), context);
            if (determination.toUpperCase().startsWith("CANCELLED"))
                event.setCancelled(true);
            
            determination = doEvent("player consumes " + id + " " + display, null, event.getPlayer(), context);
            if (determination.toUpperCase().startsWith("CANCELLED"))
                event.setCancelled(true);
            
            determination = doEvent("player consumes " + material + " " + display, null, event.getPlayer(), context);
            if (determination.toUpperCase().startsWith("CANCELLED"))
                event.setCancelled(true);
        }
    }
    
    @EventHandler
    public void joinEvent(PlayerJoinEvent event) {
        Map<String, Object> context = new HashMap<String, Object>();
        context.put("message", event.getJoinMessage());

        String determination = doEvent("player join", null, event.getPlayer(), context);

        // Handle message
        if (determination.toUpperCase().startsWith("MESSAGE"))
            event.setJoinMessage(aH.getStringFrom(determination));
    }
    
    @EventHandler
    public void levelChangeEvent(PlayerLevelChangeEvent event) {
        Map<String, Object> context = new HashMap<String, Object>();
        context.put("level", event.getNewLevel());

        doEvent("player levels up", null, event.getPlayer(), context);
        doEvent("player levels up to " + event.getNewLevel(), null, event.getPlayer(), context);        
        doEvent("player levels up from " + event.getOldLevel(), null, event.getPlayer(), context);
    }
    
    @EventHandler
    public void loginEvent(PlayerLoginEvent event) {
        Map<String, Object> context = new HashMap<String, Object>();
        context.put("hostname", event.getHostname());

        String determination = doEvent("player login", null, event.getPlayer(), context).toUpperCase();

        // Handle determine kicked
        if (determination.toUpperCase().startsWith("KICKED"))
            event.disallow(PlayerLoginEvent.Result.KICK_OTHER, aH.getStringFrom(determination));
    }
    
    @EventHandler
    public void playerMoveEvent(PlayerMoveEvent event) {
        if (event.getFrom().getBlock().equals(event.getTo().getBlock())) return;

        String name = dLocation.getSaved(event.getPlayer().getLocation());

        if (name != null) {
            Map<String, Object> context = new HashMap<String, Object>();
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

    @EventHandler
    public void quitEvent(PlayerQuitEvent event) {
        Map<String, Object> context = new HashMap<String, Object>();
        context.put("message", event.getQuitMessage());

        String determination = doEvent("player quit", null, event.getPlayer(), context).toUpperCase();

        // Handle determine message
        if (determination.toUpperCase().startsWith("MESSAGE"))
            event.setQuitMessage(aH.getStringFrom(determination));
    }

    @EventHandler
    public void respawnEvent(PlayerRespawnEvent event) {
        Map<String, Object> context = new HashMap<String, Object>();
        context.put("location", new dLocation(event.getRespawnLocation()));

        doEvent("player respawns", null, event.getPlayer(), context);
        
        if (event.isBedSpawn()) {
        	doEvent("player respawns at bed", null, event.getPlayer(), context);
        }
        else {
        	doEvent("player respawns elsewhere", null, event.getPlayer(), context);
        }
    }
    
    
    /////////////////////
    //   BLOCK EVENTS
    /////////////////
    
    @EventHandler
    public void blockBreak(BlockBreakEvent event) {

        String determination;
        Map<String, Object> context = new HashMap<String, Object>();
        
        context.put("location", new dLocation(event.getBlock().getLocation()));
        context.put("type", event.getBlock().getType().name());

        determination = doEvent("player breaks block", null, event.getPlayer(), context);

        if (determination.toUpperCase().startsWith("CANCELLED"))
        	event.setCancelled(true);

        determination = doEvent("player breaks " + event.getBlock().getType().name(), null, event.getPlayer(), context);
        
        if (determination.toUpperCase().startsWith("CANCELLED"))
        	event.setCancelled(true);
        
        determination = doEvent("player breaks " + event.getBlock().getType().name() + " with " + new dItem(event.getPlayer().getItemInHand()).identify().split(":")[0], null, event.getPlayer(), context);
        
        if (determination.toUpperCase().startsWith("CANCELLED"))
        	event.setCancelled(true);
    }
    
    
    @EventHandler
    public void blockIgnite(BlockIgniteEvent event) {

        String determination;
        Map<String, Object> context = new HashMap<String, Object>();
        
        context.put("location", new dLocation(event.getBlock().getLocation()));
        context.put("type", event.getBlock().getType().name());
        
        determination = doEvent("block ignites", null, event.getPlayer(), context);
        
        if (determination.toUpperCase().startsWith("CANCELLED"))
        	event.setCancelled(true);

        determination = doEvent(event.getBlock().getType().name() + " ignites", null, event.getPlayer(), context);
        
        if (determination.toUpperCase().startsWith("CANCELLED"))
        	event.setCancelled(true);
    }
    
    @EventHandler
    public void blockPlace(BlockPlaceEvent event) {

        String determination;
        Map<String, Object> context = new HashMap<String, Object>();
        
        context.put("location", new dLocation(event.getBlock().getLocation()));
        context.put("type", event.getBlock().getType().name());

        determination = doEvent("player places block", null, event.getPlayer(), context);

        if (determination.toUpperCase().startsWith("CANCELLED"))
        	event.setCancelled(true);

        determination = doEvent("player places " + event.getBlock().getType().name(), null, event.getPlayer(), context);
        
        if (determination.toUpperCase().startsWith("CANCELLED"))
        	event.setCancelled(true);
    }
    
    
    @EventHandler
    public void blockRedstone(BlockRedstoneEvent event) {

        Map<String, Object> context = new HashMap<String, Object>();
        
        context.put("location", new dLocation(event.getBlock().getLocation()));
        context.put("type", event.getBlock().getType().name());
        
        if (event.getNewCurrent() > 0) {
            
        	doEvent("block powered", null, null, context);
            doEvent("block " + event.getBlock().getType().name() + " powered", null, null, context);
        }
        else {
        	
        	doEvent("block unpowered", null, null, context);
            doEvent("block " + event.getBlock().getType().name() + " unpowered", null, null, context);
        }
    }
    

    /////////////////////
    //   ENTITY EVENTS
    /////////////////
    
    @EventHandler
    public void entityCombust(EntityCombustEvent event) {

        Map<String, Object> context = new HashMap<String, Object>();
        Entity entity = event.getEntity();
        
        context.put("entity", new dEntity(entity));
        context.put("duration", event.getDuration());
        
        String determination;

        determination = doEvent(entity.getType().name() + " combusts", null, null, context);

        if (determination.toUpperCase().startsWith("CANCELLED"))
        	event.setCancelled(true);
    }
    
    @EventHandler
    public void entityDamage(EntityDamageEvent event) {

        if (event.getEntity() instanceof Player
                && !CitizensAPI.getNPCRegistry().isNPC(event.getEntity())) {
            Map<String, Object> context = new HashMap<String, Object>();
            context.put("cause", event.getCause().toString());
            context.put("amount", String.valueOf(event.getDamage()));

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
    public void entityDamageByEntity(EntityDamageByEntityEvent event) {

        String determination;

        if (CitizensAPI.getNPCRegistry().isNPC(event.getEntity())
                && event.getDamager() instanceof Player) {
            Map<String, Object> context = new HashMap<String, Object>();
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
            Map<String, Object> context = new HashMap<String, Object>();
            context.put("cause", event.getCause().toString());
            context.put("damaging entity", event.getDamager().getType().toString());

            determination = doEvent("player damaged by entity", null, (Player) event.getEntity(), context);
            if (determination.toUpperCase().startsWith("CANCELLED"))
                event.setCancelled(true);
            if (aH.matchesValueArg("DAMAGE", determination, aH.ArgumentType.Integer))
                event.setDamage(aH.getIntegerFrom(determination));

            if (CitizensAPI.getNPCRegistry().isNPC(event.getDamager())) {
                dNPC npc = DenizenAPI.getDenizenNPC(CitizensAPI.getNPCRegistry().getNPC(event.getDamager()));
                context.put("damager", String.valueOf(npc.getId()));

                determination = doEvent("player damaged by npc", npc, (Player) event.getEntity(), context);
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
    public void entityExplode(EntityExplodeEvent event) {

        Map<String, Object> context = new HashMap<String, Object>();
        Entity entity = event.getEntity();
        
        context.put("entity", new dEntity(entity));
        context.put("location", new dLocation(event.getLocation()));
        
        String determination;

        determination = doEvent(entity.getType().name() + " explodes", null, null, context);

        if (determination.toUpperCase().startsWith("CANCELLED"))
        	event.setCancelled(true);
    }
    
    @EventHandler
    public void entityRegainHealth(EntityRegainHealthEvent event) {

        if (event.getEntity() instanceof  Player
                && !CitizensAPI.getNPCRegistry().isNPC(event.getEntity())) {
            Map<String, Object> context = new HashMap<String, Object>();
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
    public void entityTarget(EntityTargetEvent event) {

        Map<String, Object> context = new HashMap<String, Object>();
        Entity entity = event.getEntity();
        Entity target = event.getTarget();
        
        context.put("reason", event.getReason().name());
        context.put("entity", new dEntity(entity));
        
        if (event.getTarget() instanceof Player) {
        	context.put("target", new dPlayer((Player) target));        	
        }
        else {
        	context.put("entity", new dEntity(target));
        }

        String determination;

        determination = doEvent(entity.getType().name() + " targets " + target.getType().name(), null, null, context);

        if (determination.toUpperCase().startsWith("CANCELLED"))
        	event.setCancelled(true);

        determination = doEvent(entity.getType().name() + " targets " + target.getType().name() + " because " + event.getReason().name(), null, null, context);

        if (determination.toUpperCase().startsWith("CANCELLED"))
        	event.setCancelled(true);
    }
    
    @EventHandler
    public void entityTeleport(EntityTeleportEvent event) {

        Map<String, Object> context = new HashMap<String, Object>();
        Entity entity = event.getEntity();
        
        context.put("entity", new dEntity(entity));
        context.put("origin", new dLocation(event.getFrom()));
        context.put("destination", new dLocation(event.getTo()));
        
        String determination = doEvent(entity.getType().name() + " teleports", null, null, context);

        if (determination.toUpperCase().startsWith("CANCELLED"))
        	event.setCancelled(true);
    }
    

    /////////////////////
    //   WEATHER EVENTS
    /////////////////
    
    @EventHandler
    public void weatherChange(WeatherChangeEvent event) {
        Map<String, Object> context = new HashMap<String, Object>();
        
        String world = event.getWorld().getName();
        
        context.put("world", new dWorld(event.getWorld()));

        String determination = doEvent("weather changes", null, null, context);

        // Handle messages
        if (determination.toUpperCase().startsWith("CANCELLED"))
            event.setCancelled(true);
        
        determination = doEvent("weather changes in " + world, null, null, context);
        
        if (determination.toUpperCase().startsWith("CANCELLED"))
            event.setCancelled(true);
        
        if (event.toWeatherState() == true) {

            determination = doEvent("weather rains", null, null, context);

            // Handle messages
            if (determination.toUpperCase().startsWith("CANCELLED"))
                event.setCancelled(true);
        	
        	determination = doEvent("weather rains in " + world, null, null, context);
        
        	if (determination.toUpperCase().startsWith("CANCELLED"))
        		event.setCancelled(true);
        }
        else {
        	
        	determination = doEvent("weather clears", null, null, context);

            // Handle messages
            if (determination.toUpperCase().startsWith("CANCELLED"))
                event.setCancelled(true);
        
        	determination = doEvent("weather clears in " + world, null, null, context);
        
        	if (determination.toUpperCase().startsWith("CANCELLED"))
        		event.setCancelled(true);
        }
    }


}
