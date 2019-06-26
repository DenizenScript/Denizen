package net.aufdemrand.denizen.scripts.commands.player;

import net.aufdemrand.denizen.objects.dPlayer;
import net.aufdemrand.denizen.utilities.debugging.dB;
import net.aufdemrand.denizencore.exceptions.InvalidArgumentsException;
import net.aufdemrand.denizencore.objects.Element;
import net.aufdemrand.denizencore.objects.aH;
import net.aufdemrand.denizencore.objects.dList;
import net.aufdemrand.denizencore.scripts.ScriptEntry;
import net.aufdemrand.denizencore.scripts.commands.AbstractCommand;

import java.util.List;

public class KickCommand extends AbstractCommand {

    // <--[command]
    // @Name kick
    // @Syntax kick [<player>|...] (reason:<text>)
    // @Required 1
    // @Short Kicks a player from the server.
    // @Group player
    //
    // @Description
    // Kick a player or a list of players from the server and optionally specify a reason.
    // If no reason is specified it defaults to "Kicked."
    //
    // @Tags
    // None
    //
    // @Usage
    // Use to kick the player with the default reason.
    // - kick <player>
    //
    // @Usage
    // Use to kick the player with a reason.
    // - kick <player> "reason:Because I can."
    //
    // @Usage
    // Use to kick another player with a reason.
    // - kick p@mcmonkey4eva "reason:Because I can."
    // -->

    @Override
    public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {

        for (aH.Argument arg : aH.interpretArguments(scriptEntry.aHArgs)) {

            if (arg.matchesPrefix("reason")) {
                scriptEntry.addObject("reason", arg.asElement());
            }
            else if (arg.matchesPrefix("targets", "target", "players")
                    || arg.matchesArgumentList(dPlayer.class)) {
                scriptEntry.addObject("targets", arg.asType(dList.class).filter(dPlayer.class, scriptEntry));
            }
        }

        scriptEntry.defaultObject("reason", new Element("Kicked."));

        if (!scriptEntry.hasObject("targets")) {
            throw new InvalidArgumentsException("Must specify target(s).");
        }

    }

    @Override
    public void execute(ScriptEntry scriptEntry) {

        Element reason = scriptEntry.getElement("reason");
        List<dPlayer> targets = (List<dPlayer>) scriptEntry.getObject("targets");

        if (scriptEntry.dbCallShouldDebug()) {

            dB.report(scriptEntry, getName(),
                    aH.debugObj("targets", targets) +
                            reason.debug());

        }

        for (dPlayer player : targets) {
            if (player.isValid() && player.isOnline()) {
                player.getPlayerEntity().kickPlayer(reason.toString());
            }
        }

    }

}
