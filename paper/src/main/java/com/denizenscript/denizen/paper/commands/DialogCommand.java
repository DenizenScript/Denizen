package com.denizenscript.denizen.paper.commands;

import com.denizenscript.denizen.objects.PlayerTag;
import com.denizenscript.denizen.paper.scripts.containers.DialogScriptContainer;
import com.denizenscript.denizen.utilities.Utilities;
import com.denizenscript.denizencore.objects.core.ScriptTag;
import com.denizenscript.denizencore.scripts.ScriptEntry;
import com.denizenscript.denizencore.scripts.commands.AbstractCommand;
import com.denizenscript.denizencore.scripts.commands.generator.*;
import com.denizenscript.denizencore.utilities.debugging.Debug;
import io.papermc.paper.dialog.Dialog;

import java.util.List;

// TODO add optional per_player parsing
public class DialogCommand extends AbstractCommand {

    public DialogCommand() {
        setName("dialog");
        setSyntax("dialog [show/close] (<script>) (targets:<player>|...)");
        setRequiredArguments(1, 3);
        autoCompile();
        isProcedural = false;
    }

    // <--[command]
    // @Name Dialog
    // @Syntax dialog [show/close] (<script>) (targets:<player>|...)
    // @Required 1
    // @Maximum 3
    // @Short Shows or closes a Paper dialog for one or more players.
    // @Group player
    // @Plugin Paper
    //
    // @Description
    // Shows or closes a Paper client-side dialog for the specified players.
    // Requires Paper 1.21.6 or later.
    //
    // Use 'show' with a dialog script name to display the dialog to the target player(s).
    // Use 'close' to close any dialog currently open for the target player(s).
    //
    // If no 'targets' are specified, defaults to the linked player.
    //
    // See <@link language Dialog Script Containers> for how to define dialog scripts.
    //
    // @Tags
    // None
    //
    // @Usage
    // Use to show a dialog script to the current player.
    // - dialog show my_dialog
    //
    // @Usage
    // Use to show a dialog to multiple players.
    // - dialog show my_dialog targets:<server.online_players>
    //
    // @Usage
    // Use to close the dialog for the current player.
    // - dialog close
    // -->

    public enum Action { SHOW, CLOSE }

    public static void autoExecute(ScriptEntry scriptEntry,
                                   @ArgName("action") Action action,
                                   @ArgName("script") @ArgLinear @ArgDefaultNull ScriptTag dialogScript,
                                   @ArgName("targets") @ArgPrefixed @ArgDefaultNull @ArgSubType(PlayerTag.class) List<PlayerTag> targets) {
        if (targets == null) {
            if (!Utilities.entryHasPlayer(scriptEntry)) {
                Debug.echoError("Must specify target player(s).");
                return;
            }
            targets = List.of(Utilities.getEntryPlayer(scriptEntry));
        }
        switch (action) {
            case SHOW -> {
                if (dialogScript == null) {
                    Debug.echoError("Must specify a dialog script name for 'show'.");
                    return;
                }
                if (!(dialogScript.getContainer() instanceof DialogScriptContainer container)) {
                    Debug.echoError("Script '" + dialogScript.getName() + "' is not a dialog script container.");
                    return;
                }
                Dialog dialog = container.buildDialog(scriptEntry.getContext());
                if (dialog == null) {
                    Debug.echoError("Failed to build dialog from script '" + dialogScript.getName() + "'.");
                    return;
                }
                for (PlayerTag playerTag : targets) {
                    if (!playerTag.isOnline()) {
                        Debug.echoError("Player '" + playerTag + "' is offline. Skipping...");
                        continue;
                    }
                    playerTag.getPlayerEntity().showDialog(dialog);
                }
            }
            case CLOSE -> {
                for (PlayerTag playerTag : targets) {
                    if (!playerTag.isOnline()) {
                        Debug.echoError("Player '" + playerTag + "' is offline. Skipping...");
                        continue;
                    }
                    playerTag.getPlayerEntity().closeDialog();
                }
            }
        }
    }
}
