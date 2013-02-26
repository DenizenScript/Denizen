package net.aufdemrand.denizen.tags.core;

import net.aufdemrand.denizen.Denizen;
import net.aufdemrand.denizen.events.ReplaceableTagEvent;
import net.aufdemrand.denizen.npc.dNPC;
import net.aufdemrand.denizen.npc.traits.AssignmentTrait;
import net.aufdemrand.denizen.npc.traits.NicknameTrait;
import net.aufdemrand.denizen.utilities.DenizenAPI;
import net.aufdemrand.denizen.utilities.arguments.Location;
import net.citizensnpcs.api.ai.event.NavigationBeginEvent;
import net.citizensnpcs.api.ai.event.NavigationCancelEvent;
import net.citizensnpcs.api.ai.event.NavigationCompleteEvent;
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
        String type = event.getType() != null ? event.getType().toUpperCase() : "";
        String subType = event.getSubType() != null ? event.getSubType().toUpperCase() : "";

        if (type.equals("NAME")) {
            event.setReplaced(n.getName());
            if (subType.equals("NICKNAME")) {
                if (n.getCitizen().hasTrait(NicknameTrait.class))
                    event.setReplaced(n.getCitizen().getTrait(NicknameTrait.class).getNickname());
            }
            
        } else if (type.equals("HEALTH")) {
        	
        	if (subType.equals("MAX"))
        		event.setReplaced(String.valueOf(n.getHealthTrait().getMaxhealth()));
        	else
        		event.setReplaced(String.valueOf(n.getHealthTrait().getHealth()));
            
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
            Location loc = n.getLocation();
            event.setReplaced(loc.getBlockX()
                    + "," + loc.getBlockY()
                    + "," + loc.getBlockZ()
                    + "," + n.getWorld().getName());
            if (subType.equals("FORMATTED"))
                event.setReplaced("X '" + loc.getBlockX()
                        + "', Y '" + loc.getBlockY()
                        + "', Z '" + loc.getBlockZ()
                        + "', in world '" + n.getWorld().getName() + "'");
            else if (subType.equals("X"))
                event.setReplaced(String.valueOf(n.getLocation().getBlockX()));
            else if (subType.equals("Y"))
                event.setReplaced(String.valueOf(n.getLocation().getBlockY()));
            else if (subType.equals("Z"))
                event.setReplaced(String.valueOf(n.getLocation().getBlockZ()));
            else if (subType.equals("STANDING_ON"))
                event.setReplaced(loc.add(0, -1, 0).getBlock().getType().name());
            else if (subType.equals("STANDING_ON_DISPLAY"))
            	event.setReplaced(n.getLocation().add(0, -1, 0).getBlock().getType().name().toLowerCase().replace('_', ' '));
            else if (subType.equals("WORLD_SPAWN"))
                event.setReplaced(n.getWorld().getSpawnLocation().getBlockX()
                        + "," + n.getWorld().getSpawnLocation().getBlockY()
                        + "," + n.getWorld().getSpawnLocation().getBlockZ()
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
                Location loc = new Location(n.getNavigator().getTargetAsLocation());
                if (loc != null) event.setReplaced(loc.dScriptArgValue());
            } else if (subType.equals("IS_FIGHTING")) {
                event.setReplaced(String.valueOf(event.getNPC().getNavigator().getEntityTarget().isAggressive()));
            } else if (subType.equals("TARGET_TYPE")) {
                event.setReplaced(event.getNPC().getNavigator().getTargetType().toString());
            }

        }

    }

    private Map<Integer, Location> previousLocations = new HashMap<Integer, Location>();

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
        previousLocations.put(event.getNPC().getId(), npc.getLocation());
    }

    @EventHandler
    public void navCancel(NavigationCancelEvent event) {
        if (!event.getNPC().hasTrait(AssignmentTrait.class)) return;
        dNPC npc = DenizenAPI.getDenizenNPC(event.getNPC());
        npc.action("cancel navigation", null);
        npc.action("cancel navigation due to " + event.getCancelReason().toString(), null);
    }

}