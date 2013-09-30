package net.aufdemrand.denizen.scripts.commands.server;

import net.aufdemrand.denizen.objects.Element;
import net.citizensnpcs.api.event.DespawnReason;
import net.citizensnpcs.trait.CurrentLocation;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

import net.aufdemrand.denizen.exceptions.CommandExecutionException;
import net.aufdemrand.denizen.exceptions.InvalidArgumentsException;
import net.aufdemrand.denizen.scripts.ScriptEntry;
import net.aufdemrand.denizen.scripts.commands.AbstractCommand;
import net.aufdemrand.denizen.objects.aH;
import net.aufdemrand.denizen.utilities.debugging.dB;
import net.aufdemrand.denizen.utilities.debugging.dB.Messages;

public class ExecuteCommand extends AbstractCommand {

    enum Type { AS_SERVER, AS_NPC, AS_PLAYER, AS_OP }

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
                throw new InvalidArgumentsException(Messages.ERROR_UNKNOWN_ARGUMENT, arg.raw_value);
        }

        if (!scriptEntry.hasObject("type"))
            throw new InvalidArgumentsException(Messages.ERROR_MISSING_OTHER, "EXECUTE_TYPE");

        if (!scriptEntry.hasObject("command"))
            throw new InvalidArgumentsException(Messages.ERROR_MISSING_OTHER, "COMMAND_TEXT");

    }

    @Override
    public void execute(ScriptEntry scriptEntry) throws CommandExecutionException {

        Element cmd = scriptEntry.getElement("command");
        Element type = scriptEntry.getElement("type");

        // Report to dB
        dB.report(getName(),
                type.debug()
                + cmd.debug());

        String command = cmd.asString();

        switch (Type.valueOf(type.asString())) {

        case AS_PLAYER:
            scriptEntry.getPlayer().getPlayerEntity().performCommand(command);
            return;

        case AS_OP:
            boolean isOp = scriptEntry.getPlayer().getPlayerEntity().isOp();
            if (!isOp) scriptEntry.getPlayer().getPlayerEntity().setOp(true);
            scriptEntry.getPlayer().getPlayerEntity().performCommand(command);
            if (!isOp) scriptEntry.getPlayer().getPlayerEntity().setOp(false);
            return;

        case AS_NPC:
            if (!scriptEntry.getNPC().isSpawned()) {
                dB.echoError("Cannot EXECUTE AS_NPC unless the NPC is Spawned.");
                return;
            }
            if (scriptEntry.getNPC().getEntity().getType() != EntityType.PLAYER) {
                dB.echoError("Cannot EXECUTE AS_NPC unless the NPC is Player-Type.");
                return;
            }
            ((Player) scriptEntry.getNPC().getEntity()).setOp(true);
            ((Player) scriptEntry.getNPC().getEntity()).performCommand(command);
            ((Player) scriptEntry.getNPC().getEntity()).setOp(false);
            return;

        case AS_SERVER:
            denizen.getServer().dispatchCommand(denizen.getServer().getConsoleSender(), command);
        }
    }

}
