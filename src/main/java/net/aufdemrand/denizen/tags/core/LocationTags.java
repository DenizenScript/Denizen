package net.aufdemrand.denizen.tags.core;

import net.aufdemrand.denizen.Denizen;
import net.aufdemrand.denizen.events.ReplaceableTagEvent;
import net.aufdemrand.denizen.utilities.Utilities;
import net.aufdemrand.denizen.objects.dLocation;
import net.aufdemrand.denizen.objects.aH;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;


public class LocationTags implements Listener {

    public LocationTags(Denizen denizen) {
        denizen.getServer().getPluginManager().registerEvents(this, denizen);
    }

    @EventHandler
    public void locationTags(ReplaceableTagEvent event) {
        if (!event.matches("location")) return;

        String nameContext = event.getNameContext() != null ? event.getNameContext().toUpperCase() : "";
        String type = event.getType() != null ? event.getType().toUpperCase() : "";
        String typeContext = event.getTypeContext() != null ? event.getTypeContext().toUpperCase() : "";
        String subType = event.getSubType() != null ? event.getSubType().toUpperCase() : "";
        String specifier = event.getSpecifier() != null ? event.getSpecifier().toUpperCase() : "";
        
        dLocation fromLocation = null;
        dLocation toLocation = null;
        
        if (aH.matchesLocation("location:" + nameContext))
        	fromLocation = aH.getLocationFrom("location:" + nameContext);
        else if (event.getPlayer() != null)
        	fromLocation = new dLocation(event.getPlayer().getPlayerEntity().getLocation());
        else
        	fromLocation = new dLocation(event.getNPC().getLocation());
        
        if (aH.matchesLocation("location:" + typeContext))
        	toLocation = aH.getLocationFrom("location:" + typeContext);
        
        if (type.equals("BIOME"))
        {
            if (subType.equals("FORMATTED"))
                event.setReplaced(fromLocation.getBlock().getBiome().name().toLowerCase().replace('_', ' '));
            else if (subType.equals("HUMIDITY"))
            	event.setReplaced(String.valueOf(fromLocation.getBlock().getHumidity()));
            else if (subType.equals("TEMPERATURE"))
            	event.setReplaced(String.valueOf(fromLocation.getBlock().getTemperature()));
            else
                event.setReplaced(fromLocation.getBlock().getBiome().name());
        }
        
        else if (type.equals("BLOCK"))
        {
            if (subType.equals("BELOW"))
            {
                fromLocation = new dLocation(fromLocation.add(0, -1, 0));
            }

            else if (subType.equals("MATERIAL") || specifier.equals("MATERIAL"))
            {
                event.setReplaced(fromLocation.getBlock().getType().toString());
            }

            else if (subType.equals("DATA") || specifier.equals("DATA"))
            {
                event.setReplaced(String.valueOf(fromLocation.getBlock().getData()));
            }
        }
        
        else if (type.equals("DIRECTION"))
        {
        	if (fromLocation != null && toLocation != null)
        	{
        		event.setReplaced(Utilities.getCardinal(Utilities.getYaw
            				 	 (toLocation.toVector().subtract
            				 	 (fromLocation.toVector()).normalize())));
        	}
        }

        else if (type.equals("DISTANCE"))
        {
        	if (fromLocation != null && toLocation != null)
        	{
        		if (subType.equals("ASINT"))
        		{
        			event.setReplaced(String.valueOf((int)fromLocation.distance(toLocation)));
        		}
        		else if (subType.equals("VERTICAL"))
        		{
        			if (fromLocation.getWorld().getName() == toLocation.getWorld().getName()
        				|| specifier.equals("MULTIWORLD"))
        			{
        				// Only calculate distance between locations on different worlds
        				// if the MULTIWORLD specifier is used
        				event.setReplaced(String.valueOf(Math.abs(
        						fromLocation.getY() - toLocation.getY())));
        			}
        		}
        		else if (subType.equals("HORIZONTAL"))
        		{
        			if (fromLocation.getWorld().getName() == toLocation.getWorld().getName()
        				|| specifier.equals("MULTIWORLD"))
        			{
        				// Only calculate distance between locations on different worlds
        				// if the MULTIWORLD specifier is used
        				event.setReplaced(String.valueOf(Math.sqrt(
        						Math.pow(fromLocation.getX() - toLocation.getX(), 2) +
        						Math.pow(fromLocation.getZ() - toLocation.getZ(), 2))));
        			}
        		}
        		else
        			event.setReplaced(String.valueOf(fromLocation.distance(toLocation)));
        	}
        }
        
        else if (type.equals("FORMATTED"))       
            event.setReplaced("X '" + fromLocation.getX()
                    + "', Y '" + fromLocation.getY()
                    + "', Z '" + fromLocation.getZ()
                    + "', in world '" + fromLocation.getWorld().getName() + "'");
        
        else if (type.equals("IS_LIQUID"))
        {
        	event.setReplaced(String.valueOf(fromLocation.getBlock().isLiquid()));
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
        
        else if (type.equals("POWER"))
        {
        	event.setReplaced(String.valueOf((int) fromLocation.getBlock().getBlockPower()));
        }
        
        else if (type.equals("TIME"))
        {   
            if (subType.equals("PERIOD"))
            	if (fromLocation.getWorld().getTime() < 13500 ||
            		fromLocation.getWorld().getTime() > 23000) 
            		event.setReplaced("day");
            	else if (fromLocation.getWorld().getTime() > 13500)
            		event.setReplaced("night");
        }
        
        else if (type.equals("WORLD"))      
        	event.setReplaced(fromLocation.getWorld().getName());
        
        else if (type.equals("X"))         
        	event.setReplaced(String.valueOf(fromLocation.getX()));
        
        else if (type.equals("Y"))         
        	event.setReplaced(String.valueOf(fromLocation.getY()));
        
        else if (type.equals("Z"))          
        	event.setReplaced(String.valueOf(fromLocation.getZ()));

    }

}