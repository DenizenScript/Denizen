package net.aufdemrand.denizen.scripts.commands.core;

import net.aufdemrand.denizen.exceptions.CommandExecutionException;
import net.aufdemrand.denizen.exceptions.InvalidArgumentsException;
import net.aufdemrand.denizen.objects.Duration;
import net.aufdemrand.denizen.objects.aH;
import net.aufdemrand.denizen.objects.dLocation;
import net.aufdemrand.denizen.objects.dScript;
import net.aufdemrand.denizen.scripts.ScriptEntry;
import net.aufdemrand.denizen.scripts.ScriptQueue;
import net.aufdemrand.denizen.scripts.ScriptRegistry;
import net.aufdemrand.denizen.scripts.commands.AbstractCommand;
import net.aufdemrand.denizen.scripts.containers.core.TaskScriptContainer;
import net.aufdemrand.denizen.utilities.debugging.dB;

import java.util.HashMap;
import java.util.Map;

/**
 * @author aufdemrand
 *
 */
public class ForEachCommand extends AbstractCommand {

    // - foreach location:0,0,0,world|3,3,3,world 'fill_it'

    enum Type {LOCATION, LIST_ITEM}

    @Override
    public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {

        Type type = null;
        dScript script = null;
        dLocation location_1 = null;
        dLocation location_2 = null;

        // Parse Arguments
        for (String arg : scriptEntry.getArguments()) {

            if (aH.matchesValueArg("LOCATION, ITEM_IN_LIST", arg, aH.ArgumentType.Custom)) {
                type = Type.valueOf(arg.split(":")[0].toUpperCase());

                if (type == Type.LOCATION) {
                    location_1 = dLocation.valueOf(aH.getStringFrom(arg).split("\\|")[0]);
                    location_2 = dLocation.valueOf(aH.getStringFrom(arg).split("\\|")[1]);
                }
            }

            else {
                if (ScriptRegistry.containsScript(aH.getStringFrom(arg), TaskScriptContainer.class))
                    script = aH.getScriptFrom(arg);
                else dB.echoError("'" + aH.getStringFrom(arg) + "' is not valid! Must specify a TASK-type script.");
            }
        }

        if (type == null) throw new InvalidArgumentsException("Must specify a 'foreach' type!");

        if (type == Type.LOCATION && (location_1 == null || location_2 == null))
            throw new InvalidArgumentsException("Invalid locations have been specified!");

        scriptEntry.addObject("loc_1", location_1)
                .addObject("loc_2", location_2)
                .addObject("type", type)
                .addObject("script", script);

    }

    @Override
    public void execute(ScriptEntry scriptEntry) throws CommandExecutionException {

        // Get objects
        Type type = (Type) scriptEntry.getObject("type");
        dLocation loc_1 = (dLocation) scriptEntry.getObject("loc_1");
        dLocation loc_2 = (dLocation) scriptEntry.getObject("loc_2");
        dScript script = (dScript) scriptEntry.getObject("script");

        // Report to dB
        dB.report(getName(),
                aH.debugObj("Type", type.toString())
                        + (type == Type.LOCATION ? loc_1.debug() + loc_2.debug() : "")
                        + script.debug());

        if (type == Type.LOCATION) {

            int x_inc = -1;
            int y_inc = -1;
            int z_inc = -1;

            if (loc_1.getBlockX() <= loc_2.getBlockX()) x_inc = 1;
            if (loc_1.getBlockY() <= loc_2.getBlockY()) y_inc = 1;
            if (loc_1.getBlockZ() <= loc_2.getBlockZ()) z_inc = 1;

            int x_amt = Math.abs(loc_1.getBlockX() - loc_2.getBlockX());
            int y_amt = Math.abs(loc_1.getBlockY() - loc_2.getBlockY());
            int z_amt = Math.abs(loc_1.getBlockZ() - loc_2.getBlockZ());

            for (int x = 0; x != x_amt + 1; x++) {
                for (int y = 0; y != y_amt + 1; y++) {
                    for (int z = 0; z != z_amt + 1; z++) {
                        dLocation loc = new dLocation(loc_1.clone().add((double) x * x_inc, (double) y * y_inc, (double) z * z_inc));
                        
                        dB.echoDebug("location: " + loc.identify());

                        Map<String, String> context = new HashMap<String, String>();
                        context.put("1", loc.identify());

                        ((TaskScriptContainer) script.getContainer()).setSpeed(Duration.valueOf("0"))
                                .runTaskScript(ScriptQueue._getNextId(),
                                        scriptEntry.getPlayer(),
                                        scriptEntry.getNPC(),
                                        context);
                    }
                }
            }
        }
    }

}
