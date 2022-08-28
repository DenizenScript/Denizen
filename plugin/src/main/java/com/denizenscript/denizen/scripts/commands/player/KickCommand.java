package com.denizenscript.denizen.scripts.commands.player;

import com.denizenscript.denizencore.utilities.debugging.Debug;
import com.denizenscript.denizen.objects.PlayerTag;
import com.denizenscript.denizencore.exceptions.InvalidArgumentsException;
import com.denizenscript.denizencore.objects.Argument;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.core.ListTag;
import com.denizenscript.denizencore.scripts.ScriptEntry;
import com.denizenscript.denizencore.scripts.commands.AbstractCommand;

import java.util.List;

public class KickCommand extends AbstractCommand {

    public KickCommand() {
        setName("kick");
        setSyntax("kick [<player>|...] (reason:<text>)");
        setRequiredArguments(1, 2);
        isProcedural = false;
    }

    // <--[command]
    // @Name kick
    // @Syntax kick [<player>|...] (reason:<text>)
    // @Required 1
    // @Maximum 2
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
    // - kick <[player]> "reason:Because I can."
    // -->

    @Override
    public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {
        for (Argument arg : scriptEntry) {
            if (arg.matchesPrefix("reason")) {
                scriptEntry.addObject("reason", arg.asElement());
            }
            else if (arg.matchesPrefix("targets", "target", "players")
                    || arg.matchesArgumentList(PlayerTag.class)) {
                scriptEntry.addObject("targets", arg.asType(ListTag.class).filter(PlayerTag.class, scriptEntry));
            }
        }
        scriptEntry.defaultObject("reason", new ElementTag("Kicked."));
        if (!scriptEntry.hasObject("targets")) {
            throw new InvalidArgumentsException("Must specify target(s).");
        }
    }

    @Override
    public void execute(ScriptEntry scriptEntry) {
        ElementTag reason = scriptEntry.getElement("reason");
        List<PlayerTag> targets = (List<PlayerTag>) scriptEntry.getObject("targets");
        if (scriptEntry.dbCallShouldDebug()) {
            Debug.report(scriptEntry, getName(), db("targets", targets), reason);
        }
        for (PlayerTag player : targets) {
            if (player.isValid() && player.isOnline()) {
                player.getPlayerEntity().kickPlayer(reason.toString());
            }
        }
    }
}
