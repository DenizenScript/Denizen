package com.denizenscript.denizen.scripts.commands.player;

import com.denizenscript.denizen.utilities.PaperAPITools;
import com.denizenscript.denizencore.scripts.commands.generator.*;
import com.denizenscript.denizen.objects.PlayerTag;
import com.denizenscript.denizencore.scripts.commands.AbstractCommand;

import java.util.List;

public class KickCommand extends AbstractCommand {

    public KickCommand() {
        setName("kick");
        setSyntax("kick [<player>|...] (reason:<text>)");
        setRequiredArguments(1, 2);
        isProcedural = false;
        autoCompile();
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

    public static void autoExecute(@ArgName("targets") @ArgLinear @ArgSubType(PlayerTag.class) List<PlayerTag> targets,
                                   @ArgName("reason") @ArgPrefixed @ArgDefaultText("Kicked.") String reason) {
        for (PlayerTag player : targets) {
            if (player.isValid() && player.isOnline()) {
                PaperAPITools.instance.kickPlayer(player.getPlayerEntity(), reason);
            }
        }
    }
}
