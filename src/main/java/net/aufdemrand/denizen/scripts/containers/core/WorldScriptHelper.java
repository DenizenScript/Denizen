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
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.LivingEntity;
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

import java.util.ArrayList;
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

    public String doEvents(List<String> eventNames, dNPC npc, Player player, Map<String, Object> context) {

        String determination = "none";

        for (WorldScriptContainer script : world_scripts.values()) {

            if (script == null) continue;
            
            for (String eventName : eventNames) {
            
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
        doEvents(Arrays.asList("server start"),
        		null, null, null);
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
                
                doEvents(Arrays.asList("time change in " + world.getName(),
                					   hour + ":00 in " + world.getName()),
                		null, null, context);
                
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

        doEvents(Arrays.asList("command",
        					   command + " command"),
        		null, null, context);
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
                return doEvents(Arrays.asList("player chats"),
                			null, event.getPlayer(), context);
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

        String determination = doEvents(Arrays.asList("player enters bed"),
        		null, event.getPlayer(), context);

        if (determination.toUpperCase().startsWith("CANCELLED"))
            event.setCancelled(true);
    }
    
    @EventHandler
    public void bedLeaveEvent(PlayerBedLeaveEvent event) {
        Map<String, Object> context = new HashMap<String, Object>();
        context.put("location", new dLocation(event.getBed().getLocation()));

        doEvents(Arrays.asList("player leaves bed"),
        		null, event.getPlayer(), context);
    }
    
    @EventHandler
    public void playerBucketEmpty(PlayerBucketEmptyEvent event) {
    	
        Map<String, Object> context = new HashMap<String, Object>();
        context.put("item", new dItem(event.getBucket()));
        context.put("location", new dLocation(event.getBlockClicked().getLocation()));

        String determination = doEvents(Arrays.asList("player empties bucket"),
        		null, event.getPlayer(), context);

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
        context.put("item", new dItem(event.getBucket()));
        context.put("location", new dLocation(event.getBlockClicked().getLocation()));

        String determination = doEvents(Arrays.asList("player fills bucket"),
        		null, event.getPlayer(), context);

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
        determination = doEvents(Arrays.asList
        		("command",
        		 command + " command"), null, event.getPlayer(), context).toUpperCase();

        // If a script has determined fulfilled, cancel this event so the player doesn't
        // receive the default 'Invalid command' gibberish from bukkit.
        if (determination.equals("FULFILLED") || determination.equals("CANCELLED"))
            event.setCancelled(true);
    }
    
    @EventHandler
    public void playerDeath(PlayerDeathEvent event) {

    	Map<String, Object> context = new HashMap<String, Object>();
        context.put("message", event.getDeathMessage());
        
        String determination = doEvents(Arrays.asList
        		("player death"),
        		null, event.getEntity(), context);

        // Handle message
        if (determination.toUpperCase().startsWith("MESSAGE"))
            event.setDeathMessage(aH.getStringFrom(determination));
    }
    
    @EventHandler
    public void playerInteract(PlayerInteractEvent event) {

        Map<String, Object> context = new HashMap<String, Object>();
        Action action = event.getAction();
        dItem item = null;
        
        List<String> events = new ArrayList<String>();
        
        String interaction;
        
        if (action == Action.LEFT_CLICK_AIR || action == Action.LEFT_CLICK_BLOCK)
        	interaction = "player left clicks";
        else if (action == Action.RIGHT_CLICK_AIR || action == Action.RIGHT_CLICK_BLOCK)
        	interaction = "player right clicks";
        // The only other action is PHYSICAL, which is triggered when a player
        // stands on a pressure plate
        else interaction = "player stands";

        events.add(interaction);
        
        if (event.hasItem()) {
        	item = new dItem(event.getItem());
        	context.put("item", item);
        	
        	events.add(interaction + " with item");
        	events.add(interaction + " with " + item.identify().split(":")[0]);
        	events.add(interaction + " with " + item.identify());
        }
        
        if (event.hasBlock()) {
            Block block = event.getClickedBlock();
            context.put("location", new dLocation(block.getLocation()));
            
            interaction = interaction + " on " + block.getType().name(); 
        	events.add(interaction);
        	
        	if (event.hasItem()) {
            	events.add(interaction + " with item");
            	events.add(interaction + " with " + item.identify().split(":")[0]);
            	events.add(interaction + " with " + item.identify());
            }
        }
        
        String determination = doEvents(events, null, event.getPlayer(), context);
        
        if (determination.toUpperCase().startsWith("CANCELLED"))
            event.setCancelled(true);
    }
    
    @EventHandler
    public void playerInteractEntity(PlayerInteractEntityEvent event) {

    	Entity entity = event.getRightClicked();
    	
        String determination;
        Map<String, Object> context = new HashMap<String, Object>();
        context.put("location", new dLocation(event.getRightClicked().getLocation()));

        if (entity instanceof Player) {
        	context.put("entity", new dPlayer((Player) entity));
        }
        else {
            context.put("entity", new dEntity(entity));
        }
        
        List<String> events = new ArrayList<String>();
        events.add("player right clicks " + entity.getType().name());
        
        if (entity instanceof ItemFrame) {
        	events.add("player right clicks " + entity.getType().name() + " " +
        			new dItem(((ItemFrame) entity).getItem()).identify().split(":")[0]);
        }
        
        determination = doEvents(events, null, event.getPlayer(), context);
        
        if (determination.toUpperCase().startsWith("CANCELLED"))
        	event.setCancelled(true);
    }
    
    @EventHandler
    public void playerItemConsume(PlayerItemConsumeEvent event) {
        
        dItem item = new dItem(event.getItem()); 
    	
    	Map<String, Object> context = new HashMap<String, Object>();
        context.put("item", item);
        
        List<String> events = new ArrayList<String>();
        events.add("player consumes " + item.identify().split(":")[0]);
        events.add("player consumes " + item.identify());
        
        String determination = doEvents(events, null, event.getPlayer(), context);
        
        if (determination.toUpperCase().startsWith("CANCELLED"))
            event.setCancelled(true);
    }
    
    @EventHandler
    public void joinEvent(PlayerJoinEvent event) {
        Map<String, Object> context = new HashMap<String, Object>();
        context.put("message", event.getJoinMessage());

        String determination = doEvents(Arrays.asList
        		("player join"),
        		null, event.getPlayer(), context);

        // Handle message
        if (determination.toUpperCase().startsWith("MESSAGE"))
            event.setJoinMessage(aH.getStringFrom(determination));
    }
    
    @EventHandler
    public void levelChangeEvent(PlayerLevelChangeEvent event) {
    	
        Map<String, Object> context = new HashMap<String, Object>();
        context.put("level", event.getNewLevel());

        doEvents(Arrays.asList
        		("player levels up",
        		 "player levels up to " + event.getNewLevel(),
        		 "player levels up from " + event.getOldLevel()),
        		null, event.getPlayer(), context);
    }
    
    @EventHandler
    public void loginEvent(PlayerLoginEvent event) {
    	
        Map<String, Object> context = new HashMap<String, Object>();
        context.put("hostname", event.getHostname());

        String determination = doEvents(Arrays.asList
        		("player logs in"),
        		null, event.getPlayer(), context).toUpperCase();

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

            String determination = doEvents(Arrays.asList
            		("player walks over notable",
            		 "player walks over " + name),
            		null, event.getPlayer(), context);
            
            if (determination.toUpperCase().startsWith("FROZEN"))
                event.setCancelled(true);
        }
    }

    @EventHandler
    public void quitEvent(PlayerQuitEvent event) {
    	
        Map<String, Object> context = new HashMap<String, Object>();
        context.put("message", event.getQuitMessage());

        String determination = doEvents(Arrays.asList
        		("player quits"),
        		null, event.getPlayer(), context).toUpperCase();

        // Handle determine message
        if (determination.toUpperCase().startsWith("MESSAGE"))
            event.setQuitMessage(aH.getStringFrom(determination));
    }

    @EventHandler
    public void respawnEvent(PlayerRespawnEvent event) {
        Map<String, Object> context = new HashMap<String, Object>();
        context.put("location", new dLocation(event.getRespawnLocation()));

        List<String> events = new ArrayList<String>();
        events.add("player respawns");
        
        if (event.isBedSpawn()) {
        	events.add("player respawns at bed");
        }
        else {
        	events.add("player respawns elsewhere");
        }
        
        doEvents(events, null, event.getPlayer(), context);
    }
    
    
    /////////////////////
    //   BLOCK EVENTS
    /////////////////
    
    @EventHandler
    public void blockBreak(BlockBreakEvent event) {

        Map<String, Object> context = new HashMap<String, Object>();
        
        context.put("location", new dLocation(event.getBlock().getLocation()));
        context.put("type", event.getBlock().getType().name());

        String determination = doEvents(Arrays.asList
        		("player breaks block",
        		 "player breaks " + event.getBlock().getType().name(),
        		 "player breaks " + event.getBlock().getType().name() + " with " +
        				 new dItem(event.getPlayer().getItemInHand()).identify().split(":")[0]),
        		null, event.getPlayer(), context);

        if (determination.toUpperCase().startsWith("CANCELLED"))
        	event.setCancelled(true);
    }
    
    @EventHandler
    public void blockIgnite(BlockIgniteEvent event) {

        Map<String, Object> context = new HashMap<String, Object>();
        
        context.put("location", new dLocation(event.getBlock().getLocation()));
        context.put("type", event.getBlock().getType().name());
        
        String determination = doEvents(Arrays.asList
        		("block ignites",
        		 event.getBlock().getType().name() + " ignites"),
        		null, event.getPlayer(), context);
        
        if (determination.toUpperCase().startsWith("CANCELLED"))
        	event.setCancelled(true);
    }
    
    @EventHandler
    public void blockPlace(BlockPlaceEvent event) {

        Map<String, Object> context = new HashMap<String, Object>();
        
        context.put("location", new dLocation(event.getBlock().getLocation()));
        context.put("type", event.getBlock().getType().name());

        String determination = doEvents(Arrays.asList
        		("player places block",
        		 "player places " + event.getBlock().getType().name()),
        		null, event.getPlayer(), context);

        if (determination.toUpperCase().startsWith("CANCELLED"))
        	event.setCancelled(true);
    }
    
    
    @EventHandler
    public void blockRedstone(BlockRedstoneEvent event) {

        Map<String, Object> context = new HashMap<String, Object>();
        
        context.put("location", new dLocation(event.getBlock().getLocation()));
        context.put("type", event.getBlock().getType().name());
        
        List<String> events = new ArrayList<String>();
        
        if (event.getNewCurrent() > 0) {
        	events.add("block powered");
        	events.add(event.getBlock().getType().name() + " powered");
        }
        else {
        	events.add("block unpowered");
        	events.add(event.getBlock().getType().name() + " unpowered");
        }
        
    	String determination = doEvents(events, null, null, context);
    	
        if (determination.toUpperCase().startsWith("CANCELLED"))
        	event.setNewCurrent(event.getOldCurrent());
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
        
        String determination = doEvents(Arrays.asList(
        		entity.getType().name() + " combusts"),
        		null, null, context);

        if (determination.toUpperCase().startsWith("CANCELLED"))
        	event.setCancelled(true);
    }
    
    @EventHandler
    public void entityDamage(EntityDamageEvent event) {
    	
    	Map<String, Object> context = new HashMap<String, Object>();
    	boolean isFatal = false;
    	Entity entity = event.getEntity();
    	String entityType = entity.getType().name();
    	String cause = event.getCause().name();
    	
    	Player contextPlayer = null;
    	dNPC contextNPC = null;
    	
    	if (entity instanceof Player) {
    		context.put("entity", new dPlayer((Player) entity));
    	}
    	else if (CitizensAPI.getNPCRegistry().isNPC(entity)) {
    		contextNPC = DenizenAPI.getDenizenNPC(CitizensAPI.getNPCRegistry().getNPC(entity));
    		context.put("entity", contextNPC);
    		entityType = "npc";
    	}
    	else {
    		context.put("entity", new dEntity(entity));
    	}

    	context.put("damage", event.getDamage());
    	context.put("cause", event.getCause().name());
    	
    	if (entity instanceof LivingEntity) {
    		if (event.getDamage() >= ((LivingEntity) entity).getHealth()) {
    			isFatal = true;
    		}
    	}
    	
    	List<String> events = new ArrayList<String>();
    	events.add("entity damaged");
    	events.add("entity damaged by " + cause);
    	events.add(entityType + " damaged");
    	events.add(entityType + " damaged by " + cause);
    	
    	if (isFatal == true) {
    		events.add("entity killed");
	    	events.add("entity killed by " + cause);
	    	events.add(entityType + " killed");
		    events.add(entityType + " killed by " + cause);
    	}
    	
    	if (event instanceof EntityDamageByEntityEvent) {
    		
    		EntityDamageByEntityEvent subEvent = (EntityDamageByEntityEvent) event;
    		
    		Entity damager = subEvent.getDamager();
    		String damagerType = damager.getType().name();
    		
        	if (damager instanceof Player) {
        		context.put("damager", new dPlayer((Player) damager));
        	}
        	else if (CitizensAPI.getNPCRegistry().isNPC(damager)) {
        		context.put("damager", DenizenAPI.getDenizenNPC(CitizensAPI.getNPCRegistry().getNPC(damager)));
        		damagerType = "npc";
        	}
        	else {
        		context.put("damager", new dEntity(damager));
        	}
    		
    		events.add("entity damaged by entity");
    		events.add("entity damaged by " + damagerType);
    		events.add(entityType + " damaged by entity");
        	events.add(entityType + " damaged by " + damagerType);

    		events.add("entity damages entity");
    		events.add("entity damages " + entityType);
    		events.add(damagerType + " damages entity");
    		events.add(damagerType + " damages " + entityType);
    		
    		if (isFatal == true) {
    			events.add("entity killed by entity");
        		events.add("entity killed by " + damagerType);
        		events.add(entityType + " killed by entity");
        		events.add(entityType + " killed by " + damagerType);
        		
        		events.add("entity kills entity");
        		events.add("entity kills " + entityType);
        		events.add(damagerType + " kills entity");
        		events.add(damagerType + " kills " + entityType);
    		}
    	}
    	
        String determination = doEvents(events, contextNPC, contextPlayer, context);

        if (determination.toUpperCase().startsWith("CANCELLED"))
        	event.setCancelled(true);
        if (aH.matchesValueArg("DAMAGE", determination, aH.ArgumentType.Double))
            event.setDamage(aH.getDoubleFrom(determination));
    }
    
    @EventHandler
    public void entityExplode(EntityExplodeEvent event) {

        Map<String, Object> context = new HashMap<String, Object>();
        Entity entity = event.getEntity();
        
        context.put("entity", new dEntity(entity));
        context.put("location", new dLocation(event.getLocation()));
        
        String determination = doEvents(Arrays.asList
        		(entity.getType().name() + " explodes"),
        		null, null, context);

        if (determination.toUpperCase().startsWith("CANCELLED"))
        	event.setCancelled(true);
    }
    
    // TODO: Make work with all entities
    @EventHandler
    public void entityRegainHealth(EntityRegainHealthEvent event) {

        if (event.getEntity() instanceof  Player
                && !CitizensAPI.getNPCRegistry().isNPC(event.getEntity())) {
            Map<String, Object> context = new HashMap<String, Object>();
            context.put("reason", event.getRegainReason().toString());
            context.put("amount", String.valueOf(event.getAmount()));

            String determination = doEvents(Arrays.asList
            		("player regains health"),
            		null, (Player) event.getEntity(), context);

            if (determination.toUpperCase().startsWith("CANCELLED"))
                event.setCancelled(true);
            if (aH.matchesValueArg("AMOUNT", determination, aH.ArgumentType.Double))
                event.setAmount(aH.getDoubleFrom(determination));
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
        	context.put("target", new dEntity(target));
        }

        String determination = doEvents(Arrays.asList
        		(entity.getType().name() + " targets " + target.getType().name(),
        		 entity.getType().name() + " targets " + target.getType().name() + " because " + event.getReason().name()),
        		null, null, context);

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
        
        String determination = doEvents
        		(Arrays.asList(entity.getType().name() + " teleports"),
        		null, null, context);

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
        
        List<String> events = new ArrayList<String>();
        events.add("weather changes");
        events.add("weather changes in " + world);
        
        if (event.toWeatherState() == true) {
        	context.put("weather", "rain");
        	events.add("weather rains");
        	events.add("weather rains in " + world);
        }
        else {
        	context.put("weather", "clear");
        	events.add("weather clears");
        	events.add("weather clears in " + world);
        }
        
        String determination = doEvents(events, null, null, context);
        
        if (determination.toUpperCase().startsWith("CANCELLED"))
            event.setCancelled(true);
    }
}
