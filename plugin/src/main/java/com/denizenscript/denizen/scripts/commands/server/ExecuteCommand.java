package com.denizenscript.denizen.scripts.commands.server;

import com.denizenscript.denizen.Denizen;
import com.denizenscript.denizen.utilities.command.scripted.DenizenCommandSender;
import com.denizenscript.denizen.utilities.Utilities;
import com.denizenscript.denizencore.utilities.debugging.Debug;
import com.denizenscript.denizen.nms.NMSHandler;
import com.denizenscript.denizen.utilities.packets.NetworkInterceptHelper;
import com.denizenscript.denizencore.exceptions.InvalidArgumentsException;
import com.denizenscript.denizencore.objects.Argument;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.core.ListTag;
import com.denizenscript.denizencore.scripts.ScriptEntry;
import com.denizenscript.denizencore.scripts.commands.AbstractCommand;
import com.denizenscript.denizencore.utilities.CoreUtilities;
import org.bukkit.Bukkit;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.server.ServerCommandEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ExecuteCommand extends AbstractCommand {

    public ExecuteCommand() {
        setName("execute");
        setSyntax("execute [as_player/as_op/as_npc/as_server] [<Bukkit-command>] (silent)");
        setRequiredArguments(2, 3);
        isProcedural = false;
        setBooleansHandled("silent");
    }

    // <--[command]
    // @Name Execute
    // @Syntax execute [as_player/as_op/as_npc/as_server] [<Bukkit-command>] (silent)
    // @Required 2
    // @Maximum 3
    // @Short Executes an arbitrary server command as if the player, NPC, or server typed it in.
    // @Group server
    //
    // @Description
    // Allows the execution of server commands through a Denizen script.
    // Commands can be executed as the server, as an npc, as an opped player, or as a player, as though it was typed by the respective source.
    //
    // Note that you should generally avoid using 'as_op', which is only meant for very specific special cases. 'as_server' is usually a better option.
    //
    // Note: do not include the slash at the start. A slash at the start will be interpreted equivalent to typing two slashes at the front in-game.
    //
    // Note that this is a Denizen script command that executes Bukkit commands.
    // This can be considered the inverse of '/ex' (a Bukkit command that executes Denizen script commands).
    //
    // The 'silent' option can be specified with 'as_server' to hide the output. Note that 'silent' might or might not work with different plugins depending on how they operate.
    // It can also be used with 'as_player' or 'as_op' to use network interception to silence the command output to player chat.
    //
    // Generally, you should never use this to execute a vanilla command, there is almost always a script command that should be used instead.
    // Usually the 'execute' command should be reserved for interacting with external plugins.
    //
    // @Tags
    // <entry[saveName].output> returns the output to an as_server sender.
    //
    // @Usage
    // Use to execute the save-all command as the server.
    // - execute as_server "save-all"
    //
    // @Usage
    // Use to make the linked (non-op) player execute a command that normally only ops can use. Generally avoid ever doing this.
    // - execute as_op "staffsay hi"
    // -->

    enum Type {AS_SERVER, AS_NPC, AS_PLAYER, AS_OP}

    public DenizenCommandSender dcs = new DenizenCommandSender();
    public static final List<UUID> silencedPlayers = new ArrayList<>();

    @Override
    public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {
        for (Argument arg : scriptEntry) {
            if (arg.matches("asplayer", "as_player")
                    && !scriptEntry.hasObject("type")) {
                if (!Utilities.entryHasPlayer(scriptEntry)) {
                    throw new InvalidArgumentsException("Must have a Player link when using AS_PLAYER.");
                }
                scriptEntry.addObject("type", new ElementTag("AS_PLAYER"));
            }
            else if (arg.matches("asop", "as_op")
                    && !scriptEntry.hasObject("type")) {
                if (!Utilities.entryHasPlayer(scriptEntry)) {
                    throw new InvalidArgumentsException("Must have a Player link when using AS_OP.");
                }
                scriptEntry.addObject("type", new ElementTag("AS_OP"));
            }
            else if (arg.matches("asnpc", "as_npc")
                    && !scriptEntry.hasObject("type")) {
                if (!Utilities.entryHasNPC(scriptEntry)) {
                    throw new InvalidArgumentsException("Must have a NPC link when using AS_NPC.");
                }
                scriptEntry.addObject("type", new ElementTag("AS_NPC"));
            }
            else if (arg.matches("asserver", "as_server")
                    && !scriptEntry.hasObject("type")) {
                scriptEntry.addObject("type", new ElementTag("AS_SERVER"));
            }
            else if (!scriptEntry.hasObject("command")) {
                scriptEntry.addObject("command", arg.getRawElement());
            }
            else {
                arg.reportUnhandled();
            }
        }
        if (!scriptEntry.hasObject("type")) {
            throw new InvalidArgumentsException("Missing execution type!");
        }
        if (!scriptEntry.hasObject("command")) {
            throw new InvalidArgumentsException("Missing command text!");
        }
    }

    @Override
    public void execute(ScriptEntry scriptEntry) {
        ElementTag cmd = scriptEntry.getElement("command");
        ElementTag type = scriptEntry.getElement("type");
        boolean silent = scriptEntry.argAsBoolean("silent");
        if (scriptEntry.dbCallShouldDebug()) {
            Debug.report(scriptEntry, getName(), type, cmd, db("silent", silent));
        }
        String command = cmd.asString();
        switch (Type.valueOf(type.asString())) {
            case AS_PLAYER:
                try {
                    PlayerCommandPreprocessEvent pcpe = new PlayerCommandPreprocessEvent(Utilities.getEntryPlayer(scriptEntry).getPlayerEntity(), "/" + command);
                    Bukkit.getPluginManager().callEvent(pcpe);
                    if (!pcpe.isCancelled()) {
                        Player player = Utilities.getEntryPlayer(scriptEntry).getPlayerEntity();
                        if (silent) {
                            NetworkInterceptHelper.enable();
                            silencedPlayers.add(player.getUniqueId());
                        }
                        player.performCommand(pcpe.getMessage().startsWith("/") ?
                                pcpe.getMessage().substring(1) : pcpe.getMessage());
                        if (silent) {
                            silencedPlayers.remove(player.getUniqueId());
                        }
                    }
                }
                catch (Throwable e) {
                    Debug.echoError(scriptEntry, "Exception while executing command as player.");
                    Debug.echoError(scriptEntry, e);
                }
                break;
            case AS_OP:
                if (CoreUtilities.equalsIgnoreCase(command, "stop")) {
                    Debug.echoError("Please use as_server to execute 'stop'.");
                    return;
                }
                Player player = Utilities.getEntryPlayer(scriptEntry).getPlayerEntity();
                boolean isOp = player.isOp();
                if (!isOp) {
                    NMSHandler.playerHelper.setTemporaryOp(player, true);
                }
                try {
                    PlayerCommandPreprocessEvent pcpe = new PlayerCommandPreprocessEvent(player, "/" + command);
                    Bukkit.getPluginManager().callEvent(pcpe);
                    if (!pcpe.isCancelled()) {
                        if (silent) {
                            NetworkInterceptHelper.enable();
                            silencedPlayers.add(player.getUniqueId());
                        }
                        player.performCommand(pcpe.getMessage().startsWith("/") ?
                                pcpe.getMessage().substring(1) : pcpe.getMessage());
                        if (silent) {
                            silencedPlayers.remove(player.getUniqueId());
                        }
                    }
                }
                catch (Throwable e) {
                    Debug.echoError(scriptEntry, "Exception while executing command as OP.");
                    Debug.echoError(scriptEntry, e);
                }
                if (!isOp) {
                    NMSHandler.playerHelper.setTemporaryOp(player, false);
                }
                break;
            case AS_NPC:
                if (!Utilities.getEntryNPC(scriptEntry).isSpawned()) {
                    Debug.echoError(scriptEntry, "Cannot EXECUTE AS_NPC unless the NPC is Spawned.");
                    return;
                }
                if (Utilities.getEntryNPC(scriptEntry).getEntity().getType() != EntityType.PLAYER) {
                    Debug.echoError(scriptEntry, "Cannot EXECUTE AS_NPC unless the NPC is Player-Type.");
                    return;
                }
                Utilities.getEntryNPC(scriptEntry).getEntity().setOp(true);
                try {
                    ((Player) Utilities.getEntryNPC(scriptEntry).getEntity()).performCommand(command);
                }
                catch (Throwable e) {
                    Debug.echoError(scriptEntry, "Exception while executing command as NPC-OP.");
                    Debug.echoError(scriptEntry, e);
                }
                Utilities.getEntryNPC(scriptEntry).getEntity().setOp(false);
                break;
            case AS_SERVER:
                dcs.clearOutput();
                dcs.silent = silent;
                ServerCommandEvent sce = new ServerCommandEvent(dcs, command);
                Bukkit.getPluginManager().callEvent(sce);
                Denizen.getInstance().getServer().dispatchCommand(dcs, sce.getCommand());
                scriptEntry.saveObject("output", new ListTag(dcs.getOutput()));
                break;
        }
    }
}
