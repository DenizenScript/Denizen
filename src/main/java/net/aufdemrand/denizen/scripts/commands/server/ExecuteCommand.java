package net.aufdemrand.denizen.scripts.commands.server;

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

        String command = null;
        Type executeType = null;

        // Parse arguments
        for (String arg : scriptEntry.getArguments()) {

            if (aH.matchesArg("ASPLAYER, AS_PLAYER", arg))
                executeType = Type.AS_PLAYER;

            else if (aH.matchesArg("ASOPPLAYER, ASOP, AS_OP, AS_OP_PLAYER", arg))
                executeType = Type.AS_OP;

            else if (aH.matchesArg("ASNPC, AS_NPC", arg))
                executeType = Type.AS_NPC;

            else if (aH.matchesArg("ASSERVER, AS_SERVER", arg))
                executeType = Type.AS_SERVER;

            else command = arg;
        }

        if (executeType == null)
            throw new InvalidArgumentsException(Messages.ERROR_MISSING_OTHER, "EXECUTE_TYPE");

        if (executeType == Type.AS_NPC && scriptEntry.getNPC() == null)
            throw new InvalidArgumentsException("Must have a NPC link when using AS_NPC.");

        if ((executeType == Type.AS_OP || executeType == Type.AS_PLAYER)
                && scriptEntry.getPlayer() == null)
            throw new InvalidArgumentsException("Must have a Player link when using AS_OP or AS_PLAYER.");

        if (command == null)
            throw new InvalidArgumentsException(Messages.ERROR_MISSING_OTHER, "COMMAND_TEXT");

        scriptEntry.addObject("command", command)
                .addObject("type", executeType);

    }

    @Override
    public void execute(ScriptEntry scriptEntry) throws CommandExecutionException {

        String command = (String) scriptEntry.getObject("command");
        Type type = (Type) scriptEntry.getObject("type");

        // Report to dB
        dB.report(getName(),
                aH.debugObj("Type", type.toString())
                        + aH.debugObj("Command", command));

        switch (type) {

        case AS_PLAYER:
            scriptEntry.getPlayer().getPlayerEntity().performCommand(command);
            return;

        case AS_OP:
            boolean isOp = false;
            if (scriptEntry.getPlayer().getPlayerEntity().isOp()) isOp = true;
            if (!isOp) scriptEntry.getPlayer().getPlayerEntity().setOp(true);
            scriptEntry.getPlayer().getPlayerEntity().performCommand(command);
            if (!isOp) scriptEntry.getPlayer().getPlayerEntity().setOp(false);
            return;

        case AS_NPC:
            boolean should_despawn = false;
            if (!scriptEntry.getNPC().isSpawned()) {
                scriptEntry.getNPC().getCitizen()
                        .spawn(scriptEntry.getNPC().getCitizen()
                                .getTrait(CurrentLocation.class).getLocation());
                should_despawn = true;
            }
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
            if (should_despawn) scriptEntry.getNPC().getCitizen().despawn(DespawnReason.PLUGIN);
            return;

        case AS_SERVER:
            denizen.getServer().dispatchCommand(denizen.getServer().getConsoleSender(), command);
        }
    }

}
