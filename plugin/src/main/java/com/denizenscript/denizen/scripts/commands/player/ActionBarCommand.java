package com.denizenscript.denizen.scripts.commands.player;

import com.denizenscript.denizen.scripts.containers.core.FormatScriptContainer;
import com.denizenscript.denizen.utilities.Utilities;
import com.denizenscript.denizen.utilities.debugging.Debug;
import com.denizenscript.denizen.utilities.implementation.BukkitScriptEntryData;
import com.denizenscript.denizen.nms.NMSHandler;
import com.denizenscript.denizen.objects.PlayerTag;
import com.denizenscript.denizencore.exceptions.InvalidArgumentsException;
import com.denizenscript.denizencore.objects.Argument;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.ArgumentHelper;
import com.denizenscript.denizencore.objects.core.ListTag;
import com.denizenscript.denizencore.scripts.ScriptEntry;
import com.denizenscript.denizencore.scripts.ScriptRegistry;
import com.denizenscript.denizencore.scripts.commands.AbstractCommand;
import com.denizenscript.denizencore.tags.TagManager;

import java.util.Arrays;
import java.util.List;

public class ActionBarCommand extends AbstractCommand {

    // <--[command]
    // @Name ActionBar
    // @Syntax actionbar [<text>] (targets:<player>|...) (format:<name>)
    // @Required 1
    // @Short Sends a message to a player's action bar.
    // @group player
    //
    // @Description
    // Sends a message to the target's action bar area. If no target is specified it will default to the attached
    // player. Accepts the 'format:<name>' argument, which will reformat the text according to the specified
    // format script.
    //
    // @Tags
    // None
    //
    // @Usage
    // Use to send a message to the player's action bar.
    // - actionbar "Hey there <player.name>!"
    //
    // @Usage
    // Use to send a message to a list of players.
    // - actionbar "Hey, welcome to the server!" targets:p@john|p@bob|p@steve
    //
    // @Usage
    // Use to send a message to a list of players, with a formatted message.
    // - actionbar "Hey there!" targets:p@john|p@bob format:ServerChat
    // -->

    @Override
    public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {

        for (Argument arg : scriptEntry.getProcessedArgs()) {

            if (arg.matchesPrefix("format", "f")) {
                String formatStr = arg.getValue();
                FormatScriptContainer format = ScriptRegistry.getScriptContainer(formatStr);
                if (format == null) {
                    Debug.echoError("Could not find format script matching '" + formatStr + '\'');
                }
                scriptEntry.addObject("format", format);
            }
            if (arg.matchesPrefix("targets", "target")
                    && arg.matchesArgumentList(PlayerTag.class)) {
                scriptEntry.addObject("targets", arg.asType(ListTag.class).filter(PlayerTag.class, scriptEntry));
            }
            else if (!scriptEntry.hasObject("text")) {
                scriptEntry.addObject("text", new ElementTag(TagManager.cleanOutputFully(arg.raw_value)));
            }
        }

        if (!scriptEntry.hasObject("text")) {
            throw new InvalidArgumentsException("Must specify a message!");
        }

        if (!scriptEntry.hasObject("targets") && !Utilities.entryHasPlayer(scriptEntry)) {
            throw new InvalidArgumentsException("Must specify target(s).");
        }

        if (!scriptEntry.hasObject("targets")) {
            BukkitScriptEntryData data = (BukkitScriptEntryData) scriptEntry.entryData;
            if (!data.hasPlayer()) {
                throw new InvalidArgumentsException("Must specify valid player Targets!");
            }
            else {
                scriptEntry.addObject("targets",
                        Arrays.asList(data.getPlayer()));
            }
        }
    }

    @Override
    public void execute(ScriptEntry scriptEntry) {

        ElementTag text = scriptEntry.getElement("text");
        FormatScriptContainer format = (FormatScriptContainer) scriptEntry.getObject("format");

        List<PlayerTag> targets = (List<PlayerTag>) scriptEntry.getObject("targets");

        if (scriptEntry.dbCallShouldDebug()) {

            Debug.report(scriptEntry, getName(), text.debug() + ArgumentHelper.debugList("Targets", targets));

        }
        if (format != null) {
            text = new ElementTag(format.getFormattedText(text.asString(), scriptEntry));
        }

        for (PlayerTag player : targets) {
            if (player.isValid() && player.isOnline()) {
                NMSHandler.getPacketHelper().sendActionBarMessage(player.getPlayerEntity(), text.asString());
            }
            else {
                Debug.echoError(scriptEntry.getResidingQueue(), "Tried to send action bar message to non-existent or offline player!");
            }
        }

    }

}
