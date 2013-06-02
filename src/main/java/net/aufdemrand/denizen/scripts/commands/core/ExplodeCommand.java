package net.aufdemrand.denizen.scripts.commands.core;

import net.aufdemrand.denizen.Settings;
import net.aufdemrand.denizen.exceptions.CommandExecutionException;
import net.aufdemrand.denizen.exceptions.InvalidArgumentsException;
import net.aufdemrand.denizen.scripts.ScriptEntry;
import net.aufdemrand.denizen.scripts.commands.AbstractCommand;
import net.aufdemrand.denizen.scripts.containers.core.TaskScriptContainer;
import net.aufdemrand.denizen.utilities.Utilities;
import net.aufdemrand.denizen.utilities.arguments.Duration;
import net.aufdemrand.denizen.utilities.arguments.dLocation;
import net.aufdemrand.denizen.utilities.arguments.Script;
import net.aufdemrand.denizen.utilities.arguments.aH;
import net.aufdemrand.denizen.utilities.arguments.aH.ArgumentType;
import net.aufdemrand.denizen.utilities.debugging.dB;
import net.aufdemrand.denizen.utilities.debugging.dB.Messages;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.FireworkEffect.Type;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Projectile;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

/**
 * Create an explosion at a location.
 * If no location is specified, create the explosion where the NPC is.
 *
 * @author Alain Blanquet
 */

public class ExplodeCommand extends AbstractCommand {
    
    @Override
    public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {

        dLocation location = null;
        Float power = 1F;

        for (String arg : scriptEntry.getArguments()) {
            if (aH.matchesLocation(arg)) {
                location = aH.getLocationFrom(arg);
                dB.echoDebug("...location set to '%s'.", arg);

            } else if (aH.matchesValueArg("power", arg, ArgumentType.Float)) {
                power = aH.getFloatFrom(arg);
                dB.echoDebug("...will have a power of " + power);
                
            } else throw new InvalidArgumentsException(Messages.ERROR_UNKNOWN_ARGUMENT, arg);
        }            

         // Stash objects
         scriptEntry.addObject("location", location);
         scriptEntry.addObject("power", power);
    }
    
    @Override
    public void execute(final ScriptEntry scriptEntry) throws CommandExecutionException {
        // Get objects
        
        final dLocation location = scriptEntry.hasObject("location") ?
                (dLocation) scriptEntry.getObject("location") :
                (dLocation) scriptEntry.getNPC().getLocation();
        Float power = (Float) scriptEntry.getObject("power");
                                   

        location.getWorld().createExplosion(location, (Float) power);
 
    }

}