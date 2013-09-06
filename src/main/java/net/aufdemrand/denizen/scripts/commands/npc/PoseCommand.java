package net.aufdemrand.denizen.scripts.commands.npc;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import net.aufdemrand.denizen.exceptions.CommandExecutionException;
import net.aufdemrand.denizen.exceptions.InvalidArgumentsException;
import net.aufdemrand.denizen.objects.dNPC;
import net.aufdemrand.denizen.scripts.ScriptEntry;
import net.aufdemrand.denizen.scripts.commands.AbstractCommand;
import net.aufdemrand.denizen.objects.aH;
import net.aufdemrand.denizen.utilities.debugging.dB;
import net.aufdemrand.denizen.utilities.debugging.dB.Messages;
import net.citizensnpcs.trait.Poses;

/**
 *
 * TODO: Document usage
 *
 * Controls a NPC's 'Poses' trait.
 *
 * @author aufdemrand
 *
 */
public class PoseCommand extends AbstractCommand {

    private enum TargetType { NPC, PLAYER }
    private enum Action { ADD, REMOVE, ASSUME}

    @Override
    public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {

        Action action = Action.ASSUME;
        TargetType targetType = TargetType.NPC;
        String id = null;

        // Parse Arguments
        for (String arg : scriptEntry.getArguments()) {
            if (aH.matchesArg("ADD, ASSUME, REMOVE", arg)) {
                action = Action.valueOf(aH.getStringFrom(arg).toUpperCase());

            } else if (aH.matchesValueArg("ID", arg, aH.ArgumentType.String)) {
                id = aH.getStringFrom(arg);

            } else if (aH.matchesArg("PLAYER", arg)) {
                targetType = TargetType.PLAYER;
                    dB.echoDebug("Setting pose on PLAYER!");

            } else throw new InvalidArgumentsException(dB.Messages.ERROR_UNKNOWN_ARGUMENT);

        }

        // If TARGET is NPC/PLAYER and no NPC/PLAYER available, throw exception.
        if (targetType == TargetType.PLAYER && scriptEntry.getPlayer() == null) throw new InvalidArgumentsException(Messages.ERROR_NO_PLAYER);
        else if (targetType == TargetType.NPC && scriptEntry.getNPC() == null) throw new InvalidArgumentsException(Messages.ERROR_NO_NPCID);
        scriptEntry.addObject("target", targetType)
                .addObject("action", action).addObject("id", id);
    }

    @SuppressWarnings("incomplete-switch")
    @Override
    public void execute(ScriptEntry scriptEntry) throws CommandExecutionException {
        // Get objects

        TargetType target = (TargetType) scriptEntry.getObject("target");
        dNPC npc = scriptEntry.getNPC();
        Action action = (Action) scriptEntry.getObject("action");
        String id = (String) scriptEntry.getObject("id");

        // Report to dB
        dB.report(getName(),
                aH.debugObj(target.toString(), npc.toString())
                        + aH.debugObj("Action", action.toString())
                        + aH.debugObj("Id", id));

        switch (action) {

            case ASSUME:

                if (target.name().equals("NPC"))
                    npc.getCitizen().getTrait(Poses.class).assumePose(id);
                else {
                    Player player = scriptEntry.getPlayer().getPlayerEntity();
                    Location location = player.getLocation();
                    location.setYaw(npc.getCitizen().getTrait(Poses.class).getPose(id).getYaw());
                    location.setPitch(npc.getCitizen().getTrait(Poses.class).getPose(id).getPitch());

                    // The only way to change a player's yaw and pitch in Bukkit
                    // is to use teleport on him/her
                    player.teleport(location);
                }
        }

        // TODO: ADD ADD/REMOVE

    }
}
