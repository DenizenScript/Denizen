package com.denizenscript.denizen.scripts.commands.world;

import com.denizenscript.denizen.utilities.Utilities;
import com.denizenscript.denizen.utilities.debugging.dB;
import com.denizenscript.denizen.objects.dLocation;
import com.denizenscript.denizencore.exceptions.InvalidArgumentsException;
import com.denizenscript.denizencore.objects.Element;
import com.denizenscript.denizencore.objects.aH;
import com.denizenscript.denizencore.scripts.ScriptEntry;
import com.denizenscript.denizencore.scripts.commands.AbstractCommand;

public class ExplodeCommand extends AbstractCommand {

    // <--[command]
    // @Name Explode
    // @Syntax explode (power:<#.#>) (<location>) (fire) (breakblocks)
    // @Required 0
    // @Short Causes an explosion at the location.
    // @Group world
    //
    // @Description
    // This command causes an explosion at the location specified (or the npc / player location) which does not
    // destroy blocks or set fire to blocks within the explosion. It accepts a 'fire' option which will set blocks
    // on fire within the explosion radius. It also accepts a 'breakblocks' option which will cause the explosion to
    // break blocks within the power radius as well as creating an animation and sounds.
    // Default power: 1
    // Default location: npc.location, or if no NPC link, player.location.
    // It is highly recommended you specify a location to be safe.
    //
    // @Tags
    // None
    //
    // @Usage
    // Use to create an explosion at a player's location.
    // - explode <player.location>
    //
    // @Usage
    // Use to create an explosion at a player, which breaks blocks and causes fire with a power of 5.
    // - explode power:5 <player.location> fire breakblocks
    //
    // @Usage
    // Use to create an explosion with a power radius of 3 at an NPC's location.
    // - explode power:3 <npc.location>
    //
    // @Usage
    // Use to create an explosion with a power radius of 3 at a 12,12,-1297 in a world called survival which breaks blocks.
    // - explode power:3 l@12,12,-1297,survival breakblocks
    // -->

    @Override
    public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {

        // Iterate through arguments
        for (aH.Argument arg : aH.interpretArguments(scriptEntry.aHArgs)) {

            if (!scriptEntry.hasObject("location")
                    && arg.matchesArgumentType(dLocation.class)) {

                scriptEntry.addObject("location", arg.asType(dLocation.class));
            }
            else if (!scriptEntry.hasObject("power")
                    && arg.matchesPrimitive(aH.PrimitiveType.Float)
                    && arg.matchesPrefix("power", "p")) {

                scriptEntry.addObject("power", arg.asElement());
            }
            else if (!scriptEntry.hasObject("breakblocks")
                    && arg.matches("breakblocks")) {

                scriptEntry.addObject("breakblocks", "");
            }
            else if (!scriptEntry.hasObject("fire")
                    && arg.matches("fire")) {

                scriptEntry.addObject("fire", "");
            }
            else {
                arg.reportUnhandled();
            }
        }

        // Use default values if necessary
        scriptEntry.defaultObject("power", new Element(1.0));
        scriptEntry.defaultObject("location",
                Utilities.entryHasNPC(scriptEntry) ? Utilities.getEntryNPC(scriptEntry).getLocation() : null,
                Utilities.entryHasPlayer(scriptEntry) ? Utilities.getEntryPlayer(scriptEntry).getLocation() : null);

        if (!scriptEntry.hasObject("location")) {
            throw new InvalidArgumentsException("Missing location argument!");
        }
    }

    @Override
    public void execute(final ScriptEntry scriptEntry) {
        // Get objects

        final dLocation location = (dLocation) scriptEntry.getObject("location");
        Element power = (Element) scriptEntry.getObject("power");
        boolean breakblocks = scriptEntry.hasObject("breakblocks");
        boolean fire = scriptEntry.hasObject("fire");

        // Report to dB
        if (scriptEntry.dbCallShouldDebug()) {
            dB.report(scriptEntry, getName(),
                    (aH.debugObj("location", location.toString()) +
                            aH.debugObj("power", power) +
                            aH.debugObj("breakblocks", breakblocks) +
                            aH.debugObj("fire", fire)));
        }

        location.getWorld().createExplosion
                (location.getX(), location.getY(), location.getZ(),
                        power.asFloat(), fire, breakblocks);
    }
}
