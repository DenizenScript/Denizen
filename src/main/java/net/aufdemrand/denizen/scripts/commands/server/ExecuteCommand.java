package net.aufdemrand.denizen.scripts.commands.server;

import net.aufdemrand.denizen.objects.Element;
import net.aufdemrand.denizen.objects.dList;
import net.aufdemrand.denizen.utilities.DenizenCommandSender;
import org.bukkit.Bukkit;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

import net.aufdemrand.denizen.exceptions.CommandExecutionException;
import net.aufdemrand.denizen.exceptions.InvalidArgumentsException;
import net.aufdemrand.denizen.scripts.ScriptEntry;
import net.aufdemrand.denizen.scripts.commands.AbstractCommand;
import net.aufdemrand.denizen.objects.aH;
import net.aufdemrand.denizen.utilities.debugging.dB;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.server.ServerCommandEvent;

public class ExecuteCommand extends AbstractCommand {

    enum Type { AS_SERVER, AS_NPC, AS_PLAYER, AS_OP }

    public DenizenCommandSender dcs = new DenizenCommandSender();

    @Override
    public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {

        // Parse arguments
        for (aH.Argument arg : aH.interpret(scriptEntry.getArguments())) {

            if (arg.matches("ASPLAYER, AS_PLAYER, PLAYER")
                    && !scriptEntry.hasObject("type")) {
                if (!scriptEntry.hasPlayer())
                    throw new InvalidArgumentsException("Must have a Player link when using AS_PLAYER.");
                scriptEntry.addObject("type", new Element("AS_PLAYER"));
            }

            else if (arg.matches("ASOPPLAYER, ASOP, AS_OP, AS_OP_PLAYER, OP")
                    && !scriptEntry.hasObject("type")) {
                if (!scriptEntry.hasPlayer())
                    throw new InvalidArgumentsException("Must have a Player link when using AS_OP.");
                scriptEntry.addObject("type", new Element("AS_OP"));
            }

            else if (arg.matches("ASNPC, AS_NPC, NPC")
                    && !scriptEntry.hasObject("type")) {
                if (!scriptEntry.hasNPC())
                    throw new InvalidArgumentsException("Must have a NPC link when using AS_NPC.");
                scriptEntry.addObject("type", new Element("AS_NPC"));
            }

            else if (arg.matches("ASSERVER, AS_SERVER, SERVER")
                    && !scriptEntry.hasObject("type"))
                scriptEntry.addObject("type", new Element("AS_SERVER"));

            else if (!scriptEntry.hasObject("command"))
                scriptEntry.addObject("command", new Element(arg.raw_value));

            else
                arg.reportUnhandled();
        }

        if (!scriptEntry.hasObject("type"))
            throw new InvalidArgumentsException("Missing execution type!");

        if (!scriptEntry.hasObject("command"))
            throw new InvalidArgumentsException("Missing command text!");

    }

    @Override
    public void execute(ScriptEntry scriptEntry) throws CommandExecutionException {

        Element cmd = scriptEntry.getElement("command");
        Element type = scriptEntry.getElement("type");

        // Report to dB
        dB.report(scriptEntry, getName(),
                type.debug()
                + cmd.debug());

        String command = cmd.asString();

        switch (Type.valueOf(type.asString())) {

        case AS_PLAYER:
            try {
                PlayerCommandPreprocessEvent pcpe = new PlayerCommandPreprocessEvent(scriptEntry.getPlayer().getPlayerEntity(), "/" + command);
                Bukkit.getPluginManager().callEvent(pcpe);
                if (!pcpe.isCancelled())
                    scriptEntry.getPlayer().getPlayerEntity().performCommand(
                            pcpe.getMessage().startsWith("/") ? pcpe.getMessage().substring(1): pcpe.getMessage());
            }
            catch (Throwable e) {
                dB.echoError(scriptEntry.getResidingQueue(), "Exception while executing command as player.");
                dB.echoError(scriptEntry.getResidingQueue(), e);
            }
            return;

        case AS_OP:
            boolean isOp = scriptEntry.getPlayer().getPlayerEntity().isOp();
            if (!isOp) scriptEntry.getPlayer().getPlayerEntity().setOp(true);
            try {
                PlayerCommandPreprocessEvent pcpe = new PlayerCommandPreprocessEvent(scriptEntry.getPlayer().getPlayerEntity(), "/" + command);
                Bukkit.getPluginManager().callEvent(pcpe);
                if (!pcpe.isCancelled())
                    scriptEntry.getPlayer().getPlayerEntity().performCommand(
                            pcpe.getMessage().startsWith("/") ? pcpe.getMessage().substring(1): pcpe.getMessage());
            }
            catch (Throwable e) {
                dB.echoError(scriptEntry.getResidingQueue(), "Exception while executing command as OP.");
                dB.echoError(scriptEntry.getResidingQueue(), e);
            }
            if (!isOp) scriptEntry.getPlayer().getPlayerEntity().setOp(false);
            return;

        case AS_NPC:
            if (!scriptEntry.getNPC().isSpawned()) {
                dB.echoError(scriptEntry.getResidingQueue(), "Cannot EXECUTE AS_NPC unless the NPC is Spawned.");
                return;
            }
            if (scriptEntry.getNPC().getEntity().getType() != EntityType.PLAYER) {
                dB.echoError(scriptEntry.getResidingQueue(), "Cannot EXECUTE AS_NPC unless the NPC is Player-Type.");
                return;
            }
            ((Player) scriptEntry.getNPC().getEntity()).setOp(true);
            try {
                ((Player) scriptEntry.getNPC().getEntity()).performCommand(command);
            }
            catch (Throwable e) {
                dB.echoError(scriptEntry.getResidingQueue(), "Exception while executing command as NPC-OP.");
                dB.echoError(scriptEntry.getResidingQueue(), e);
            }
            ((Player) scriptEntry.getNPC().getEntity()).setOp(false);
            return;

        case AS_SERVER:
            dcs.clearOutput();
            ServerCommandEvent sce = new ServerCommandEvent(dcs, command);
            Bukkit.getPluginManager().callEvent(sce);
            denizen.getServer().dispatchCommand(dcs, sce.getCommand());
            scriptEntry.addObject("output", new dList(dcs.getOutput()));
        }
    }

}
