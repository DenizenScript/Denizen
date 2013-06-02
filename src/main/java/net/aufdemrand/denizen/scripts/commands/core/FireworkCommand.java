package net.aufdemrand.denizen.scripts.commands.core;

import java.util.ArrayList;
import java.util.List;

import net.aufdemrand.denizen.exceptions.CommandExecutionException;
import net.aufdemrand.denizen.exceptions.InvalidArgumentsException;
import net.aufdemrand.denizen.scripts.ScriptEntry;
import net.aufdemrand.denizen.scripts.commands.AbstractCommand;
import net.aufdemrand.denizen.utilities.arguments.dLocation;
import net.aufdemrand.denizen.utilities.arguments.aH;
import net.aufdemrand.denizen.utilities.arguments.aH.ArgumentType;
import net.aufdemrand.denizen.utilities.debugging.dB;
import net.aufdemrand.denizen.utilities.debugging.dB.Messages;

import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.FireworkEffect.Builder;
import org.bukkit.FireworkEffect.Type;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Firework;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.meta.FireworkMeta;

/**
 * Shoots a firework into the air from a location.
 * If no location is chosen, the firework is shot from the NPC's location.
 *
 * @author David Cernat
 */

public class FireworkCommand extends AbstractCommand {
	
	public enum EquipType { HAND, BOOTS, LEGS, CHEST, HEAD }
	
    @Override
    public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {

        // Initialize necessary fields
        dLocation location = null;
        Type type = Type.BALL;
        Integer power = 1;
        Boolean flicker = false;
        Boolean trail = false;
        List<String> effects = new ArrayList<String>();

        for (String arg : scriptEntry.getArguments()) {
            if (aH.matchesLocation(arg)) {
                location = aH.getLocationFrom(arg);
                dB.echoDebug("...location set to '%s'.", arg);

            } else if (aH.matchesValueArg("type", arg, ArgumentType.String)) {
            	//type = aH.getTypeFrom(arg);
                dB.echoDebug("...will have a power of " + power);
            
            } else if (aH.matchesValueArg("power", arg, ArgumentType.Integer)) {
            	power = aH.getIntegerFrom(arg);
                dB.echoDebug("...will have a power of " + power);
            
            } else if (aH.matchesArg("flicker", arg)) {
                flicker = true;
                dB.echoDebug("...will flicker.");
                
            } else if (aH.matchesArg("trail", arg)) {
                trail = true;
                dB.echoDebug("...will have a trail.");
                
            } else if (aH.matchesValueArg("EFFECTS, EFFECT", arg, ArgumentType.String)) {
            	// May be multiple effects, so let's treat this as a potential list.
                // dScript list entries are separated by pipes ('|')
                for (String element : aH.getListFrom(arg)) {
                	
                	String[] effect = element.split(";", 5);
                	
                	dB.echoApproval("Element: " + element);
                	dB.echoApproval("Effect length: " + effect.length);
                	
                	if (effect.length == 2) {
                	
                		//if (effect[1])
                		
                		//for (FireworkEffect.Type type : FireworkEffect.Type.values()) {
                	        
                			dB.echoApproval("Looking at: " + type.name());
                			
                			if (type.name().equals(effect[1])) {
                				dB.echoApproval("Effect types matched!");
                				break;
                	        }
                	    //}
                		
                		dB.echoApproval("Effect " + element + " seems valid!");
                           
                    }
                    else  {
                    	dB.echoError("Invalid firework EFFECT!");
                    }
                }
            } else throw new InvalidArgumentsException(Messages.ERROR_UNKNOWN_ARGUMENT, arg);
        }

        // Stash objects
        scriptEntry.addObject("location", location);
        scriptEntry.addObject("power", power);
        scriptEntry.addObject("flicker", flicker);
        scriptEntry.addObject("trail", trail);
    }
    
	@Override
    public void execute(final ScriptEntry scriptEntry) throws CommandExecutionException {
        // Get objects
    	
        final dLocation location = scriptEntry.hasObject("location") ?
        		                   (dLocation) scriptEntry.getObject("location") :
        		                   (dLocation) scriptEntry.getNPC().getLocation();
        Integer power = (Integer) scriptEntry.getObject("power");
        Boolean flicker = (Boolean) scriptEntry.getObject("flicker");
        Boolean trail = (Boolean) scriptEntry.getObject("trail");
        Type type = (Type) scriptEntry.getObject("type");
        
        Firework firework = location.getWorld().spawn(location, Firework.class);
        FireworkMeta fireworkMeta = (FireworkMeta) firework.getFireworkMeta();
        fireworkMeta.setPower(power);
        
        Builder fireworkBuilder = FireworkEffect.builder();
        
        fireworkBuilder.with(type);
		//fireworkBuilder.withColor(arg0);
		//fireworkBuilder.withFade(arg0);
        
        if (flicker) { fireworkBuilder.withFlicker(); }
        if (trail) { fireworkBuilder.withTrail(); }
        
        //fireworkBuilder.
        
        //fireworkMeta.addEffects(FireworkEffect.builder().withColor(Color.YELLOW).with(Type.STAR).build());
        fireworkMeta.addEffects(fireworkBuilder.build());
        firework.setFireworkMeta(fireworkMeta);

    }

}