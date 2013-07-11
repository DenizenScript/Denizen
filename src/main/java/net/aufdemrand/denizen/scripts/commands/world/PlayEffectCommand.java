package net.aufdemrand.denizen.scripts.commands.world;

import org.bukkit.Effect;
import org.bukkit.EntityEffect;
import org.bukkit.craftbukkit.v1_6_R2.entity.CraftWolf;
import org.bukkit.entity.Wolf;

import net.aufdemrand.denizen.exceptions.CommandExecutionException;
import net.aufdemrand.denizen.exceptions.InvalidArgumentsException;
import net.aufdemrand.denizen.objects.Element;
import net.aufdemrand.denizen.objects.aH;
import net.aufdemrand.denizen.objects.dLocation;
import net.aufdemrand.denizen.scripts.ScriptEntry;
import net.aufdemrand.denizen.scripts.commands.AbstractCommand;
import net.aufdemrand.denizen.utilities.debugging.dB;
import net.aufdemrand.denizen.utilities.debugging.dB.Messages;

/* playeffect [location:<x,y,z,world>] [effect:<name>] (data:<#>) (radius:<#>)*/

/** 
 * Arguments: [] - Required, () - Optional 
 * [location:<x,y,z,world>] specifies location of the effect
 * [effect:<name>] sets the name of effect to be played
 * (data:<#>) sets the special data value of the effect
 * (radius:<#>) adjusts the radius within which players will observe the effect
 * 
 * Example Usage:
 * playeffect location:123,65,765,world effect:record_play data:2259 radius:7
 * playeffect location:<npc.location> e:smoke r:3
 * 
 * @author David Cernat
 */

public class PlayEffectCommand extends AbstractCommand {

	// Implement some special effects that are not in Bukkit's Effect class
	private enum SpecialEffect { HEARTS, EXPLOSION }
	
	@Override
	public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {

        // Iterate through arguments
        for (aH.Argument arg : aH.interpret(scriptEntry.getArguments())) {
        	
			if (!scriptEntry.hasObject("location")
                    && arg.matchesArgumentType(dLocation.class)) {
                // Location arg
                scriptEntry.addObject("location", arg.asType(dLocation.class));
            }
			
			if (!scriptEntry.hasObject("effect")) {
				
				// Add effect
				if (arg.matchesEnum(Effect.values())) {
                    scriptEntry.addObject("effect", Effect.valueOf(arg.getValue().toUpperCase()));
                }
				else if (arg.matchesEnum(SpecialEffect.values())) {
                    scriptEntry.addObject("specialeffect", SpecialEffect.valueOf(arg.getValue().toUpperCase()));
                }
			}
			
			else if (!scriptEntry.hasObject("radius")
                    && arg.matchesPrimitive(aH.PrimitiveType.Integer)
                    && arg.matchesPrefix("radius, r")) {
                // Add value
                scriptEntry.addObject("radius", arg.asElement());
            }
			
			else if (!scriptEntry.hasObject("data")
                    && arg.matchesPrimitive(aH.PrimitiveType.Integer)
                    && arg.matchesPrefix("data, d")) {
                // Add value
                scriptEntry.addObject("data", arg.asElement());
            }
		}
        
        // Check to make sure required arguments have been filled
        
        if ((!scriptEntry.hasObject("location")))
            throw new InvalidArgumentsException(Messages.ERROR_MISSING_OTHER, "LOCATION");

        if (!scriptEntry.hasObject("effect") && !scriptEntry.hasObject("specialeffect"))
            throw new InvalidArgumentsException(Messages.ERROR_MISSING_OTHER, "EFFECT");
        
        // Use a default radius and data if necessary
        
        if ((!scriptEntry.hasObject("radius"))) {
        	scriptEntry.addObject("radius", new Element(5));
        }
        
        if ((!scriptEntry.hasObject("data"))) {
        	scriptEntry.addObject("data", new Element(0));
        }
	}

	@Override
	public void execute(ScriptEntry scriptEntry) throws CommandExecutionException {

        // Extract objects from ScriptEntry
        dLocation location = (dLocation) scriptEntry.getObject("location");
        Effect effect = (Effect) scriptEntry.getObject("effect");
        SpecialEffect specialEffect = (SpecialEffect) scriptEntry.getObject("specialeffect");
        Element radius = (Element) scriptEntry.getObject("radius");
        Element data = (Element) scriptEntry.getObject("data");

        // Report to dB
        dB.report(getName(),
        		(effect != null ?
        			aH.debugObj("effect", effect.name()) :
        			aH.debugObj("special effect", specialEffect.name())) +
        		aH.debugObj("location", location.toString() +
        		aH.debugObj("radius", radius) +
        		aH.debugObj("data", data)));
        
        // Play the Bukkit effect
        if (effect != null) {
        	location.getWorld().playEffect(location, effect, data.asInt(), radius.asInt());
        }
        // Play one of the special effects
        else {
        	if (specialEffect.equals(SpecialEffect.HEARTS)) {
        	
        		Wolf wolf = (Wolf) location.getWorld().spawn(location, Wolf.class);
        		((CraftWolf) wolf).getHandle().setInvisible(true);
        		wolf.playEffect(EntityEffect.WOLF_HEARTS);
        		wolf.remove();
        	}
        	else if (specialEffect.equals(SpecialEffect.EXPLOSION)) {
            	
        		location.getWorld().createExplosion(location, 0);
        	}
        }
	}

}
