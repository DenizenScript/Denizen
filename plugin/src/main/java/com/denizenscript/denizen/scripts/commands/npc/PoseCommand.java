package com.denizenscript.denizen.scripts.commands.npc;

import com.denizenscript.denizen.utilities.Utilities;
import com.denizenscript.denizencore.utilities.debugging.Debug;
import com.denizenscript.denizen.objects.LocationTag;
import com.denizenscript.denizen.objects.NPCTag;
import com.denizenscript.denizencore.exceptions.InvalidArgumentsException;
import com.denizenscript.denizencore.objects.Argument;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.scripts.ScriptEntry;
import com.denizenscript.denizencore.scripts.commands.AbstractCommand;
import net.citizensnpcs.trait.Poses;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class PoseCommand extends AbstractCommand {

    public PoseCommand() {
        setName("pose");
        setSyntax("pose (add/remove/{assume}) [id:<name>] (player/{npc}) (<location>)");
        setRequiredArguments(1, 4);
        isProcedural = false;
    }

    // <--[command]
    // @Name Pose
    // @Syntax pose (add/remove/{assume}) [id:<name>] (player/{npc}) (<location>)
    // @Required 1
    // @Maximum 4
    // @Plugin Citizens
    // @Short Rotates the player or NPC to match a pose, or adds/removes an NPC's poses.
    // @Group npc
    //
    // @Description
    // Makes a player or NPC assume the position of a pose saved on an NPC, removes a
    // pose with a specified ID from the current linked NPC, or adds a pose to the NPC
    // with an ID and a location, although the only thing that matters in the location
    // is the pitch and yaw.
    //
    // @Tags
    // <NPCTag.has_pose[<name>]>
    // <NPCTag.pose[<name>]>
    //
    // @Usage
    // Make an NPC assume a pose.
    // - pose id:MyPose1
    //
    // @Usage
    // Add a pose to an NPC. (Note that only the last 2 numbers matter)
    // - pose add id:MyPose2 0,0,0,-2.3,5.4
    //
    // @Usage
    // Remove a pose from an NPC.
    // - pose remove id:MyPose1
    // -->

    private enum TargetType {NPC, PLAYER}

    private enum Action {ADD, REMOVE, ASSUME}

    @Override
    public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {
        for (Argument arg : scriptEntry) {
            if (arg.matches("add", "assume", "remove")) {
                scriptEntry.addObject("action", Action.valueOf(arg.getValue().toUpperCase()));
            }
            else if (arg.matchesPrefix("id")) {
                scriptEntry.addObject("pose_id", arg.asElement());
            }
            else if (arg.matches("player")) {
                scriptEntry.addObject("target", TargetType.PLAYER);
            }
            else if (arg.matchesArgumentType(LocationTag.class)) {
                scriptEntry.addObject("pose_loc", arg.asType(LocationTag.class));
            }
        }
        // Even if the target is a player, this command requires an NPC to get the pose from.
        if (!Utilities.entryHasNPC(scriptEntry)) {
            throw new InvalidArgumentsException("This command requires an NPC!");
        }
        if (!scriptEntry.hasObject("pose_id")) {
            throw new InvalidArgumentsException("No ID specified!");
        }
        scriptEntry.defaultObject("target", TargetType.NPC);
        scriptEntry.defaultObject("action", Action.ASSUME);
        // If the target is a player, it needs a player! However, you can't ADD/REMOVE poses from players, so only allow ASSUME.
        if (scriptEntry.getObject("target") == TargetType.PLAYER) {
            if (scriptEntry.getObject("action") != Action.ASSUME) {
                throw new InvalidArgumentsException("You cannot add or remove poses from a player.");
            }
            else if (!Utilities.entryHasPlayer(scriptEntry)) {
                throw new InvalidArgumentsException("This command requires a linked player!");
            }
        }
    }

    @Override
    public void execute(ScriptEntry scriptEntry) {
        TargetType target = (TargetType) scriptEntry.getObject("target");
        NPCTag npc = Utilities.getEntryNPC(scriptEntry);
        Action action = (Action) scriptEntry.getObject("action");
        ElementTag idElement = scriptEntry.getElement("pose_id");
        LocationTag pose_loc = scriptEntry.getObjectTag("pose_loc");
        if (scriptEntry.dbCallShouldDebug()) {
            Debug.report(scriptEntry, getName(), db("Target", target), (target == TargetType.PLAYER ? Utilities.getEntryPlayer(scriptEntry) : ""), npc, db("Action", action), idElement, pose_loc);
        }
        if (!npc.getCitizen().hasTrait(Poses.class)) {
            npc.getCitizen().addTrait(Poses.class);
        }
        Poses poses = npc.getCitizen().getOrAddTrait(Poses.class);
        String id = idElement.asString();
        switch (action) {
            case ASSUME:
                if (!poses.hasPose(id)) {
                    Debug.echoError("Pose \"" + id + "\" doesn't exist for " + npc);
                }
                if (target.name().equals("NPC")) {
                    poses.assumePose(id);
                }
                else {
                    Player player = Utilities.getEntryPlayer(scriptEntry).getPlayerEntity();
                    Location location = player.getLocation();
                    location.setYaw(poses.getPose(id).getYaw());
                    location.setPitch(poses.getPose(id).getPitch());
                    // The only way to change a player's yaw and pitch in Bukkit is to use teleport on them
                    player.teleport(location);
                }
                break;
            case ADD:
                if (!poses.addPose(id, pose_loc)) {
                    Debug.echoError(npc + " already has that pose!");
                }
                break;
            case REMOVE:
                if (!poses.removePose(id)) {
                    Debug.echoError(npc + " does not have that pose!");
                }
                break;
        }
    }
}
