package com.denizenscript.denizen.scripts.commands.player;

import com.denizenscript.denizen.utilities.Utilities;
import com.denizenscript.denizen.utilities.debugging.Debug;
import com.denizenscript.denizen.objects.dMaterial;
import com.denizenscript.denizen.objects.dPlayer;
import com.denizenscript.denizencore.exceptions.InvalidArgumentsException;
import com.denizenscript.denizencore.objects.Argument;
import com.denizenscript.denizencore.objects.Duration;
import com.denizenscript.denizencore.objects.ArgumentHelper;
import com.denizenscript.denizencore.objects.dList;
import com.denizenscript.denizencore.scripts.ScriptEntry;
import com.denizenscript.denizencore.scripts.commands.AbstractCommand;

import java.util.ArrayList;

public class ItemCooldownCommand extends AbstractCommand {

    // <--[command]
    // @Name ItemCooldown
    // @Syntax itemcooldown [<material>|...] (duration:<duration>)
    // @Required 1
    // @Short Places a cooldown on a material in a player's inventory.
    // @Group player
    //
    // @Description
    // Places a cooldown on a material in a player's inventory.
    //
    // @Tags
    // <p@player.item_cooldown[<material>]>
    //
    // @Usage
    // Places a 1 second cooldown on using an ender pearl.
    // - itemcooldown ender_pearl
    //
    // @Usage
    // Places a 10 minute cooldown on using golden apples.
    // - itemcooldown golden_apple d:10m
    //
    // -->

    @Override
    public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {

        for (Argument arg : ArgumentHelper.interpretArguments(scriptEntry.aHArgs)) {

            if (!scriptEntry.hasObject("materials")
                    && (arg.matchesArgumentType(dMaterial.class)
                    || arg.matchesArgumentType(dList.class))) {
                scriptEntry.addObject("materials", arg.asType(dList.class).filter(dMaterial.class, scriptEntry));
            }
            else if (!scriptEntry.hasObject("duration")
                    && arg.matchesPrefix("d", "duration")
                    && arg.matchesArgumentType(Duration.class)) {
                scriptEntry.addObject("duration", arg.asType(Duration.class));
            }
            else {
                arg.reportUnhandled();
            }
        }

        if (!scriptEntry.hasObject("materials")) {
            throw new InvalidArgumentsException("Missing materials argument!");
        }

        scriptEntry.defaultObject("duration", new Duration(1));
    }

    @Override
    public void execute(ScriptEntry scriptEntry) {

        ArrayList<dMaterial> materials = (ArrayList<dMaterial>) scriptEntry.getObject("materials");
        Duration duration = scriptEntry.getdObject("duration");
        dPlayer player = Utilities.getEntryPlayer(scriptEntry);

        if (player == null) {
            Debug.echoError("Invalid linked player.");
            return;
        }

        if (scriptEntry.dbCallShouldDebug()) {

            Debug.report(scriptEntry, getName(), ArgumentHelper.debugList("materials", materials) + duration.debug());

        }

        for (dMaterial mat : materials) {
            player.getPlayerEntity().setCooldown(mat.getMaterial(), duration.getTicksAsInt());
        }
    }
}
