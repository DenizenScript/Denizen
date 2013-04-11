package net.aufdemrand.denizen.scripts.commands.core;

import net.aufdemrand.denizen.exceptions.CommandExecutionException;
import net.aufdemrand.denizen.exceptions.InvalidArgumentsException;
import net.aufdemrand.denizen.scripts.ScriptEntry;
import net.aufdemrand.denizen.scripts.commands.AbstractCommand;
import net.aufdemrand.denizen.utilities.arguments.dEntity;
import net.aufdemrand.denizen.utilities.arguments.dLocation;
import net.aufdemrand.denizen.utilities.arguments.aH;
import net.aufdemrand.denizen.utilities.arguments.aH.ArgumentType;
import net.aufdemrand.denizen.utilities.debugging.dB;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent;

import java.util.ArrayList;
import java.util.List;

/**
 * This command will teleport an entity to a new location.
 * 	<br/>
 *  <pre>Usage: TELEPORT (NPC) [LOCATION:x,y,z,world] (TARGETS:[[NPCID|PlayerName](,)+])</pre>
 *  <br/>
 *  Examples:
 *  <br/><br/>
 *  1)  NPC wants to teleport the player to 100,100,100 in world:<br/>
 *  		<pre>- teleport location:100,100,100,world</pre>
 *  <br/>
 *  2)	NPC wants to teleport herself to 50,50,50 in world:<br/>
 *  		<pre>- teleport npc location:50,50,50,world</pre>
 *  <br/>
 *  3)  NPC wants to teleport NPC #456 and the player named "Dave" to 25,25,25
 *  		in world:<br/>
 *  		<pre>- teleport location:25,25,25,world targets:456,Dave</pre>
 *  <br/>
 */
public class TeleportCommand extends AbstractCommand {

    public	static	final	String	NPC_ARG = "NPC";

    /**
     * This method will parse the arguments needed to execute the Teleport
     * command from the given script entry.  It verifies that the format of the
     * command is accurate.  If not, it will throw an InvalidArgumentException
     * with the error message.
     *
     * @param	scriptEntry	The script entry processing this command.
     *
     * @throws	InvalidArgumentsException
     */
    @Override
    public void parseArgs(ScriptEntry scriptEntry)
            throws InvalidArgumentsException {

        //
        // List of entities to be teleported.
        //
        List<LivingEntity> teleportEntities = new ArrayList<LivingEntity> ();

        //
        // List of NPCs to be teleported.
        //
        List<NPC> teleportNPCs = new ArrayList<NPC> ();

        //
        // This is the location that the entity/entities are being teleported to.
        //
        dLocation teleportLocation = null;

        //
        // Process the arguments.
        //
        Boolean teleportPlayer = true;
        for (String arg : scriptEntry.getArguments()) {
            //
            // Is this script attempting to teleport the NPC?
            //
            if (arg.equalsIgnoreCase(TeleportCommand.NPC_ARG)) {
                teleportNPCs.add (scriptEntry.getNPC().getCitizen());
                teleportPlayer = false;
            }

            // If argument is a location?
            else if (aH.matchesLocation(arg))
                teleportLocation = aH.getLocationFrom(arg);

            else if (aH.matchesValueArg("TARGETS, TARGET", arg, ArgumentType.Custom)) {
                teleportPlayer = false;
                for (String target : aH.getListFrom(arg)) {
                    // Get entity
                    LivingEntity entity = dEntity.valueOf(target).getBukkitEntity();
                    if (entity != null) {
                        if (CitizensAPI.getNPCRegistry().getNPC(entity) != null) {
                            teleportNPCs.add(CitizensAPI.getNPCRegistry().getNPC(entity));
                            continue;
                        } else if (entity instanceof Player) {
                            teleportEntities.add(aH.getPlayerFrom(target));
                            continue;
                        }
                    }
                    dB.echoError("Invalid TARGET '%s'!", target);
                }
            }

            else throw new InvalidArgumentsException(dB.Messages.ERROR_UNKNOWN_ARGUMENT, arg);
        }

        //
        // If we're teleporting the player, add them as a recipient.
        //
        if (teleportPlayer == true) {
            teleportEntities.add (scriptEntry.getPlayer());
        }

        // Check some required arguments, make sure they are valid
        if (teleportLocation == null)
            throw new InvalidArgumentsException("Missing LOCATION argument. No teleport location.");
        if (teleportEntities.isEmpty() && teleportNPCs.isEmpty())
            throw new InvalidArgumentsException("Missing TARGETS argument. Nothing to teleport.");

        // Store objects in ScriptEntry for use in execute()
        scriptEntry.addObject("location", teleportLocation)
                .addObject("entities", teleportEntities)
                .addObject("npcs", teleportNPCs);
    }

    /**
     * Executes the Teleport command.
     *
     * @param	scriptEntry the ScriptEntry
     */
    @SuppressWarnings("unchecked")
    @Override
    public void execute(ScriptEntry scriptEntry) throws CommandExecutionException {

        dLocation teleportLocation = (dLocation) scriptEntry.getObject("location");
        List<LivingEntity> teleportEntities = (List<LivingEntity>) scriptEntry.getObject("entities");
        List<NPC> teleportNPCs = (List<NPC>) scriptEntry.getObject("npcs");

        // Debug output
        dB.echoApproval("<G>Executing '<Y>" + getName() + "<G>': "
                + teleportLocation.debug() + ", "
                + "Targets=<Y>'" + teleportEntities.toString() + "/" + teleportNPCs.toString() + "<G>'");

        for (LivingEntity entity : teleportEntities) {
            entity.teleport(teleportLocation);
        }

        for (NPC npc : teleportNPCs) {
            npc.spawn(teleportLocation);
            npc.getBukkitEntity().teleport(teleportLocation, PlayerTeleportEvent.TeleportCause.COMMAND);
        }
    }
}
