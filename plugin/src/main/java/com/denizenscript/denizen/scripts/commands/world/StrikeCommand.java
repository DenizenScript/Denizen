package com.denizenscript.denizen.scripts.commands.world;

import com.denizenscript.denizen.utilities.debugging.dB;
import com.denizenscript.denizen.objects.dLocation;
import com.denizenscript.denizencore.exceptions.InvalidArgumentsException;
import com.denizenscript.denizencore.objects.Element;
import com.denizenscript.denizencore.objects.aH;
import com.denizenscript.denizencore.scripts.ScriptEntry;
import com.denizenscript.denizencore.scripts.commands.AbstractCommand;


public class StrikeCommand extends AbstractCommand {

    // <--[command]
    // @Name Strike
    // @Syntax strike (no_damage) [<location>]
    // @Required 1
    // @Short Strikes lightning down upon the location.
    // @Group world
    //
    // @Description
    // Causes lightning to strike at the specified location, which can optionally have damage disabled.
    // The lightning will still cause fires to start, even without the 'no_damage' argument.
    // Lightning caused by this command will cause creepers to activate. Using the no_damage argument makes the
    // lightning do no damage to the player or any other entities, and means creepers struck will not activate.
    //
    // @Tags
    // None
    //
    // @Usage
    // Use to cause lightning to strike the player.
    // - strike <player.location>
    //
    // @Usage
    // Use to strike the player with lightning causing no damage.
    // - strike no_damage <player.location>
    // -->

    @Override
    public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {

        // Iterate through arguments
        for (aH.Argument arg : aH.interpretArguments(scriptEntry.aHArgs)) {

            if (!scriptEntry.hasObject("location")
                    && arg.matchesArgumentType(dLocation.class)) {
                scriptEntry.addObject("location", arg.asType(dLocation.class));
            }
            else if (arg.matches("no_damage") || arg.matches("nodamage")) {
                scriptEntry.addObject("damage", new Element(false));
            }
            else {
                arg.reportUnhandled();
            }

        }

        // Check required args
        if (!scriptEntry.hasObject("location")) {
            throw new InvalidArgumentsException("Missing location argument!");
        }

        scriptEntry.defaultObject("damage", new Element(true));
    }

    @Override
    public void execute(ScriptEntry scriptEntry) {

        // Extract objects from ScriptEntry
        dLocation location = (dLocation) scriptEntry.getObject("location");
        Boolean damage = scriptEntry.getElement("damage").asBoolean();

        // Debugger
        if (scriptEntry.dbCallShouldDebug()) {
            dB.report(scriptEntry, getName(),
                    location.debug()
                            + aH.debugObj("Damageable", String.valueOf(damage)));
        }

        // Play the sound
        if (damage) {
            location.getWorld().strikeLightning(location);
        }
        else {
            location.getWorld().strikeLightningEffect(location);
        }
    }
}
