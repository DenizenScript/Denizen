package com.denizenscript.denizen.scripts.commands.server;

import com.denizenscript.denizen.scripts.containers.core.FormatScriptContainer;
import com.denizenscript.denizen.utilities.DenizenAPI;
import com.denizenscript.denizen.utilities.FormattedTextHelper;
import com.denizenscript.denizen.utilities.debugging.Debug;
import com.denizenscript.denizen.flags.FlagManager;
import com.denizenscript.denizen.objects.PlayerTag;
import com.denizenscript.denizencore.exceptions.InvalidArgumentsException;
import com.denizenscript.denizencore.objects.Argument;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.ArgumentHelper;
import com.denizenscript.denizencore.scripts.ScriptEntry;
import com.denizenscript.denizencore.scripts.ScriptRegistry;
import com.denizenscript.denizencore.scripts.commands.AbstractCommand;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class AnnounceCommand extends AbstractCommand {

    // <--[command]
    // @Name Announce
    // @Syntax announce [<text>] (to_ops/to_console/to_flagged:<flag_name>) (format:<name>)
    // @Required 1
    // @Short Announces a message for everyone online to read.
    // @Group server
    //
    // @Description
    // Announce sends a raw message to players. Simply using announce with text will send
    // the message to all online players. Specifing the 'to_ops' argument will narrow down the players
    // in which the message is sent to ops only. Alternatively, using the 'to_flagged' argument
    // will send the message to players only if the specified flag does not equal true. You can also
    // use the 'to_console' argument to make it so it only shows in the server console. Announce
    // can also utilize a format script with the 'format' argument. See the format script-container
    // for more information.
    //
    // Note that the default announce mode (that shows for all players) relies on the Bukkit broadcast
    // system, which requires the permission "bukkit.broadcast.user" to see broadcasts.
    //
    // @Tags
    // None
    //
    // @Usage
    // Use to send an important message to your players.
    // - announce 'Warning! This server will restart in 5 minutes!'
    //
    // @Usage
    // Use to send a message to a specific 'group' of players.
    // - announce to_flagged:clan_subang '[<player.name>] Best clan ever!'
    //
    // @Usage
    // Use to easily send a message to all online ops.
    // - announce to_ops '<player.name> requires help!'
    //
    // @Usage
    // Use to send a message to just the console (Primarily for debugging / logging).
    // - announce to_console 'Warning- <player.name> broke a mob spawner at location <player.location>'
    // -->

    enum AnnounceType {ALL, TO_OPS, TO_FLAGGED, TO_CONSOLE}

    @Override
    public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {

        // Users tend to forget quotes sometimes on commands like this, so
        // let's check if there are more argument than usual.
        if (scriptEntry.getArguments().size() > 3) {
            throw new InvalidArgumentsException("Too many arguments! Did you forget a 'quote'?");
        }

        for (Argument arg : scriptEntry.getProcessedArgs()) {

            if (!scriptEntry.hasObject("type")
                    && arg.matches("to_ops")) {
                scriptEntry.addObject("type", AnnounceType.TO_OPS);
            }
            else if (!scriptEntry.hasObject("type")
                    && arg.matches("to_console")) {
                scriptEntry.addObject("type", AnnounceType.TO_CONSOLE);
            }
            else if (!scriptEntry.hasObject("type")
                    && arg.matchesPrefix("to_flagged")) {
                scriptEntry.addObject("type", AnnounceType.TO_FLAGGED);
                scriptEntry.addObject("flag", arg.asElement());
            }
            else if (!scriptEntry.hasObject("format")
                    && arg.matchesPrefix("format")) {
                FormatScriptContainer format = null;
                String formatStr = arg.getValue();
                format = ScriptRegistry.getScriptContainer(formatStr);
                if (format == null) {
                    Debug.echoError("Could not find format script matching '" + formatStr + '\'');
                }
                scriptEntry.addObject("format", format);
            }
            else if (!scriptEntry.hasObject("text")) {
                scriptEntry.addObject("text", new ElementTag(arg.raw_value));
            }

        }

        // If text is missing, alert the console.
        if (!scriptEntry.hasObject("text")) {
            throw new InvalidArgumentsException("Missing text argument!");
        }

        scriptEntry.defaultObject("type", AnnounceType.ALL);

    }

    @Override
    public void execute(ScriptEntry scriptEntry) {
        // Fetch objects
        ElementTag text = scriptEntry.getElement("text");
        AnnounceType type = (AnnounceType) scriptEntry.getObject("type");
        FormatScriptContainer format = (FormatScriptContainer) scriptEntry.getObject("format");
        ElementTag flag = scriptEntry.getElement("flag");

        // Report to dB
        if (scriptEntry.dbCallShouldDebug()) {
            Debug.report(scriptEntry, getName(),
                    ArgumentHelper.debugObj("Message", text)
                            + (format != null ? ArgumentHelper.debugObj("Format", format.getName()) : "")
                            + ArgumentHelper.debugObj("Type", type.name())
                            + (flag != null ? ArgumentHelper.debugObj("Flag_Name", flag) : ""));
        }

        String message = format != null ? format.getFormattedText(scriptEntry) : text.asString();

        // Use Bukkit to broadcast the message to everybody in the server.
        if (type == AnnounceType.ALL) {
            DenizenAPI.getCurrentInstance().getServer().spigot().broadcast(FormattedTextHelper.parse(message));
        }
        else if (type == AnnounceType.TO_OPS) {
            for (Player player : Bukkit.getOnlinePlayers()) {
                if (player.isOp()) {
                    player.spigot().sendMessage(FormattedTextHelper.parse(message));
                }
            }
        }
        else if (type == AnnounceType.TO_FLAGGED) {
            for (Player player : Bukkit.getOnlinePlayers()) {
                if (FlagManager.playerHasFlag(PlayerTag.mirrorBukkitPlayer(player), flag.asString())) {
                    player.spigot().sendMessage(FormattedTextHelper.parse(message));
                }
            }
        }
        else if (type == AnnounceType.TO_CONSOLE) {
            Bukkit.getServer().getConsoleSender().sendMessage(message);
        }
    }
}


