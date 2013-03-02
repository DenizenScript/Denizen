package net.aufdemrand.denizen.tags.core;

import net.aufdemrand.denizen.Denizen;
import net.aufdemrand.denizen.events.ReplaceableTagEvent;
import net.aufdemrand.denizen.utilities.arguments.aH;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.Random;
import java.util.UUID;

public class UtilTags implements Listener {

    public UtilTags(Denizen denizen) {
        denizen.getServer().getPluginManager().registerEvents(this, denizen);
    }

   @EventHandler
    public void utilTags(ReplaceableTagEvent event) {
    if (!event.matches("UTIL")) return;
    
    String type = event.getType() != null ? event.getType() : "";
    String subType = event.getSubType() != null ? event.getSubType() : "";
    String subTypeContext = event.getSubTypeContext() != null ? event.getSubTypeContext().toUpperCase() : "";
    String specifier = event.getSpecifier() != null ? event.getSpecifier() : "";
    String specifierContext = event.getSpecifierContext() != null ? event.getSpecifierContext().toUpperCase() : "";

       if (type.equalsIgnoreCase("RANDOM"))
    	   if (subType.equalsIgnoreCase("INT")) {
    		   if (specifier.equalsIgnoreCase("TO")) {
    			   if (aH.matchesInteger(subTypeContext) && aH.matchesInteger(specifierContext)) {
    				   int min = aH.getIntegerFrom(subTypeContext);
    				   int max = aH.getIntegerFrom(specifierContext);

                       // in case the first number is larger than the second, reverse them
    				   if (min > max) {
    					   int store = min;
    					   min = max;
    					   max = store;
    				   }
    				   
    				   Random rand = new Random();
    				   event.setReplaced(String.valueOf(rand.nextInt(max - min + 1) + min));
    			   }
    		   }
    	   }

    	   else if (subType.equalsIgnoreCase("UUID"))
    		   event.setReplaced(UUID.randomUUID().toString());

   }
    
}