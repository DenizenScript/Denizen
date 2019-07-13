package com.denizenscript.denizen.scripts.commands.player;

import com.denizenscript.denizen.scripts.containers.core.FormatScriptContainer;
import com.denizenscript.denizen.utilities.Utilities;
import com.denizenscript.denizen.utilities.debugging.dB;
import com.denizenscript.denizen.BukkitScriptEntryData;
import com.denizenscript.denizen.nms.NMSHandler;
import com.denizenscript.denizen.objects.dPlayer;
import com.denizenscript.denizencore.exceptions.InvalidArgumentsException;
import com.denizenscript.denizencore.objects.Argument;
import com.denizenscript.denizencore.objects.Element;
import com.denizenscript.denizencore.objects.ArgumentHelper;
import com.denizenscript.denizencore.objects.dList;
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
    // - actionbar "Hey, welcome to the server!" targets:p@Fortifier42|p@mcmonkey4eva|p@Morphan1
    //
    // @Usage
    // Use to send a message to a list of players, with a formatted message.
    // - actionbar "Hey there!" targets:p@Fortifier42|p@mcmonkey4eva format:ServerChat
    // -->

    @Override
    public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {

        for (Argument arg : ArgumentHelper.interpretArguments(scriptEntry.aHArgs)) {

            if (arg.matchesPrefix("format", "f")) {
                String formatStr = arg.getValue();
                FormatScriptContainer format = ScriptRegistry.getScriptContainer(formatStr);
                if (format == null) {
                    dB.echoError("Could not find format script matching '" + formatStr + '\'');
                }
                scriptEntry.addObject("format", format);
            }
            if (arg.matchesPrefix("targets", "target")
                    && arg.matchesArgumentList(dPlayer.class)) {
                scriptEntry.addObject("targets", arg.asType(dList.class).filter(dPlayer.class, scriptEntry));
            }
            else if (!scriptEntry.hasObject("text")) {
                scriptEntry.addObject("text", new Element(TagManager.cleanOutputFully(arg.raw_value)));
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

        Element text = scriptEntry.getElement("text");
        FormatScriptContainer format = (FormatScriptContainer) scriptEntry.getObject("format");

        List<dPlayer> targets = (List<dPlayer>) scriptEntry.getObject("targets");

        if (scriptEntry.dbCallShouldDebug()) {

            dB.report(scriptEntry, getName(), text.debug() + ArgumentHelper.debugList("Targets", targets));

        }
        if (format != null) {
            text = new Element(format.getFormattedText(scriptEntry));
        }

        for (dPlayer player : targets) {
            if (player.isValid() && player.isOnline()) {
                NMSHandler.getInstance().getPacketHelper().sendActionBarMessage(player.getPlayerEntity(), text.asString());
            }
            else {
                dB.echoError(scriptEntry.getResidingQueue(), "Tried to send action bar message to non-existent or offline player!");
            }
        }

    }

}
