package net.aufdemrand.denizen.tags.core;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;
import java.util.UUID;

import net.aufdemrand.denizen.Denizen;
import net.aufdemrand.denizen.events.ReplaceableTagEvent;
import net.aufdemrand.denizen.tags.Attribute;
import net.aufdemrand.denizen.utilities.Utilities;
import net.aufdemrand.denizen.objects.Element;
import net.aufdemrand.denizen.objects.aH;
import net.aufdemrand.denizen.objects.dList;


import net.aufdemrand.denizen.utilities.javaluator.DoubleEvaluator;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class UtilTags implements Listener {

    public UtilTags(Denizen denizen) {
        denizen.getServer().getPluginManager().registerEvents(this, denizen);
    }

    @EventHandler
    public void mathTags(ReplaceableTagEvent event) {
        if (!(event.matches("m") || event.matches("math"))) return;
        Double evalulation = new DoubleEvaluator().evaluate(event.getValue());
        event.setReplaced(String.valueOf(evalulation));
    }

    @EventHandler
    public void utilTags(ReplaceableTagEvent event) {
        if (!event.matches("UTIL, U")) return;

        String type = event.getType() != null ? event.getType() : "";
        String typeContext = event.getTypeContext() != null ? event.getTypeContext() : "";
        String subType = event.getSubType() != null ? event.getSubType() : "";
        String subTypeContext = event.getSubTypeContext() != null ? event.getSubTypeContext().toUpperCase() : "";
        String specifier = event.getSpecifier() != null ? event.getSpecifier() : "";
        String specifierContext = event.getSpecifierContext() != null ? event.getSpecifierContext().toUpperCase() : "";
        
        if (type.equalsIgnoreCase("RANDOM")) {
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

                        event.setReplaced(String.valueOf(Utilities.getRandom().nextInt(max - min + 1) + min));
                    }
                }
            }
            
            else if (subType.equalsIgnoreCase("ELEMENT")) {
            	dList list = dList.valueOf(subTypeContext);
            	event.setReplaced(list.get(new Random().nextInt(list.size())));
            }

            else if (subType.equalsIgnoreCase("UUID"))
                event.setReplaced(UUID.randomUUID().toString());
        }

        else if (type.equalsIgnoreCase("SUBSTR")
                || type.equalsIgnoreCase("TRIM")
                || type.equalsIgnoreCase("SUBSTRING")) {
            String text = event.getTypeContext();
            int from = 1;
            int to = text.length() + 1;
            
            if (subType.equalsIgnoreCase("AFTER")) {
                from = text.toUpperCase().indexOf(subTypeContext) + subTypeContext.length() + 1;
            }
            
            if (subType.equalsIgnoreCase("BEFORE")) {
                to = text.toUpperCase().indexOf(subTypeContext) + 1;
            }
            
            try {
                if (subType.equalsIgnoreCase("FROM"))
                    from = Integer.valueOf(subTypeContext);
            } catch (NumberFormatException e) { }
            
            try {
                if (specifier.equalsIgnoreCase("TO"))
                    to = Integer.valueOf(specifierContext);
            } catch (NumberFormatException e) { }

            if (to > text.length())
                to = text.length() + 1;

            event.setReplaced(text.substring(from - 1, to - 1));
        }

        else if (type.equalsIgnoreCase("REPLACE")) {
            String item_to_replace = event.getTypeContext();
            String replace = event.getSubTypeContext();
            String replacement = event.getSpecifierContext();
            event.setReplaced(item_to_replace.replace(replace, replacement));
        }

        else if (type.equalsIgnoreCase("UPPERCASE")) {
            String item_to_uppercase = event.getTypeContext();
            event.setReplaced(item_to_uppercase.toUpperCase());
        }

        else if (type.equalsIgnoreCase("LOWERCASE")) {
            String item_to_uppercase = event.getTypeContext();
            event.setReplaced(item_to_uppercase.toLowerCase());
        }
        
        else if (type.equalsIgnoreCase("DATE")) {
        	Date currentDate = new Date();
        	SimpleDateFormat format = new SimpleDateFormat();
        	
        	if (subType.equalsIgnoreCase("TIME")) {
        		if (specifier.equalsIgnoreCase("24HOUR")) {
        			format.applyPattern("k:mm");
        		} else format.applyPattern("K:mm a");
        		
        	} else format.applyPattern("EEE, MMM d, yyyy");
        	
        	event.setReplaced(format.format(currentDate));
        }

        else if (type.equalsIgnoreCase("AS_ELEMENT")) {
            Attribute attribute = new Attribute(event.raw_tag, event.getScriptEntry());
            event.setReplaced(new Element(typeContext).getAttribute(attribute.fulfill(2)));
        }

    }

}