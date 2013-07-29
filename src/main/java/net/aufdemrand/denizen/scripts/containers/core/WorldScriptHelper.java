package net.aufdemrand.denizen.scripts.containers.core;

import net.aufdemrand.denizen.Settings;
import net.aufdemrand.denizen.objects.*;
import net.aufdemrand.denizen.scripts.ScriptBuilder;
import net.aufdemrand.denizen.scripts.ScriptEntry;
import net.aufdemrand.denizen.scripts.commands.core.DetermineCommand;
import net.aufdemrand.denizen.scripts.queues.core.InstantQueue;
import net.aufdemrand.denizen.tags.TagManager;
import net.aufdemrand.denizen.utilities.DenizenAPI;
import net.aufdemrand.denizen.utilities.debugging.dB;
import net.aufdemrand.denizen.utilities.Utilities;
import net.citizensnpcs.api.CitizensAPI;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.Vehicle;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockBurnEvent;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.event.block.BlockPhysicsEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.BlockRedstoneEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityCombustEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.EntityTameEvent;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.entity.EntityTeleportEvent;
import org.bukkit.event.entity.ExplosionPrimeEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.hanging.HangingBreakEvent;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerBedEnterEvent;
import org.bukkit.event.player.PlayerBedLeaveEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerBucketFillEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.event.player.PlayerGameModeChangeEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLevelChangeEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.server.ServerCommandEvent;
import org.bukkit.event.vehicle.VehicleDamageEvent;
import org.bukkit.event.vehicle.VehicleDestroyEvent;
import org.bukkit.event.vehicle.VehicleEnterEvent;
import org.bukkit.event.vehicle.VehicleExitEvent;
import org.bukkit.event.weather.LightningStrikeEvent;
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
    

    /////////////////////
    //   EVENT HANDLER
    /////////////////
    
    public static String doEvents(List<String> eventNames, dNPC npc, Player player, Map<String, Object> context) {

        String determination = "none";

        // dB.log("Fired for '" + eventNames.toString() + "'");

        for (WorldScriptContainer script : world_scripts.values()) {

            if (script == null) continue;
            
            for (String eventName : eventNames) {

                if (!script.contains("EVENTS.ON " + eventName.toUpperCase())) continue;

            	// Fetch script from Event
                //
                // Note: a "new dPlayer(null)" will not be null itself,
                //       so keep a ternary operator here
            	List<ScriptEntry> entries = script.getEntries
            			(player != null ? new dPlayer(player) : null,
            			 npc, "events.on " + eventName);
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
                InstantQueue.getQueue(null).addEntries(entries).start(); 

            	if (DetermineCommand.hasOutcome(id))
            		determination =  DetermineCommand.getOutcome(id);
            	}
        }

        return determination;
    }
    
    
    /////////////////////
    //   BLOCK EVENTS
    /////////////////
    
    @EventHandler
    public void blockBreak(BlockBreakEvent event) {

        Map<String, Object> context = new HashMap<String, Object>();
        Block block = event.getBlock();
        String blockType = block.getType().name();
        
        context.put("location", new dLocation(block.getLocation()));
        context.put("type", new Element(blockType));

        dItem item = new dItem(event.getPlayer().getItemInHand());
        
        List<String> events = new ArrayList<String>();
        events.add("player breaks block");
        events.add("player breaks " + blockType);
        events.add("player breaks block with " + item.identify());
        events.add("player breaks " + blockType + " with " + item.identify());
        
        if (item.identify().equals(item.identify().split(":")[0]) == false) {
        	
        	events.add("player breaks block with " +
        			item.identify().split(":")[0]);
        	events.add("player breaks " + blockType + " with " +
        			item.identify().split(":")[0]);
        }
        
        String determination = doEvents(events, null, event.getPlayer(), context);

        if (determination.toUpperCase().startsWith("CANCELLED"))
        	event.setCancelled(true);
        
        if (determination.toUpperCase().startsWith("DROP")) {

        	// Cancel the event
        	event.setCancelled(true);
        	
        	// If "drops:nothing" is used, clear the block's drops
        	if (aH.getStringFrom(determination).equalsIgnoreCase("nothing")) {
        		block.getDrops().clear();
        	}
        	
        	// Otherwise, get a list of items from "drops"
        	else {
        	
        		List<dObject> newItems = dList.valueOf(aH.getStringFrom(determination)).filter(dItem.class);
            	List<ItemStack> drops = new ArrayList<ItemStack>();
            	
            	for (dObject newItem : newItems) {
            		
            		block.getWorld().dropItemNaturally(block.getLocation(),
            				((dItem) newItem).getItemStack()); // Drop each item
            		
            		drops.add(((dItem) newItem).getItemStack());
            	}
        	}

        	// Remove the block
        	block.setType(Material.AIR);
        }
    }
    
    @EventHandler
    public void blockBurn(BlockBurnEvent event) {

        Map<String, Object> context = new HashMap<String, Object>();
        
        context.put("location", new dLocation(event.getBlock().getLocation()));
        context.put("type", new Element(event.getBlock().getType().name()));

        String determination = doEvents(Arrays.asList
        		("block burns",
        		 event.getBlock().getType().name() + " burns"),
        		null, null, context);

        if (determination.toUpperCase().startsWith("CANCELLED"))
        	event.setCancelled(true);
    }
    
    @EventHandler
    public void blockIgnite(BlockIgniteEvent event) {

        Map<String, Object> context = new HashMap<String, Object>();
        
        context.put("location", new dLocation(event.getBlock().getLocation()));
        context.put("type", new Element(event.getBlock().getType().name()));
        
        String determination = doEvents(Arrays.asList
        		("block ignites",
        		 event.getBlock().getType().name() + " ignites"),
        		null, event.getPlayer(), context);
        
        if (determination.toUpperCase().startsWith("CANCELLED"))
        	event.setCancelled(true);
    }
    
    @EventHandler
    public void blockPhysics(BlockPhysicsEvent event) {

        Map<String, Object> context = new HashMap<String, Object>();
        
        context.put("location", new dLocation(event.getBlock().getLocation()));
        context.put("type", new Element(event.getBlock().getType().name()));

        String determination = doEvents(Arrays.asList
        		("block moves",
        		 event.getBlock().getType().name() + " moves"),
        		null, null, context);

        if (determination.toUpperCase().startsWith("CANCELLED"))
        	event.setCancelled(true);
    }
    
    @EventHandler
    public void blockPlace(BlockPlaceEvent event) {

        Map<String, Object> context = new HashMap<String, Object>();
        
        context.put("location", new dLocation(event.getBlock().getLocation()));
        context.put("type", new Element(event.getBlock().getType().name()));

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
        context.put("type", new Element(event.getBlock().getType().name()));
        
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
    
    @EventHandler
    public void blockFromTo(BlockFromToEvent event) {

        Map<String, Object> context = new HashMap<String, Object>();
        
        context.put("location", new dLocation(event.getBlock().getLocation()));
        context.put("type", new Element(event.getBlock().getType().name()));
        context.put("destination", new dLocation(event.getToBlock().getLocation()));

        String determination = doEvents(Arrays.asList
        		("block spreads",
        		 event.getBlock().getType().name() + " spreads"),
        		null, null, context);

        if (determination.toUpperCase().startsWith("CANCELLED"))
        	event.setCancelled(true);
    }
    
    @EventHandler
    public void signChange(SignChangeEvent event) {
    	
    	final Map<String, Object> context = new HashMap<String, Object>();
    	
    	final Player player = event.getPlayer();
    	final Block block = event.getBlock();
        Sign sign = (Sign) block.getState();
        final String[] oldLines = sign.getLines();
        
        context.put("old", new dList(Arrays.asList(oldLines)));
		context.put("location", new dLocation(block.getLocation()));
        
    	Bukkit.getScheduler().scheduleSyncDelayedTask(DenizenAPI.getCurrentInstance(), new Runnable() {
			public void run() {

				Sign sign = (Sign) block.getState();
		    	context.put("new", new dList(Arrays.asList(sign.getLines())));
		    	
		    	String determination = doEvents(Arrays.asList
		    			("player changes sign"),
		        		null, player, context);

		    	if (determination.toUpperCase().startsWith("CANCELLED"))
		    		Utilities.setSignLines(sign, oldLines);
			}
		}, 1);
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
                
                context.put("time", new Element(String.valueOf(hour)));
                context.put("world", new dWorld(world));
                
                doEvents(Arrays.asList("time changes in " + world.getName(),
                					   hour + ":00 in " + world.getName()),
                		null, null, context);
                
                current_time.put(world.getName(), hour);
            }
        }
    }
    
    
    /////////////////////
    //   HANGING EVENTS
    /////////////////
    
    @EventHandler
    public void hangingBreak(HangingBreakEvent event) {

        Map<String, Object> context = new HashMap<String, Object>();
    	
        Player player = null;
    	dNPC npc = null;
    	
    	String hangingType = event.getEntity().getType().name();
    	String cause =  event.getCause().name();
        
        context.put("hanging", new dEntity(event.getEntity()));
        context.put("cause", new Element(cause));

        List<String> events = new ArrayList<String>();
        events.add("hanging breaks");
        events.add("hanging breaks because " + cause);
        events.add(hangingType + " breaks");
        events.add(hangingType +
		 		" breaks because " + cause);
        
        if (event instanceof HangingBreakByEntityEvent) {
        	
        	HangingBreakByEntityEvent subEvent = (HangingBreakByEntityEvent) event;
        	
        	Entity entity = subEvent.getRemover();
        	String entityType = entity.getType().name();
        	
        	if (CitizensAPI.getNPCRegistry().isNPC(entity)) {
        		npc = DenizenAPI.getDenizenNPC(CitizensAPI.getNPCRegistry().getNPC(entity));
        		context.put("entity", npc);
        		entityType = "npc";
        	}
        	else if (entity instanceof Player) {
        		player = (Player) entity;
        		context.put("entity", new dPlayer((Player) entity));
        	}
        	else {
        		context.put("entity", new dEntity(entity));
        	}
        	
        	events.add("entity breaks hanging");
        	events.add("entity breaks hanging because " + cause);
        	events.add("entity breaks " + hangingType);
        	events.add("entity breaks " + hangingType + " because " + cause);
        	events.add(entityType + " breaks hanging");
        	events.add(entityType + " breaks hanging because " + cause);
        	events.add(entityType + " breaks " + hangingType);
        	events.add(entityType + " breaks " + hangingType + " because " + cause);
        }
        
        String determination = doEvents(events, npc, player, context);

        if (determination.toUpperCase().startsWith("CANCELLED"))
        	event.setCancelled(true);
    }
    
    
    /////////////////////
    //   ENTITY EVENTS
    /////////////////
    
    @EventHandler
    public void creatureSpawn(CreatureSpawnEvent event) {

        Map<String, Object> context = new HashMap<String, Object>();
        Entity entity = event.getEntity();
        
        context.put("entity", new dEntity(entity));
        context.put("reason", new Element(event.getSpawnReason().name()));
        
        String determination = doEvents(Arrays.asList
        		("entity spawns",
        		 "entity spawns because " + event.getSpawnReason().name(),
        		 entity.getType().name() + " spawns",
        		 entity.getType().name() + " spawns because " +
        				 event.getSpawnReason().name()),
        		null, null, context);

        if (determination.toUpperCase().startsWith("CANCELLED"))
        	event.setCancelled(true);
    }
    
    @EventHandler
    public void entityCombust(EntityCombustEvent event) {

        Map<String, Object> context = new HashMap<String, Object>();
        Entity entity = event.getEntity();
        
        context.put("entity", new dEntity(entity));
        context.put("duration", new Duration((long) event.getDuration()));
        
        String determination = doEvents(Arrays.asList
        		("entity combusts",
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

    	String determination;
    	
    	Player player = null;
    	dNPC npc = null;
    	
    	if (CitizensAPI.getNPCRegistry().isNPC(entity)) {
    		npc = DenizenAPI.getDenizenNPC(CitizensAPI.getNPCRegistry().getNPC(entity));
    		context.put("entity", npc);
    		entityType = "npc";
    	}
    	else if (entity instanceof Player) {
    		player = (Player) entity;
    		context.put("entity", new dPlayer((Player) entity));
    	}
    	else {
    		context.put("entity", new dEntity(entity));
    	}

    	context.put("damage", new Element(event.getDamage()));
    	context.put("cause", new Element(event.getCause().name()));
    	
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
    		
    		// Have a different set of player and NPC contexts for events
    		// like "player damages player" from the one we have for
    		// "player damaged by player"
    		
        	Player subPlayer = null;
        	dNPC subNPC = null;
    		
    		Entity damager = subEvent.getDamager();
    		String damagerType = damager.getType().name();
    		
        	if (CitizensAPI.getNPCRegistry().isNPC(damager)) {
        		subNPC = DenizenAPI.getDenizenNPC(CitizensAPI.getNPCRegistry().getNPC(entity));
        		context.put("damager", DenizenAPI.getDenizenNPC
        						(CitizensAPI.getNPCRegistry().getNPC(damager)));
        		damagerType = "npc";
        		
        		// If we had no NPC in our regular context, use this one
        		if (npc == null) npc = subNPC;
        	}
    		else if (damager instanceof Player) {
        		subPlayer = (Player) damager;
        		context.put("damager", new dPlayer((Player) damager));
        		
        		// If we had no player in our regular context, use this one
        		if (player == null) player = subPlayer;
        	}
        	else {
        		context.put("damager", new dEntity(damager));
        		
        		if (damager instanceof Projectile) {
        			context.put("shooter", new dEntity(((Projectile) damager).getShooter()));
        		}
        	}
    		
    		events.add("entity damaged by entity");
    		events.add("entity damaged by " + damagerType);
    		events.add(entityType + " damaged by entity");
        	events.add(entityType + " damaged by " + damagerType);

        	// Have a new list of events for the subContextPlayer
        	// and subContextNPC
        	
        	List<String> subEvents = new ArrayList<String>();
        	
    		subEvents.add("entity damages entity");
    		subEvents.add("entity damages " + entityType);
    		subEvents.add(damagerType + " damages entity");
    		subEvents.add(damagerType + " damages " + entityType);
    		
    		if (isFatal == true) {
    			events.add("entity killed by entity");
        		events.add("entity killed by " + damagerType);
        		events.add(entityType + " killed by entity");
        		events.add(entityType + " killed by " + damagerType);
        		
        		subEvents.add("entity kills entity");
        		subEvents.add("entity kills " + entityType);
        		subEvents.add(damagerType + " kills entity");
        		subEvents.add(damagerType + " kills " + entityType);
    		}
    		
    		determination = doEvents(subEvents, subNPC, subPlayer, context);

            if (determination.toUpperCase().startsWith("CANCELLED"))
            	event.setCancelled(true);
            if (aH.matchesValueArg("DAMAGE", determination, aH.ArgumentType.Double))
                event.setDamage(aH.getDoubleFrom(determination));
    	}
    	
        determination = doEvents(events, npc, player, context);

        if (determination.toUpperCase().startsWith("CANCELLED"))
        	event.setCancelled(true);
        if (aH.matchesValueArg("DAMAGE", determination, aH.ArgumentType.Double))
            event.setDamage(aH.getDoubleFrom(determination));
    }

    @EventHandler
    public void entityExplode(EntityExplodeEvent event) {

        Map<String, Object> context = new HashMap<String, Object>();
        Entity entity = event.getEntity();
        
        if (entity == null) return;
        
        context.put("entity", new dEntity(entity));
        context.put("location", new dLocation(event.getLocation()));
        String blocks = "";
        for (Block block : event.blockList()) {
        	blocks = blocks + new dLocation(block.getLocation()) + "|";
        }
        context.put("blocks", blocks.length() > 0 ? new dList(blocks) : null);
        
        String determination = doEvents(Arrays.asList
        		("entity explodes",
        		 entity.getType().name() + " explodes"),
        		null, null, context);

        if (determination.toUpperCase().startsWith("CANCELLED"))
        	event.setCancelled(true);
    }
    
    @EventHandler
    public void entityRegainHealth(EntityRegainHealthEvent event) {

        Map<String, Object> context = new HashMap<String, Object>();
        Entity entity = event.getEntity();
        String entityType = entity.getType().name();
        
        context.put("reason", new Element(event.getRegainReason().name()));
        context.put("amount", new Element(event.getAmount()));
        
        Player player = null;
        dNPC npc = null;
        
        if (CitizensAPI.getNPCRegistry().isNPC(entity)) {
        	npc = DenizenAPI.getDenizenNPC(CitizensAPI.getNPCRegistry().getNPC(entity));
            context.put("entity", npc);
            entityType = "npc";
        }
        else if (entity instanceof Player) {
        	player = (Player) entity;
        	context.put("entity", new dPlayer(player));
        }
        else {
        	context.put("entity", new dEntity(entity));
        }

        String determination = doEvents(Arrays.asList
        		("entity heals",
        		 "entity heals because " + event.getRegainReason().name(),
        		 entityType + " heals",
        		 entityType + " heals because " + event.getRegainReason().name()),
        		 npc, player, context);

        if (determination.toUpperCase().startsWith("CANCELLED"))
        	event.setCancelled(true);
        
        if (aH.matchesValueArg("AMOUNT", determination, aH.ArgumentType.Double))
            event.setAmount(aH.getDoubleFrom(determination));
    }
    
    @EventHandler
    public void entityShootBow(EntityShootBowEvent event) {
    	
        Map<String, Object> context = new HashMap<String, Object>();
        Entity entity = event.getEntity();
        context.put("entity", new dEntity(entity));
        
        String determination = doEvents(Arrays.asList
        		("entity shoots arrow",
        		 entity.getType().name() + " shoots arrow"),
        		null, null, context);
        
        if (dEntity.matches(determination)) {
        	dEntity newProjectile = dEntity.valueOf(determination);
        	if (!newProjectile.isSpawned())
        		try { 
        			newProjectile = new dEntity(entity.getWorld().spawnEntity(entity.getLocation(), EntityType.valueOf(aH.getStringFrom(determination))));
        			event.setProjectile(newProjectile.getBukkitEntity());
        		}
        		catch(Exception e){}
        	else
        		event.setProjectile(newProjectile.getBukkitEntity());
        }
        
        if (determination.toUpperCase().startsWith("CANCELLED"))
        	event.setCancelled(true);
        
    }
    
    @EventHandler
    public void entityTame(EntityTameEvent event) {

        Map<String, Object> context = new HashMap<String, Object>();
        Entity entity = event.getEntity();
        context.put("entity", new dEntity(entity));
        Player player = null;
        
        List<String> events = new ArrayList<String>();
        events.add("entity tamed");
        events.add(entity.getType().name() + " tamed");
        
        if (event.getOwner() instanceof Player) {
        	player = (Player) event.getOwner();
        	events.add("player tames entity");
        	events.add("player tames " + entity.getType().name());
        }

        String determination = doEvents(events, null, player, context);

        if (determination.toUpperCase().startsWith("CANCELLED"))
        	event.setCancelled(true);
    }
    
    @EventHandler
    public void entityTarget(EntityTargetEvent event) {

        Map<String, Object> context = new HashMap<String, Object>();
        Entity entity = event.getEntity();
        Entity target = event.getTarget();
        Player player = null;
        
        String reason = event.getReason().name();
        String entityType = entity.getType().name();
        
        context.put("entity", new dEntity(entity));
        context.put("reason", new Element(reason));
        
        List<String> events = new ArrayList<String>();
        events.add("entity targets");
        events.add("entity targets because " + reason);
        events.add(entityType + " targets");
        events.add(entityType + " targets because " + reason);

        if (target != null) {
        	
            if (event.getTarget() instanceof Player) {
            	player = (Player) target;
            	context.put("target", new dPlayer(player));
            }
            else {
            	context.put("target", new dEntity(target));
            }
            
            String targetType = target.getType().name();
        	
        	events.add("entity targets entity");
        	events.add("entity targets entity because " + reason);
        	events.add("entity targets " + targetType);
        	events.add("entity targets " + targetType + " because " + reason);
        	events.add(entityType + " targets entity");
        	events.add(entityType + " targets entity because " + reason);
        	events.add(entityType + " targets " + targetType);
        	events.add(entityType + " targets " + targetType + " because " + reason);
        }
        
        String determination = doEvents(events, null, player, context);

        if (determination.toUpperCase().startsWith("CANCELLED"))
        	event.setCancelled(true);
        
        // If the determination matches a dEntity, change the event's target
        //
        // Note: this only works with a handful of monster types, like spiders
        //       and endermen for instance
        
        else if (dEntity.matches(determination)) {
        	dEntity newTarget = dEntity.valueOf(determination);
        	
        	if (newTarget.isSpawned()) {
        		event.setTarget(newTarget.getBukkitEntity());
        	}
        }
    }
    
    @EventHandler
    public void entityTeleport(EntityTeleportEvent event) {

        Map<String, Object> context = new HashMap<String, Object>();
        Entity entity = event.getEntity();
        
        context.put("entity", new dEntity(entity));
        context.put("origin", new dLocation(event.getFrom()));
        context.put("destination", new dLocation(event.getTo()));
        
        String determination = doEvents(Arrays.asList
        		("entity teleports",
        		 entity.getType().name() + " teleports"),
        		null, null, context);

        if (determination.toUpperCase().startsWith("CANCELLED"))
        	event.setCancelled(true);
    }
    
    @EventHandler
    public void explosionPrimeEvent(ExplosionPrimeEvent event) {

        Map<String, Object> context = new HashMap<String, Object>();
        Entity entity = event.getEntity();
        
        context.put("entity", new dEntity(entity));
        context.put("radius", new Element(String.valueOf(event.getRadius())));
        context.put("fire", new Element(event.getFire()));
        
        String determination = doEvents(Arrays.asList
        		("entity explosion primes",
        		 entity.getType().name() + " explosion primes"),
        		null, null, context);

        if (determination.toUpperCase().startsWith("CANCELLED"))
        	event.setCancelled(true);
    }
    
    @EventHandler
    public void foodLevelChange(FoodLevelChangeEvent event) {

        Map<String, Object> context = new HashMap<String, Object>();
        Entity entity = event.getEntity();
        
        context.put("entity", entity instanceof Player ?
        		 			  new dPlayer((Player) entity) :
        				      new dEntity(entity));
        
        String determination = doEvents(Arrays.asList
        		(entity.getType().name() + " changes food level"),
        		null, null, context);

        if (determination.toUpperCase().startsWith("CANCELLED"))
        	event.setCancelled(true);
        if (aH.matchesValueArg("FOOD", determination, aH.ArgumentType.Integer))
            event.setFoodLevel(aH.getIntegerFrom(determination));
    }
    
    
    /////////////////////
    //   INVENTORY EVENTS
    /////////////////
    
    @EventHandler
    public void inventoryClickEvent(InventoryClickEvent event) {
    	
        Map<String, Object> context = new HashMap<String, Object>();
    	dItem item = new dItem(event.getCurrentItem());
        
        Player player = (Player) event.getWhoClicked();
        String type = event.getInventory().getType().name();
        String click = event.getClick().name();
        
        context.put("item", item);
        context.put("inventory", new dInventory(event.getInventory()));
        context.put("click", new Element(click));
        
        List<String> events = new ArrayList<String>();
        events.add("player clicks in inventory");
        events.add("player clicks in " + type + " inventory");
        
        String interaction = "player " + click + " clicks";
        
        events.add(interaction + " in inventory");
        events.add(interaction + " in " + type + " inventory");
        
        if (item.getItemStack() != null) {

        	events.add(interaction + " on " +
        			item.identify() + " in inventory");
        	events.add(interaction + " on " +
        			item.identify() + " in " + type + " inventory");
        	
        	if (item.identify().equals(item.identify().split(":")[0]) == false) {
        		
        		events.add(interaction + " on " +
                		item.identify().split(":")[0] + " in inventory");
        		events.add(interaction + " on " +
            			item.identify().split(":")[0] + " in " + type + " inventory");
        	}
        }
        
        String determination = doEvents(events, null, player, context);

        if (determination.toUpperCase().startsWith("CANCELLED"))
            event.setCancelled(true);
    }
    
    
    /////////////////////
    //   PLAYER EVENTS
    /////////////////

    @EventHandler(priority = EventPriority.LOWEST)
    public void playerChat(final AsyncPlayerChatEvent event) {
    	
        final Map<String, Object> context = new HashMap<String, Object>();
        context.put("message", new Element(event.getMessage()));

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

        String determination = doEvents
        		(Arrays.asList("player enters bed"),
        		null, event.getPlayer(), context);

        if (determination.toUpperCase().startsWith("CANCELLED"))
            event.setCancelled(true);
    }
    
    @EventHandler
    public void bedLeaveEvent(PlayerBedLeaveEvent event) {
    	
        Map<String, Object> context = new HashMap<String, Object>();
        context.put("location", new dLocation(event.getBed().getLocation()));

        doEvents(Arrays.asList
        		("player leaves bed"),
        		null, event.getPlayer(), context);
    }
    
    @EventHandler
    public void playerBucketEmpty(PlayerBucketEmptyEvent event) {
    	
        Map<String, Object> context = new HashMap<String, Object>();
        context.put("item", new dItem(event.getBucket()));
        context.put("location", new dLocation(event.getBlockClicked().getLocation()));

        String determination = doEvents(Arrays.asList
        		("player empties bucket"),
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

        String determination = doEvents(Arrays.asList
        		("player fills bucket"),
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
        context.put("command", new Element(command));
        context.put("raw_args", new Element((event.getMessage().split(" ").length > 1 ? event.getMessage().split(" ", 2)[1] : "")));
        context.put("server", Element.FALSE);
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
        context.put("message", new Element(event.getDeathMessage()));
        
        String determination = doEvents(Arrays.asList
        		("player dies",
        		 "player death"),
        		null, event.getEntity(), context);

        // Handle message
        if (determination.toUpperCase().startsWith("MESSAGE"))
            event.setDeathMessage(aH.getStringFrom(determination));
    }
    
    @EventHandler
    public void playerFish(PlayerFishEvent event) {

        Entity entity = event.getCaught();
        String state = event.getState().name();
        dNPC npc = null;
    	
    	Map<String, Object> context = new HashMap<String, Object>();
        context.put("hook", new dEntity(event.getHook()));
        context.put("state", new Element(state));

        List<String> events = new ArrayList<String>();
        events.add("player fishes");
        events.add("player fishes while " + state);
        
        if (entity != null) {
        	
        	String entityType = entity.getType().name();
        	
        	if (CitizensAPI.getNPCRegistry().isNPC(entity)) {
        		npc = DenizenAPI.getDenizenNPC(CitizensAPI.getNPCRegistry().getNPC(entity));
        		context.put("entity", npc);
        		entityType = "npc";
        	}
        	else if (entity instanceof Player) {
        		context.put("entity", new dPlayer((Player) entity));
        	}
        	else {
        		context.put("entity", new dEntity(entity));
        	}
        	
        	events.add("player fishes " + entityType);
        	events.add("player fishes " + entityType + " while " + state);
        }
        
        String determination = doEvents(events, npc, event.getPlayer(), context);

        // Handle message
        if (determination.toUpperCase().startsWith("CANCELLED"))
            event.setCancelled(true);
    }
    
    @EventHandler
    public void playerGameModeChange(PlayerGameModeChangeEvent event) {
    	
    	Map<String, Object> context = new HashMap<String, Object>();
        context.put("gamemode", new Element(event.getNewGameMode().name()));
        
        String determination = doEvents(Arrays.asList
        		("player changes gamemode",
        		 "player changes gamemode to " + event.getNewGameMode().name()),
        		null, event.getPlayer(), context);

        // Handle message
        if (determination.toUpperCase().startsWith("CANCELLED"))
            event.setCancelled(true);
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
        	events.add(interaction + " with " + item.identify());
        	
        	if (item.identify().equals(item.identify().split(":")[0]) == false) {
        	
        		events.add(interaction + " with " + item.identify().split(":")[0]);
        	}
        }
        
        if (event.hasBlock()) {
            Block block = event.getClickedBlock();
            context.put("location", new dLocation(block.getLocation()));
            
            interaction = interaction + " on " + block.getType().name(); 
        	events.add(interaction);
        	
        	if (event.hasItem()) {
            	events.add(interaction + " with item");
            	events.add(interaction + " with " + item.identify());
            	
            	if (item.identify().equals(item.identify().split(":")[0]) == false) {
            		
                	events.add(interaction + " with " + item.identify().split(":")[0]);
            	}
            }
        }
        
        String determination = doEvents(events, null, event.getPlayer(), context);
        
        if (determination.toUpperCase().startsWith("CANCELLED"))
            event.setCancelled(true);
    }
    
    @EventHandler
    public void playerInteractEntity(PlayerInteractEntityEvent event) {

    	Entity entity = event.getRightClicked();
    	String entityType = entity.getType().name();
    	dNPC npc = null;
    	dItem item = new dItem(event.getPlayer().getItemInHand());
    	
        String determination;
        Map<String, Object> context = new HashMap<String, Object>();
        context.put("location", new dLocation(event.getRightClicked().getLocation()));

        if (CitizensAPI.getNPCRegistry().isNPC(entity)) {
        	npc = DenizenAPI.getDenizenNPC
					(CitizensAPI.getNPCRegistry().getNPC(entity));
        	context.put("entity", npc);
        	entityType = "npc";
        }
        else if (entity instanceof Player) {
        	context.put("entity", new dPlayer((Player) entity));
        }
        else {
            context.put("entity", new dEntity(entity));
        }
        
        List<String> events = new ArrayList<String>();
        events.add("player right clicks entity");
        events.add("player right clicks " + entityType);
        events.add("player right clicks entity with " +
        		item.identify());
        events.add("player right clicks " + entityType + " with " +
           		item.identify());
        
        if (item.identify().equals(item.identify().split(":")[0]) == false) {
        	
            events.add("player right clicks entity with " +
            		item.identify().split(":")[0]);
            events.add("player right clicks " + entityType + " with " +
               		item.identify().split(":")[0]);
        }
        
        if (entity instanceof ItemFrame) {
        	dItem itemFrame = new dItem(((ItemFrame) entity).getItem());
        	context.put("itemframe", itemFrame);
        	
        	events.add("player right clicks " + entityType + " " +
        			itemFrame.identify());
        	
        	if (itemFrame.identify().equals(itemFrame.identify().split(":")[0]) == false) {
        		
        		events.add("player right clicks " + entityType + " " +
            			itemFrame.identify().split(":")[0]);
        	}
        }
        
        determination = doEvents(events, npc, event.getPlayer(), context);
        
        if (determination.toUpperCase().startsWith("CANCELLED"))
        	event.setCancelled(true);
    }
    
    @EventHandler
    public void playerItemConsume(PlayerItemConsumeEvent event) {
        
        dItem item = new dItem(event.getItem()); 
    	
    	Map<String, Object> context = new HashMap<String, Object>();
        context.put("item", item);
        
        List<String> events = new ArrayList<String>();
        events.add("player consumes " + item.identify());
        
        if (item.identify().equals(item.identify().split(":")[0]) == false) {
        	
        	events.add("player consumes " + item.identify().split(":")[0]);
        }
        
        String determination = doEvents(events, null, event.getPlayer(), context);
        
        if (determination.toUpperCase().startsWith("CANCELLED"))
            event.setCancelled(true);
    }
    
    @EventHandler
    public void joinEvent(PlayerJoinEvent event) {
        Map<String, Object> context = new HashMap<String, Object>();
        context.put("message", new Element(event.getJoinMessage()));

        String determination = doEvents(Arrays.asList
        		("player joins",
        		 "player join"),
        		null, event.getPlayer(), context);

        // Handle message
        if (determination.toUpperCase().startsWith("MESSAGE"))
            event.setJoinMessage(aH.getStringFrom(determination));
    }
    
    @EventHandler
    public void levelChangeEvent(PlayerLevelChangeEvent event) {
    	
        Map<String, Object> context = new HashMap<String, Object>();
        context.put("level", new Element(event.getNewLevel()));

        doEvents(Arrays.asList
        		("player levels up",
        		 "player levels up to " + event.getNewLevel(),
        		 "player levels up from " + event.getOldLevel()),
        		null, event.getPlayer(), context);
    }
    
    @EventHandler
    public void loginEvent(PlayerLoginEvent event) {
    	
        Map<String, Object> context = new HashMap<String, Object>();
        context.put("hostname", new Element(event.getHostname()));

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
            context.put("notable", new Element(name));

            String determination = doEvents(Arrays.asList
            		("player walks over notable",
            		 "player walks over " + name,
            		 "walked over notable",
            		 "walked over " + name),
            		null, event.getPlayer(), context);
            
            if (determination.toUpperCase().startsWith("CANCELLED") ||
            	determination.toUpperCase().startsWith("FROZEN"))
                event.setCancelled(true);
        }
    }

    @EventHandler
    public void quitEvent(PlayerQuitEvent event) {
    	
        Map<String, Object> context = new HashMap<String, Object>();
        context.put("message", new Element(event.getQuitMessage()));

        String determination = doEvents(Arrays.asList
        		("player quits",
        		 "player quit"),
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
        
        String determination = doEvents(events, null, event.getPlayer(), context);
        
        // Handle determine message
        if (determination.toUpperCase().startsWith("LOCATION")) {
        	
        	dLocation location = dLocation.valueOf(aH.getStringFrom(determination));

        	if (location != null) event.setRespawnLocation(location);
        }
    }
    

    /////////////////////
    //   SERVER EVENTS
    /////////////////

    @EventHandler
    public void serverCommandEvent(ServerCommandEvent event) {
        Map<String, Object> context = new HashMap<String, Object>();
        
        List<String> args = Arrays.asList(
                aH.buildArgs(
                        TagManager.tag(null, null,
                                (event.getCommand().split(" ").length > 1 ? event.getCommand().split(" ", 2)[1] : ""))));

        dList args_list = new dList(args);

        String command = event.getCommand().split(" ")[0].replace("/", "").toUpperCase();

        // Fill context
        context.put("args", args_list);
        context.put("command", new Element(command));
        context.put("raw_args", new Element((event.getCommand().split(" ").length > 1 ? event.getCommand().split(" ", 2)[1] : "")));
        context.put("server", Element.TRUE);

        doEvents(Arrays.asList("command",
        					   command + " command"),
        		null, null, context);
    }
    
    
    /////////////////////
    //   VEHICLE EVENTS
    /////////////////
    
    @EventHandler
    public void vehicleDamage(VehicleDamageEvent event) {
    	
        Entity entity = event.getAttacker();
    	
        if (entity == null) return;
        
        Map<String, Object> context = new HashMap<String, Object>();
    	Vehicle vehicle = event.getVehicle();
        
    	String entityType = entity.getType().name();
    	String vehicleType = vehicle.getType().name();
    	
    	Player player = null;

    	context.put("damage", new Element(event.getDamage()));
    	context.put("vehicle", new dEntity(vehicle));
    	
    	if (entity instanceof Player) {
    		context.put("entity", new dPlayer((Player) entity));
    		player = (Player) entity;
    	}
    	else {
    		context.put("entity", new dEntity(entity));
    	}
    	
        String determination = doEvents(Arrays.asList
        		("entity damages vehicle",
        		 entityType + " damages vehicle",
        		 "entity damages " + vehicleType,
        		 entityType + " damages " + vehicleType),
        		null, player, context);
        
        if (determination.toUpperCase().startsWith("CANCELLED"))
            event.setCancelled(true);
        
        if (aH.matchesValueArg("DAMAGE", determination, aH.ArgumentType.Double))
            event.setDamage(aH.getDoubleFrom(determination));
    }
    
    @EventHandler
    public void vehicleDestroy(VehicleDestroyEvent event) {
    	    	
        Map<String, Object> context = new HashMap<String, Object>();
        
    	Entity entity = event.getAttacker();
    	Vehicle vehicle = event.getVehicle();
        
    	String entityType = entity.getType().name();
    	String vehicleType = vehicle.getType().name();
    	
    	Player player = null;

    	context.put("vehicle", new dEntity(vehicle));
    	
    	if (entity instanceof Player) {
    		context.put("entity", new dPlayer((Player) entity));
    		player = (Player) entity;
    	}
    	else {
    		context.put("entity", new dEntity(entity));
    	}
    	
        String determination = doEvents(Arrays.asList
        		("entity destroys vehicle",
        		 entityType + " destroys vehicle",
        		 "entity destroys " + vehicleType,
        		 entityType + " destroys " + vehicleType),
        		null, player, context);
        
        if (determination.toUpperCase().startsWith("CANCELLED"))
            event.setCancelled(true);
    }
    
    @EventHandler
    public void vehicleEnter(VehicleEnterEvent event) {
    	    	
        Map<String, Object> context = new HashMap<String, Object>();
        
    	Entity entity = event.getEntered();
    	Vehicle vehicle = event.getVehicle();
        
    	String entityType = entity.getType().name();
    	String vehicleType = vehicle.getType().name();
    	
    	Player player = null;

    	context.put("vehicle", new dEntity(vehicle));
    	
    	if (entity instanceof Player) {
    		context.put("entity", new dPlayer((Player) entity));
    		player = (Player) entity;
    	}
    	else {
    		context.put("entity", new dEntity(entity));
    	}
    	
        String determination = doEvents(Arrays.asList
        		("entity enters vehicle",
        		 entityType + " enters vehicle",
        		 "entity enters " + vehicleType,
        		 entityType + " enters " + vehicleType),
        		null, player, context);
        
        if (determination.toUpperCase().startsWith("CANCELLED"))
            event.setCancelled(true);
    }
    
    @EventHandler
    public void vehicleExit(VehicleExitEvent event) {
    	    	
        Map<String, Object> context = new HashMap<String, Object>();
        
    	Entity entity = event.getExited();
    	Vehicle vehicle = event.getVehicle();
        
    	String entityType = entity.getType().name();
    	String vehicleType = vehicle.getType().name();

    	Player player = null;
    	
    	context.put("vehicle", new dEntity(vehicle));
    	
    	if (entity instanceof Player) {
    		context.put("entity", new dPlayer((Player) entity));
    		player = (Player) entity;
    	}
    	else {
    		context.put("entity", new dEntity(entity));
    	}
    	
        String determination = doEvents(Arrays.asList
        		("entity exits vehicle",
        		 entityType + " exits vehicle",
        		 "entity exits " + vehicleType,
        		 entityType + " exits " + vehicleType),
        		null, player, context);
        
        if (determination.toUpperCase().startsWith("CANCELLED"))
            event.setCancelled(true);
    }
    

    /////////////////////
    //   WEATHER EVENTS
    /////////////////
    
    @EventHandler
    public void lightningStrike(LightningStrikeEvent event) {
    	
        Map<String, Object> context = new HashMap<String, Object>();
        String world = event.getWorld().getName();
        context.put("world", new dWorld(event.getWorld()));
        context.put("location", new dLocation(event.getLightning().getLocation()));
        
        String determination = doEvents(Arrays.asList
        		("lightning strikes",
        		 "lightning strikes in " + world),
        		null, null, context);
        
        if (determination.toUpperCase().startsWith("CANCELLED"))
            event.setCancelled(true);
    }
    
    @EventHandler
    public void weatherChange(WeatherChangeEvent event) {
    	
        Map<String, Object> context = new HashMap<String, Object>();
        String world = event.getWorld().getName();
        context.put("world", new dWorld(event.getWorld()));
        
        List<String> events = new ArrayList<String>();
        events.add("weather changes");
        events.add("weather changes in " + world);
        
        if (event.toWeatherState() == true) {
        	context.put("weather", new Element("rain"));
        	events.add("weather rains");
        	events.add("weather rains in " + world);
        }
        else {
        	context.put("weather", new Element("clear"));
        	events.add("weather clears");
        	events.add("weather clears in " + world);
        }
        
        String determination = doEvents(events, null, null, context);
        
        if (determination.toUpperCase().startsWith("CANCELLED"))
            event.setCancelled(true);
    }
}
