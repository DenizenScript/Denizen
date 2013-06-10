package net.aufdemrand.denizen.tags.core;

import net.aufdemrand.denizen.Denizen;
import net.aufdemrand.denizen.events.ReplaceableTagEvent;
import net.aufdemrand.denizen.npc.dNPC;
import net.aufdemrand.denizen.npc.traits.AssignmentTrait;
import net.aufdemrand.denizen.npc.traits.NicknameTrait;
import net.aufdemrand.denizen.utilities.DenizenAPI;
import net.aufdemrand.denizen.utilities.Utilities;
import net.aufdemrand.denizen.utilities.arguments.aH;
import net.aufdemrand.denizen.utilities.arguments.dLocation;
import net.citizensnpcs.api.ai.event.NavigationBeginEvent;
import net.citizensnpcs.api.ai.event.NavigationCancelEvent;
import net.citizensnpcs.api.ai.event.NavigationCompleteEvent;

import org.bukkit.ChatColor;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.HashMap;
import java.util.Map;

public class NPCTags implements Listener {

    public NPCTags(Denizen denizen) {
        denizen.getServer().getPluginManager().registerEvents(this, denizen);
    }

    @EventHandler
    public void npcTags(ReplaceableTagEvent event) {
        if (!event.matches("npc")) return;

        dNPC n = event.getNPC();
        if (n == null) return; // to avoid exceptions in scripts with no NPC attached
        
        String type = event.getType() != null ? event.getType().toUpperCase() : "";
        String typeContext = event.getTypeContext() != null ? event.getTypeContext() : "";
        String subType = event.getSubType() != null ? event.getSubType().toUpperCase() : "";
        
        if (type.equals("NAME")) {
            event.setReplaced(ChatColor.stripColor(n.getName()));
            if (subType.equals("NICKNAME")) {
                if (n.getCitizen().hasTrait(NicknameTrait.class))
                    event.setReplaced(n.getCitizen().getTrait(NicknameTrait.class).getNickname());
            }    
        }
        
        else if (type.equalsIgnoreCase("CLOSEST"))
        {
            int range = 100;

            if (aH.matchesInteger(typeContext))
                range = aH.getIntegerFrom(typeContext);

            if (subType.equalsIgnoreCase("PLAYER")) {
                event.setReplaced(String.valueOf(Utilities.getClosestPlayer(n.getLocation(), range).getName()));
            }
        }
        
        else if (type.equals("HEALTH")) {
        	
        	if (subType.equals("MAX"))
        		event.setReplaced(String.valueOf(n.getCitizen().getBukkitEntity().getMaxHealth()));
        	else
        		
        		event.setReplaced(String.valueOf(n.getCitizen().getBukkitEntity().getHealth()));
            
        } else if (type.equals("TYPE")) {
        	if (subType.equals("FORMATTED"))
        		event.setReplaced(String.valueOf(n.getEntityType().name().toLowerCase().replace('_', ' ')));
        	else
        		event.setReplaced(String.valueOf(n.getEntityType().name()));

        } else if (type.equals("ID")) {
            event.setReplaced(String.valueOf(n.getId()));
        
        } else if (type.equals("OWNER")) {
            event.setReplaced(String.valueOf(n.getOwner()));

        } else if (type.equals("LOCATION")) {
            dLocation loc = n.getLocation();
            event.setReplaced(loc.getX()
                    + "," + loc.getY()
                    + "," + loc.getZ()
                    + "," + n.getWorld().getName());
            if (subType.equals("BLOCK"))
                event.setReplaced(loc.getBlockX()
                        + "," + loc.getBlockY()
                        + "," + loc.getBlockZ()
                        + "," + n.getWorld().getName());
            else if (subType.equals("FORMATTED"))
                event.setReplaced("X '" + loc.getX()
                        + "', Y '" + loc.getY()
                        + "', Z '" + loc.getZ()
                        + "', in world '" + n.getWorld().getName() + "'");
            else if (subType.equals("X"))
                event.setReplaced(String.valueOf(n.getLocation().getX()));
            else if (subType.equals("Y"))
                event.setReplaced(String.valueOf(n.getLocation().getY()));
            else if (subType.equals("Z"))
                event.setReplaced(String.valueOf(n.getLocation().getZ()));
            else if (subType.equals("STANDING_ON"))
                event.setReplaced(loc.add(0, -1, 0).getBlock().getType().name());
            else if (subType.equals("STANDING_ON_DISPLAY"))
            	event.setReplaced(n.getLocation().add(0, -1, 0).getBlock().getType().name().toLowerCase().replace('_', ' '));
            else if (subType.equals("WORLD_SPAWN"))
                event.setReplaced(n.getWorld().getSpawnLocation().getX()
                        + "," + n.getWorld().getSpawnLocation().getY()
                        + "," + n.getWorld().getSpawnLocation().getZ()
                        + "," + n.getWorld().getName());
            else if (subType.equals("WORLD"))
                event.setReplaced(n.getWorld().getName());
            else if (subType.equals("PREVIOUS_LOCATION"))
                if (previousLocations.containsKey(n.getId()))
                    event.setReplaced(previousLocations.get(n.getId()).dScriptArgValue());

        } else if (type.equals("NAVIGATOR")) {
            if (subType.equals("IS_NAVIGATING"))
                event.setReplaced(Boolean.toString(n.getNavigator().isNavigating()));
            else if (subType.equals("SPEED"))
                event.setReplaced(String.valueOf(n.getNavigator().getLocalParameters().speedModifier()));
            else if (subType.equals("AVOID_WATER"))
                event.setReplaced(Boolean.toString(n.getNavigator().getLocalParameters().avoidWater()));
            else if (subType.equals("TARGET_LOCATION")) {
                dLocation loc = new dLocation(n.getNavigator().getTargetAsLocation());
                if (loc != null) event.setReplaced(loc.dScriptArgValue());
            } else if (subType.equals("IS_FIGHTING")) {
                event.setReplaced(String.valueOf(event.getNPC().getNavigator().getEntityTarget().isAggressive()));
            } else if (subType.equals("TARGET_TYPE")) {
                event.setReplaced(event.getNPC().getNavigator().getTargetType().toString());
            }

        }

    }

