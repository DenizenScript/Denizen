package com.denizenscript.denizen.scripts.commands.server;

import com.denizenscript.denizen.Denizen;
import com.denizenscript.denizen.scripts.containers.core.FormatScriptContainer;
import com.denizenscript.denizen.utilities.FormattedTextHelper;
import com.denizenscript.denizencore.utilities.debugging.Debug;
import com.denizenscript.denizen.objects.PlayerTag;
import com.denizenscript.denizencore.exceptions.InvalidArgumentsException;
import com.denizenscript.denizencore.objects.Argument;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.scripts.ScriptEntry;
import com.denizenscript.denizencore.scripts.ScriptRegistry;
import com.denizenscript.denizencore.scripts.commands.AbstractCommand;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class AnnounceCommand extends AbstractCommand {

    public AnnounceCommand() {
        setName("announce");
        setSyntax("announce [<text>] (to_ops/to_console/to_flagged:<flag_name>/to_permission:<node>) (format:<script>)");
        setRequiredArguments(1, 3);
        isProcedural = true;
    }

    // <--[command]
    // @Name Announce
    // @Syntax announce [<text>] (to_ops/to_console/to_flagged:<flag_name>/to_permission:<node>) (format:<script>)
    // @Required 1
    // @Maximum 3
    // @Short Announces a message for everyone online to read.
    // @Group server
    //
    // @Description
    // Announce sends a raw message to players.
    // Simply using announce with text will send the message to all online players using the Spigot broadcast system.
    // Specifying the 'to_ops' argument will narrow down the players in which the message is sent to ops only.
    // Alternatively, using the 'to_permission' argument will send the message to only players that have the specified permission node.
    // Or, using the 'to_flagged' argument will send the message to only players that have the specified flag.
    // You can also use the 'to_console' argument to make it so it only shows in the server console.
    //
    // Announce can also utilize a format script with the 'format' argument. See <@link language Format Script Containers>.
    //
    // Note that the default announce mode (that shows for all players) relies on the Spigot broadcast system, which requires the permission "bukkit.broadcast.user" to see broadcasts.
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

    enum AnnounceType {ALL, TO_OPS, TO_FLAGGED, TO_CONSOLE, TO_PERMISSION}

    @Override
    public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {
        for (Argument arg : scriptEntry) {
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
            else if (!scriptEntry.hasObject("type")
                    && arg.matchesPrefix("to_permission")) {
                scriptEntry.addObject("type", AnnounceType.TO_PERMISSION);
                scriptEntry.addObject("flag", arg.asElement());
            }
            else if (!scriptEntry.hasObject("format")
                    && arg.matchesPrefix("format")) {
                FormatScriptContainer format;
                String formatStr = arg.getValue();
                format = ScriptRegistry.getScriptContainer(formatStr);
                if (format == null) {
                    Debug.echoError("Could not find format script matching '" + formatStr + '\'');
                }
                scriptEntry.addObject("format", format);
            }
            else if (!scriptEntry.hasObject("text")) {
                scriptEntry.addObject("text", arg.getRawElement());
            }
            else {
                arg.reportUnhandled();
            }
        }
        if (!scriptEntry.hasObject("text")) {
            throw new InvalidArgumentsException("Missing text argument!");
        }
        scriptEntry.defaultObject("type", AnnounceType.ALL);
    }

    @Override
    public void execute(ScriptEntry scriptEntry) {
        if (scriptEntry.getResidingQueue().procedural) {
            Debug.echoError("'Announce' should not be used in a procedure script. Consider the 'debug' command instead.");
        }
        ElementTag text = scriptEntry.getElement("text");
        AnnounceType type = (AnnounceType) scriptEntry.getObject("type");
        FormatScriptContainer format = (FormatScriptContainer) scriptEntry.getObject("format");
        ElementTag flag = scriptEntry.getElement("flag");
        if (scriptEntry.dbCallShouldDebug()) {
            Debug.report(scriptEntry, getName(), db("message", text), (format != null ? db("format", format.getName()) : ""), db("type", type.name()), flag);
        }
        String message = format != null ? format.getFormattedText(text.asString(), scriptEntry) : text.asString();
        // Use Bukkit to broadcast the message to everybody in the server.
        switch (type) {
            case ALL:
                Denizen.getInstance().getServer().spigot().broadcast(FormattedTextHelper.parse(message, ChatColor.WHITE));
                break;
            case TO_OPS:
                for (Player player : Bukkit.getOnlinePlayers()) {
                    if (player.isOp()) {
                        player.spigot().sendMessage(FormattedTextHelper.parse(message, ChatColor.WHITE));
                    }
                }
                break;
            case TO_PERMISSION:
                for (Player player : Bukkit.getOnlinePlayers()) {
                    if (player.hasPermission(flag.asString())) {
                        player.spigot().sendMessage(FormattedTextHelper.parse(message, ChatColor.WHITE));
                    }
                }
            case TO_FLAGGED:
                for (Player player : Bukkit.getOnlinePlayers()) {
                    if (new PlayerTag(player).getFlagTracker().hasFlag(flag.asString())) {
                        player.spigot().sendMessage(FormattedTextHelper.parse(message, ChatColor.WHITE));
                    }
                }
                break;
            case TO_CONSOLE:
                Bukkit.getServer().getConsoleSender().spigot().sendMessage(FormattedTextHelper.parse(message, ChatColor.WHITE));
                break;
        }
    }
}
