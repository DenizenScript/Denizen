package net.aufdemrand.denizen.scripts.commands.world;

import org.bukkit.Effect;

import net.aufdemrand.denizen.exceptions.CommandExecutionException;
import net.aufdemrand.denizen.exceptions.InvalidArgumentsException;
import net.aufdemrand.denizen.objects.Element;
import net.aufdemrand.denizen.objects.aH;
import net.aufdemrand.denizen.objects.dLocation;
import net.aufdemrand.denizen.scripts.ScriptEntry;
import net.aufdemrand.denizen.scripts.commands.AbstractCommand;
import net.aufdemrand.denizen.utilities.debugging.dB;
import net.aufdemrand.denizen.utilities.debugging.dB.Messages;
import net.aufdemrand.denizen.utilities.ParticleEffect;

/** 
 * Lets you play a Bukkit effect or a ParticleEffect from the
 * ParticleEffect Library at a certain location.
 * 
 * Arguments: [] - Required, () - Optional 
 * [location:<x,y,z,world>] specifies location of the effect
 * [effect:<name>] sets the name of effect to be played
 * (data:<#>) sets the special data value of the effect
 * (radius:<#>) adjusts the radius within which players will observe the effect
 * (qty:<#>) sets the number of times the effect will be played
 * (offset:<#>) sets the offset of ParticleEffects.
 * 
 * Example Usage:
 * playeffect location:123,65,765,world effect:record_play data:2259 radius:7
 * playeffect location:<npc.location> e:smoke r:3
 * playeffect location:<npc.location> effect:heart radius:7 qty:1000 offset:20
 * 
 * @author David Cernat
 */

public class PlayEffectCommand extends AbstractCommand {

	@Override
	public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {

        // Iterate through arguments
        for (aH.Argument arg : aH.interpret(scriptEntry.getArguments())) {
        	
			if (!scriptEntry.hasObject("location")
                    && arg.matchesArgumentType(dLocation.class)) {
                // Location arg
                scriptEntry.addObject("location", arg.asType(dLocation.class));
            }
			
			else if (!scriptEntry.hasObject("effect") &&
					 !scriptEntry.hasObject("particleeffect")) {
				
				// Add effect
				if (arg.matchesEnum(Effect.values())) {
                    scriptEntry.addObject("effect", Effect.valueOf(arg.getValue().toUpperCase()));
                }
				else if (arg.matchesEnum(ParticleEffect.values())) {
                    scriptEntry.addObject("particleeffect",
                    		ParticleEffect.valueOf(arg.getValue().toUpperCase()));
                }
			}
			
			else if (!scriptEntry.hasObject("radius")
                    && arg.matchesPrimitive(aH.PrimitiveType.Double)
                    && arg.matchesPrefix("range, radius, r")) {
                // Add value
                scriptEntry.addObject("radius", arg.asElement());
            }
			
			else if (!scriptEntry.hasObject("data")
                    && arg.matchesPrimitive(aH.PrimitiveType.Double)
                    && arg.matchesPrefix("data, d")) {
                // Add value
                scriptEntry.addObject("data", arg.asElement());
            }
			
			else if (!scriptEntry.hasObject("qty")
                    && arg.matchesPrimitive(aH.PrimitiveType.Integer)
                    && arg.matchesPrefix("qty, q")) {
                // Add value
                scriptEntry.addObject("qty", arg.asElement());
            }
			
			else if (!scriptEntry.hasObject("offset")
                    && arg.matchesPrimitive(aH.PrimitiveType.Double)
                    && arg.matchesPrefix("offset, o")) {
                // Add value
                scriptEntry.addObject("offset", arg.asElement());
            }
		}
        
        // Check to make sure required arguments have been filled
        
        if ((!scriptEntry.hasObject("location")))
            throw new InvalidArgumentsException(Messages.ERROR_MISSING_OTHER, "LOCATION");

        if (!scriptEntry.hasObject("effect") &&
        	!scriptEntry.hasObject("particleeffect"))
            throw new InvalidArgumentsException(Messages.ERROR_MISSING_OTHER, "EFFECT");
        
        // Use default values if necessary
        
        if ((!scriptEntry.hasObject("radius"))) {
        	scriptEntry.addObject("radius", new Element(5));
        }
        
        if ((!scriptEntry.hasObject("data"))) {
        	scriptEntry.addObject("data", new Element(0));
        }
        
        if ((!scriptEntry.hasObject("qty"))) {
        	scriptEntry.addObject("qty", new Element(1));
        }
        
        if ((!scriptEntry.hasObject("offset"))) {
        	scriptEntry.addObject("offset", new Element(0.5));
        }
	}

	@Override
	public void execute(ScriptEntry scriptEntry) throws CommandExecutionException {

        // Extract objects from ScriptEntry
        dLocation location = (dLocation) scriptEntry.getObject("location");
        Effect effect = (Effect) scriptEntry.getObject("effect");
        ParticleEffect particleEffect = (ParticleEffect) scriptEntry.getObject("particleeffect");
        Element radius = (Element) scriptEntry.getObject("radius");
        Element data = (Element) scriptEntry.getObject("data");
        Element qty = (Element) scriptEntry.getObject("qty");
        Element offset = (Element) scriptEntry.getObject("offset");

        // Slightly increase the location's Y so effects don't seem
        // to come out of the ground
        location.add(0, 1, 0);
        
        // Report to dB
        dB.report(getName(),
        		(effect != null ?
        			aH.debugObj("effect", effect.name()) :
        			aH.debugObj("special effect", particleEffect.name())) +
        		aH.debugObj("location", location.toString()) +
        		aH.debugObj("radius", radius) +
        		aH.debugObj("data", data) +
        		aH.debugObj("qty", qty) +
        		(effect != null ? "" : aH.debugObj("offset", offset)));
        
        // Play the Bukkit effect the number of times specified
        if (effect != null) {
        	
        	for (int n = 0; n < qty.asInt(); n++) {
        		location.getWorld().playEffect(location, effect, data.asInt(), radius.asInt());
        	}
        }
        // Play a ParticleEffect
        else {
        	ParticleEffect.valueOf(particleEffect.name())
        		.play(location, radius.asDouble(),
        			  offset.asFloat(), offset.asFloat(), offset.asFloat(), data.asFloat(), qty.asInt());
        }
	}
}
