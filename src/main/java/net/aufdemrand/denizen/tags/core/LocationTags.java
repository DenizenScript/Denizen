package net.aufdemrand.denizen.tags.core;

import net.aufdemrand.denizen.Denizen;
import net.aufdemrand.denizen.events.ReplaceableTagEvent;
import net.aufdemrand.denizen.utilities.arguments.Location;
import net.aufdemrand.denizen.utilities.arguments.aH;
import org.bukkit.util.Vector;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;


public class LocationTags implements Listener {

    public LocationTags(Denizen denizen) {
        denizen.getServer().getPluginManager().registerEvents(this, denizen);
    }
    
    /**
     * Converts a vector to a yaw
     * 
     * Thanks to bergerkiller
     * 
     * @param rot
     * @return
     */
    
    public static float getYaw(Vector vector) {
        double dx = vector.getX();
        double dz = vector.getZ();
        double yaw = 0;
        // Set yaw
        if (dx != 0) {
            // Set yaw start value based on dx
            if (dx < 0) {
                yaw = 1.5 * Math.PI;
            } else {
                yaw = 0.5 * Math.PI;
            }
            yaw -= Math.atan(dz / dx);
        } else if (dz < 0) {
            yaw = Math.PI;
        }
        return (float) (-yaw * 180 / Math.PI - 90);
    }
    
    /**
     * Converts a rotation to a cardinal direction name.
     * 
     * Thanks to sk89qs
     * 
     * @param rot
     * @return
     */
    private static String getCardinal(double yaw) {
    
    	yaw = (yaw - 90) % 360;
    	if (yaw < 0)
            yaw += 360.0;
    	
        if (0 <= yaw && yaw < 22.5) {
            return "North";
        } else if (22.5 <= yaw && yaw < 67.5) {
            return "Northeast";
        } else if (67.5 <= yaw && yaw < 112.5) {
            return "East";
        } else if (112.5 <= yaw && yaw < 157.5) {
            return "Southeast";
        } else if (157.5 <= yaw && yaw < 202.5) {
            return "South";
        } else if (202.5 <= yaw && yaw < 247.5) {
            return "Southwest";
        } else if (247.5 <= yaw && yaw < 292.5) {
            return "West";
        } else if (292.5 <= yaw && yaw < 337.5) {
            return "Northwest";
        } else if (337.5 <= yaw && yaw < 360.0) {
            return "North";
        } else {
            return null;
        }
    }

    @EventHandler
    public void locationTags(ReplaceableTagEvent event) {
        if (!event.matches("location")) return;

        String nameContext = event.getNameContext() != null ? event.getNameContext().toUpperCase() : "";
        String type = event.getType() != null ? event.getType().toUpperCase() : "";
        String subType = event.getSubType() != null ? event.getSubType().toUpperCase() : "";
        String typeContext = event.getTypeContext() != null ? event.getTypeContext().toUpperCase() : "";
        Location fromLocation = null;
        Location toLocation = null;
        
        if (aH.matchesLocation("location:" + nameContext))
        {
        	fromLocation = aH.getLocationFrom("location:" + nameContext);
        }
        else if (event.getPlayer() != null)
        {
        	fromLocation = new Location(event.getPlayer().getLocation());
        }
        else
        {
        	fromLocation = new Location (event.getNPC().getLocation());
        }
        
        if (aH.matchesLocation("location:" + typeContext))
        {
        	toLocation = aH.getLocationFrom("location:" + typeContext);
        }
        
        if (type.equals("BIOME"))
        {            
            if (subType.equals("DISPLAY"))
                event.setReplaced(fromLocation.getBlock().getBiome().name().toLowerCase().replace('_', ' '));
            else
                event.setReplaced(fromLocation.getBlock().getBiome().name());
        }
        
        else if (type.equals("DIRECTION"))
        {
            event.setReplaced(getCardinal(getYaw
            				 (toLocation.toVector().subtract
            				 (fromLocation.toVector()).normalize())));
            
        }
        
        else if (type.equals("DISTANCE"))
        {
        	event.setReplaced(String.valueOf(fromLocation.distance(toLocation)));
        }
        
        else if (type.equals("FORMATTED"))
        {            
            event.setReplaced("X '" + fromLocation.getBlockX()
                    + "', Y '" + fromLocation.getBlockY()
                    + "', Z '" + fromLocation.getBlockZ()
                    + "', in world '" + fromLocation.getWorld().getName() + "'");
        }
        
        else if (type.equals("LIGHT"))
        {
        	if (subType.equals("BLOCKS"))
        		event.setReplaced(String.valueOf((int) fromLocation.getBlock().getLightFromBlocks()));
        	else if (subType.equals("SKY"))
        		event.setReplaced(String.valueOf((int) fromLocation.getBlock().getLightFromSky()));
        	else
        		event.setReplaced(String.valueOf((int) fromLocation.getBlock().getLightLevel()));
        }
        
        else if (type.equals("WORLD"))
        {            
        	event.setReplaced(fromLocation.getWorld().getName());
        }
        
        else if (type.equals("X"))
        {            
        	event.setReplaced(String.valueOf(fromLocation.getBlockX()));
        }
        
        else if (type.equals("Y"))
        {            
        	event.setReplaced(String.valueOf(fromLocation.getBlockY()));
        }
        
        else if (type.equals("Z"))
        {            
        	event.setReplaced(String.valueOf(fromLocation.getBlockZ()));
        }

    }

}