    private Map<Integer, dLocation> previousLocations = new HashMap<Integer, dLocation>();

    @EventHandler
    public void navComplete(NavigationCompleteEvent event) {
        if (!event.getNPC().hasTrait(AssignmentTrait.class)) return;
        dNPC npc = DenizenAPI.getDenizenNPC(event.getNPC());
        npc.action("complete navigation", null);
    }

    @EventHandler
    public void navBegin(NavigationBeginEvent event) {
        if (!event.getNPC().hasTrait(AssignmentTrait.class)) return;
        dNPC npc = DenizenAPI.getDenizenNPC(event.getNPC());
        npc.action("begin navigation", null);

        if (event.getNPC().getNavigator().getTargetType().toString() == "ENTITY")
        {
        	LivingEntity entity = event.getNPC().getNavigator().getEntityTarget().getTarget();
        	
        	// If the NPC has an entity target, is aggressive towards it
        	// and that entity is not dead, trigger "on attack" command
        	if (event.getNPC().getNavigator().getEntityTarget().isAggressive()
        		&& entity.isDead() == false)
        	{
        		Player player = null;
        	
        		// Check if the entity attacked by this NPC is a player
        		if (entity instanceof Player)
        			player = (Player) entity;
        		
        		npc.action("attack", player);
        	
        		npc.action("attack on "
        				+ entity.getType().toString(), player);  
        	}
        	previousLocations.put(event.getNPC().getId(), npc.getLocation());
        }
    }

    @EventHandler
    public void navCancel(NavigationCancelEvent event) {
        if (!event.getNPC().hasTrait(AssignmentTrait.class)) return;
        dNPC npc = DenizenAPI.getDenizenNPC(event.getNPC());
        npc.action("cancel navigation", null);
        npc.action("cancel navigation due to " + event.getCancelReason().toString(), null);
    }

}