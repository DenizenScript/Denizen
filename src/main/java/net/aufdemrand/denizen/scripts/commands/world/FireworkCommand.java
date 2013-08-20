package net.aufdemrand.denizen.scripts.commands.world;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import net.aufdemrand.denizen.exceptions.CommandExecutionException;
import net.aufdemrand.denizen.exceptions.InvalidArgumentsException;
import net.aufdemrand.denizen.objects.aH;
import net.aufdemrand.denizen.objects.aH.ArgumentType;
import net.aufdemrand.denizen.objects.dColor;
import net.aufdemrand.denizen.objects.dLocation;
import net.aufdemrand.denizen.scripts.ScriptEntry;
import net.aufdemrand.denizen.scripts.commands.AbstractCommand;
import net.aufdemrand.denizen.utilities.Utilities;
import net.aufdemrand.denizen.utilities.debugging.dB;
import net.aufdemrand.denizen.utilities.debugging.dB.Messages;

import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.FireworkEffect.Builder;
import org.bukkit.FireworkEffect.Type;
import org.bukkit.entity.Firework;
import org.bukkit.inventory.meta.FireworkMeta;

/**
 * Shoots a firework into the air from a location.
 * If no location is chosen, the firework is shot from the NPC's location.
 *
 * @author David Cernat
 */

public class FireworkCommand extends AbstractCommand {
    
    @Override
    public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {

        // Initialize necessary fields
        dLocation location = null;
        Type type = Type.BALL;
        Integer power = 1;
        Boolean flicker = false;
        Boolean trail = false;
        List<Color> primary = new ArrayList<Color>();
        List<Color> fade = new ArrayList<Color>();

        for (String arg : scriptEntry.getArguments()) {
            if (aH.matchesLocation(arg)) {
                location = aH.getLocationFrom(arg);
                dB.echoDebug("...location set to '%s'.", arg);

            }
            else if (aH.matchesValueArg("type", arg, ArgumentType.String)) {
                
                String typeArg = arg.split(":", 2)[1].toUpperCase();
                
                if (typeArg.matches("RANDOM")) {
                    
                    type = FireworkEffect.Type.values()[Utilities.getRandom().nextInt(FireworkEffect.Type.values().length)];
                }
                else {
                
                    for (FireworkEffect.Type typeValue : FireworkEffect.Type.values()) {
                    
                        if (typeArg.matches(typeValue.name())) {
                        
                            type = typeValue;
                            break;
                        }
                    }
                }
                
                dB.echoDebug("...will be of type " + type);
            
            }
            else if (aH.matchesValueArg("power", arg, ArgumentType.Integer)) {
                power = aH.getIntegerFrom(arg);
                dB.echoDebug("...will have a power of " + power);
            
            }
            else if (aH.matchesArg("flicker", arg)) {
                flicker = true;
                dB.echoDebug("...will flicker.");
                
            }
            else if (aH.matchesArg("trail", arg)) {
                trail = true;
                dB.echoDebug("...will have a trail.");
            
            }
            else if (aH.matchesValueArg("PRIMARY", arg, ArgumentType.String)) {
                // May be multiple colors, so let's treat this as a potential list.
                // dScript list entries are separated by pipes ('|')
                for (String element : aH.getListFrom(arg)) {
                    
                    if (dColor.matches(element)) {
                        primary.add(dColor.valueOf(element).getColor());
                    }
                    else {
                        dB.echoError("Invalid color " + element + "!");
                    }
                }
            } else if (aH.matchesValueArg("FADE", arg, ArgumentType.String)) {
                // Same as above
                for (String element : aH.getListFrom(arg)) {
                    
                    if (dColor.matches(element)) {
                        fade.add(dColor.valueOf(element).getColor());
                    }
                    else {
                        dB.echoError("Invalid color " + element + "!");
                    }
                }
            }
            else throw new InvalidArgumentsException(Messages.ERROR_UNKNOWN_ARGUMENT, arg);
        }

        // Stash objects
        scriptEntry.addObject("location", location);
        scriptEntry.addObject("type", type);
        scriptEntry.addObject("primary", primary);
        scriptEntry.addObject("fade", fade);
        scriptEntry.addObject("power", power);
        scriptEntry.addObject("flicker", flicker);
        scriptEntry.addObject("trail", trail);
    }
    
    @SuppressWarnings("unchecked")
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
        List<Color> primary = (List<Color>) scriptEntry.getObject("primary");
        List<Color> fade = (List<Color>) scriptEntry.getObject("fade");
        
        Firework firework = location.getWorld().spawn(location, Firework.class);
        FireworkMeta fireworkMeta = (FireworkMeta) firework.getFireworkMeta();
        fireworkMeta.setPower(power);
        
        Builder fireworkBuilder = FireworkEffect.builder();
        
        fireworkBuilder.with(type);
        
        // If there are no primary colors, there will be an error, so add one
        if (primary.size() == 0) {
            
            primary.add(dColor.valueOf("yellow").getColor());
        }
        
        fireworkBuilder.withColor(primary);
        fireworkBuilder.withFade(fade);
        
        if (flicker) { fireworkBuilder.withFlicker(); }
        if (trail) { fireworkBuilder.withTrail(); }
        
        fireworkMeta.addEffects(fireworkBuilder.build());
        firework.setFireworkMeta(fireworkMeta);

    }

}