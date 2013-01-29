package net.aufdemrand.denizen.tags.core;

import net.aufdemrand.denizen.Denizen;
import net.aufdemrand.denizen.events.ReplaceableTagEvent;
import net.aufdemrand.denizen.npc.dNPC;
import net.aufdemrand.denizen.npc.traits.NicknameTrait;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

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

        } else if (type.equals("ID")) {
            event.setReplaced(String.valueOf(event.getNPC().getId()));

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
            else if (subType.equals("WORLD"))
                event.setReplaced(n.getWorld().getName());
            else if (subType.equals("BIOME"))
                event.setReplaced(n.getLocation().getBlock().getBiome().name());
            else if (subType.equals("BIOME_DISPLAY"))
            	event.setReplaced(n.getLocation().getBlock().getBiome().name().toLowerCase().replace('_', ' '));
            else if (subType.equals("STANDING_ON"))
                event.setReplaced(loc.add(0, -1, 0).getBlock().getType().name());
            else if (subType.equals("STANDING_ON_DISPLAY"))
            	event.setReplaced(n.getLocation().add(0, -1, 0).getBlock().getType().name().toLowerCase().replace('_', ' '));
            else if (subType.equals("LIGHT"))
                event.setReplaced(String.valueOf((int) n.getLocation().getBlock().getLightLevel()));
            else if (subType.equals("LIGHT_BLOCKS"))
                event.setReplaced(String.valueOf((int) n.getLocation().getBlock().getLightFromBlocks()));
            else if (subType.equals("LIGHT_SKY"))
                event.setReplaced(String.valueOf((int) n.getLocation().getBlock().getLightFromSky()));
            else if (subType.equals("WORLD_SPAWN"))
                event.setReplaced(n.getWorld().getSpawnLocation().getBlockX()
                        + "," + n.getWorld().getSpawnLocation().getBlockY()
                        + "," + n.getWorld().getSpawnLocation().getBlockZ()
                        + "," + n.getWorld().getName());
            else if (subType.equals("WORLD"))
                event.setReplaced(n.getWorld().getName());


        } else if (type.equals("NAVIGATOR")) {
            if (subType.equals("ISNAVIGATING"))
                event.setReplaced(Boolean.toString(n.getNavigator().isNavigating()));
            else if (subType.equals("SPEED"))
                event.setReplaced(String.valueOf(n.getNavigator().getLocalParameters().speedModifier()));
            else if (subType.equals("AVOID_WATER"))
                event.setReplaced(Boolean.toString(n.getNavigator().getLocalParameters().avoidWater()));
            else if (subType.equals("TARGET_LOCATION")) {
                Location loc = n.getNavigator().getTargetAsLocation();
                if (loc != null)
                    event.setReplaced(loc.getBlockX()
                            + "," + loc.getBlockY()
                            + "," + loc.getBlockZ()
                            + "," + n.getWorld().getName());
            }
        }
    }


}