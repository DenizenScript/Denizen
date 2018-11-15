package net.aufdemrand.denizen.scripts.commands.npc;

import net.aufdemrand.denizen.BukkitScriptEntryData;
import net.aufdemrand.denizen.objects.dLocation;
import net.aufdemrand.denizen.objects.dNPC;
import net.aufdemrand.denizen.utilities.debugging.dB;
import net.aufdemrand.denizencore.exceptions.CommandExecutionException;
import net.aufdemrand.denizencore.exceptions.InvalidArgumentsException;
import net.aufdemrand.denizencore.objects.aH;
import net.aufdemrand.denizencore.scripts.ScriptEntry;
import net.aufdemrand.denizencore.scripts.commands.AbstractCommand;
import net.citizensnpcs.trait.Poses;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class PoseCommand extends AbstractCommand {

    private enum TargetType {NPC, PLAYER}

    private enum Action {ADD, REMOVE, ASSUME}

    @Override
    public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {

        // Parse Arguments
        for (aH.Argument arg : aH.interpret(scriptEntry.getArguments())) {

            if (arg.matches("add", "assume", "remove")) {
                scriptEntry.addObject("action", Action.valueOf(arg.getValue().toUpperCase()));
            }
            else if (arg.matchesPrefix("id")) {
                scriptEntry.addObject("pose_id", arg.getValue());
            }
            else if (arg.matches("player")) {
                scriptEntry.addObject("target", TargetType.PLAYER);
            }
            else if (arg.matchesArgumentType(dLocation.class)) {
                scriptEntry.addObject("pose_loc", arg.asType(dLocation.class));
            }

        }

        // Even if the target is a player, this command requires an NPC to get the pose from.
        if (!((BukkitScriptEntryData) scriptEntry.entryData).hasNPC()) {
            throw new InvalidArgumentsException("This command requires an NPC!");
        }

        // It also requires a pose ID
        if (!scriptEntry.hasObject("pose_id")) {
            throw new InvalidArgumentsException("No ID specified!");
        }

        // Set default objects
        scriptEntry.defaultObject("target", TargetType.NPC);
        scriptEntry.defaultObject("action", Action.ASSUME);

        // If the target is a player, it needs a player! However, you can't ADD/REMOVE poses
        // from players, so only allow ASSUME.
        if (scriptEntry.getObject("target") == TargetType.PLAYER) {
            if (scriptEntry.getObject("action") != Action.ASSUME) {
                throw new InvalidArgumentsException("You cannot add or remove poses from a player.");
            }
            else if (!((BukkitScriptEntryData) scriptEntry.entryData).hasPlayer()) {
                throw new InvalidArgumentsException("This command requires a linked player!");
            }
        }

    }

    @Override
    public void execute(ScriptEntry scriptEntry) throws CommandExecutionException {

        // Get objects
        TargetType target = (TargetType) scriptEntry.getObject("target");
        dNPC npc = ((BukkitScriptEntryData) scriptEntry.entryData).getNPC();
        Action action = (Action) scriptEntry.getObject("action");
        String id = (String) scriptEntry.getObject("pose_id");
        dLocation pose_loc = (dLocation) scriptEntry.getObject("pose_loc");

        // Report to dB
        if (scriptEntry.dbCallShouldDebug()) {
            dB.report(scriptEntry, getName(),
                    aH.debugObj("Target", target.toString())
                            + (target == TargetType.PLAYER ? ((BukkitScriptEntryData) scriptEntry.entryData).getPlayer().debug() : "")
                            + npc.debug()
                            + aH.debugObj("Action", action.toString())
                            + aH.debugObj("Id", id)
                            + (pose_loc != null ? pose_loc.debug() : ""));
        }

        if (!npc.getCitizen().hasTrait(Poses.class)) {
            npc.getCitizen().addTrait(Poses.class);
        }

        Poses poses = npc.getCitizen().getTrait(Poses.class);

        switch (action) {

            case ASSUME:
                if (!poses.hasPose(id)) {
                    throw new CommandExecutionException("Pose \"" + id + "\" doesn't exist for " + npc.toString());
                }

                if (target.name().equals("NPC")) {
                    poses.assumePose(id);
                }
                else {
                    Player player = ((BukkitScriptEntryData) scriptEntry.entryData).getPlayer().getPlayerEntity();
                    Location location = player.getLocation();
                    location.setYaw(poses.getPose(id).getYaw());
                    location.setPitch(poses.getPose(id).getPitch());

                    // The only way to change a player's yaw and pitch in Bukkit
                    // is to use teleport on him/her
                    player.teleport(location);
                }
                break;

            case ADD:
                if (!poses.addPose(id, pose_loc)) {
                    throw new CommandExecutionException(npc.toString() + " already has that pose!");
                }
                break;

            case REMOVE:
                if (!poses.removePose(id)) {
                    throw new CommandExecutionException(npc.toString() + " does not have that pose!");
                }
                break;

        }

    }
}
