package net.aufdemrand.denizen.scripts.commands.server;

import net.aufdemrand.denizen.BukkitScriptEntryData;
import net.aufdemrand.denizen.utilities.DenizenAPI;
import net.aufdemrand.denizen.utilities.DenizenCommandSender;
import net.aufdemrand.denizen.utilities.debugging.dB;
import net.aufdemrand.denizencore.exceptions.CommandExecutionException;
import net.aufdemrand.denizencore.exceptions.InvalidArgumentsException;
import net.aufdemrand.denizencore.objects.Element;
import net.aufdemrand.denizencore.objects.aH;
import net.aufdemrand.denizencore.objects.dList;
import net.aufdemrand.denizencore.scripts.ScriptEntry;
import net.aufdemrand.denizencore.scripts.commands.AbstractCommand;
import org.bukkit.Bukkit;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.server.ServerCommandEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ExecuteCommand extends AbstractCommand {

    enum Type {AS_SERVER, AS_NPC, AS_PLAYER, AS_OP}

    public DenizenCommandSender dcs = new DenizenCommandSender();
    public static final List<UUID> silencedPlayers = new ArrayList<UUID>();

    @Override
    public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {

        // Parse arguments
        for (aH.Argument arg : aH.interpret(scriptEntry.getArguments())) {

            if (arg.matches("ASPLAYER", "AS_PLAYER", "PLAYER")
                    && !scriptEntry.hasObject("type")) {
                if (!((BukkitScriptEntryData) scriptEntry.entryData).hasPlayer())
                    throw new InvalidArgumentsException("Must have a Player link when using AS_PLAYER.");
                scriptEntry.addObject("type", new Element("AS_PLAYER"));
            }

            else if (arg.matches("ASOPPLAYER", "ASOP", "AS_OP", "AS_OP_PLAYER", "OP")
                    && !scriptEntry.hasObject("type")) {
                if (!((BukkitScriptEntryData) scriptEntry.entryData).hasPlayer())
                    throw new InvalidArgumentsException("Must have a Player link when using AS_OP.");
                scriptEntry.addObject("type", new Element("AS_OP"));
            }

            else if (arg.matches("ASNPC", "AS_NPC", "NPC")
                    && !scriptEntry.hasObject("type")) {
                if (!((BukkitScriptEntryData) scriptEntry.entryData).hasNPC())
                    throw new InvalidArgumentsException("Must have a NPC link when using AS_NPC.");
                scriptEntry.addObject("type", new Element("AS_NPC"));
            }

            else if (arg.matches("ASSERVER", "AS_SERVER", "SERVER")
                    && !scriptEntry.hasObject("type"))
                scriptEntry.addObject("type", new Element("AS_SERVER"));

            else if (!scriptEntry.hasObject("silent")
                    && arg.matches("silent"))
                scriptEntry.addObject("silent", new Element("true"));

            else if (!scriptEntry.hasObject("command"))
                scriptEntry.addObject("command", new Element(arg.raw_value));

            else
                arg.reportUnhandled();
        }

        if (!scriptEntry.hasObject("type"))
            throw new InvalidArgumentsException("Missing execution type!");

        if (!scriptEntry.hasObject("command"))
            throw new InvalidArgumentsException("Missing command text!");

        scriptEntry.defaultObject("silent", new Element("false"));

    }

    @Override
    public void execute(ScriptEntry scriptEntry) throws CommandExecutionException {

        Element cmd = scriptEntry.getElement("command");
        Element type = scriptEntry.getElement("type");
        Element silent = scriptEntry.getElement("silent");

        // Report to dB
        dB.report(scriptEntry, getName(),
                type.debug()
                        + cmd.debug()
                        + silent.debug());

        String command = cmd.asString();

        switch (Type.valueOf(type.asString())) {

            case AS_PLAYER:
                try {
                    PlayerCommandPreprocessEvent pcpe = new PlayerCommandPreprocessEvent(((BukkitScriptEntryData) scriptEntry.entryData).getPlayer().getPlayerEntity(), "/" + command);
                    Bukkit.getPluginManager().callEvent(pcpe);
                    if (!pcpe.isCancelled()) {
                        boolean silentBool = silent.asBoolean();
                        Player player = ((BukkitScriptEntryData) scriptEntry.entryData).getPlayer().getPlayerEntity();
                        if (silentBool) {
                            silencedPlayers.add(player.getUniqueId());
                        }
                        player.performCommand(pcpe.getMessage().startsWith("/") ?
                                pcpe.getMessage().substring(1) : pcpe.getMessage());
                        if (silentBool) {
                            silencedPlayers.remove(player.getUniqueId());
                        }
                    }
                }
                catch (Throwable e) {
                    dB.echoError(scriptEntry.getResidingQueue(), "Exception while executing command as player.");
                    dB.echoError(scriptEntry.getResidingQueue(), e);
                }
                return;

            case AS_OP:
                boolean isOp = ((BukkitScriptEntryData) scriptEntry.entryData).getPlayer().getPlayerEntity().isOp();
                if (!isOp) ((BukkitScriptEntryData) scriptEntry.entryData).getPlayer().getPlayerEntity().setOp(true);
                try {
                    PlayerCommandPreprocessEvent pcpe = new PlayerCommandPreprocessEvent(((BukkitScriptEntryData) scriptEntry.entryData).getPlayer().getPlayerEntity(), "/" + command);
                    Bukkit.getPluginManager().callEvent(pcpe);
                    if (!pcpe.isCancelled()) {
                        boolean silentBool = silent.asBoolean();
                        Player player = ((BukkitScriptEntryData) scriptEntry.entryData).getPlayer().getPlayerEntity();
                        if (silentBool) {
                            silencedPlayers.add(player.getUniqueId());
                        }
                        player.performCommand(pcpe.getMessage().startsWith("/") ?
                                pcpe.getMessage().substring(1) : pcpe.getMessage());
                        if (silentBool) {
                            silencedPlayers.remove(player.getUniqueId());
                        }
                    }
                }
                catch (Throwable e) {
                    dB.echoError(scriptEntry.getResidingQueue(), "Exception while executing command as OP.");
                    dB.echoError(scriptEntry.getResidingQueue(), e);
                }
                if (!isOp) ((BukkitScriptEntryData) scriptEntry.entryData).getPlayer().getPlayerEntity().setOp(false);
                return;

            case AS_NPC:
                if (!((BukkitScriptEntryData) scriptEntry.entryData).getNPC().isSpawned()) {
                    dB.echoError(scriptEntry.getResidingQueue(), "Cannot EXECUTE AS_NPC unless the NPC is Spawned.");
                    return;
                }
                if (((BukkitScriptEntryData) scriptEntry.entryData).getNPC().getEntity().getType() != EntityType.PLAYER) {
                    dB.echoError(scriptEntry.getResidingQueue(), "Cannot EXECUTE AS_NPC unless the NPC is Player-Type.");
                    return;
                }
                ((Player) ((BukkitScriptEntryData) scriptEntry.entryData).getNPC().getEntity()).setOp(true);
                try {
                    ((Player) ((BukkitScriptEntryData) scriptEntry.entryData).getNPC().getEntity()).performCommand(command);
                }
                catch (Throwable e) {
                    dB.echoError(scriptEntry.getResidingQueue(), "Exception while executing command as NPC-OP.");
                    dB.echoError(scriptEntry.getResidingQueue(), e);
                }
                ((Player) ((BukkitScriptEntryData) scriptEntry.entryData).getNPC().getEntity()).setOp(false);
                return;

            case AS_SERVER:
                dcs.clearOutput();
                dcs.silent = silent.asBoolean();
                ServerCommandEvent sce = new ServerCommandEvent(dcs, command);
                Bukkit.getPluginManager().callEvent(sce);
                DenizenAPI.getCurrentInstance().getServer().dispatchCommand(dcs, sce.getCommand());
                scriptEntry.addObject("output", new dList(dcs.getOutput()));
        }
    }
}
