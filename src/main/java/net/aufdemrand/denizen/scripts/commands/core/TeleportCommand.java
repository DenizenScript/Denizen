package net.aufdemrand.denizen.scripts.commands.core;

import net.aufdemrand.denizen.exceptions.CommandExecutionException;
import net.aufdemrand.denizen.exceptions.InvalidArgumentsException;
import net.aufdemrand.denizen.scripts.ScriptEntry;
import net.aufdemrand.denizen.scripts.commands.AbstractCommand;
import net.aufdemrand.denizen.utilities.arguments.aH;
import net.aufdemrand.denizen.utilities.arguments.aH.ArgumentType;
import net.aufdemrand.denizen.utilities.debugging.dB;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
import org.bukkit.Bukkit;
import org.bukkit.Location;
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
     * This method is called when the command is enabled.
     */
    @Override
    public void onEnable() {
    }

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
        Location teleportLocation = null;

        //
        // Sanity check to make sure the number of arguments being passed in is
        // reasonable.
        //
        if (scriptEntry.getArguments ().size () > 3) {
            throw new InvalidArgumentsException("Too many arguments.");
        }

        //
        // Process the arguments.
        //
        Boolean teleportPlayer = true;
        for (String thisArg : scriptEntry.getArguments()) {
            //
            // Is this script attempting to teleport the NPC?
            //
            if (thisArg.equalsIgnoreCase (TeleportCommand.NPC_ARG)) {
                teleportNPCs.add ((scriptEntry.getNPC().getCitizen()));
                teleportPlayer = false;
                dB.echoDebug("...Teleporting the NPC instead of the PLAYER", thisArg);
            }
            // If argument is a location?
            else if (aH.matchesLocation(thisArg)) {
                teleportLocation = aH.getLocationFrom(thisArg);
                dB.echoDebug("...Teleport location now at '%s'.", thisArg);
            } else if (aH.matchesValueArg("TARGETS", thisArg, ArgumentType.Custom)) {
                teleportPlayer = false;
                for (String target : aH.getListFrom(thisArg)) {
                    if (target.matches("\\d+")) {
                        NPC npc = CitizensAPI.getNPCRegistry().getById(Integer.valueOf(target));
                        if ( npc != null) {
                            teleportNPCs.add (npc);
                            continue;
                        } else {
                            dB.echoError("Unable to find NPC: %s", target);
                        }
                    } else {
                        Player player = Bukkit.getPlayer(target);
                        if (player != null) {
                            teleportEntities.add(player);
                            continue;
                        } else {
                            dB.echoError("Unable to find player: %s", target);
                        }
                    }

                    dB.echoError("Invalid TARGET '%s'!", target);
                }
            }
            else {
                dB.echoError("...unable to match '%s'!", thisArg);
            }
        }

        //
        // If we're teleporting the player, add them as a recipient.
        //
        if (teleportPlayer == true) {
            teleportEntities.add (scriptEntry.getPlayer());
        }

        scriptEntry.addObject("location", teleportLocation);
        scriptEntry.addObject("entities", teleportEntities);
        scriptEntry.addObject("npcs", teleportNPCs);
    }

    /**
     * Executes the Teleport command.
     *
     * @param	scriptEntry the ScriptEntry
     */
    @Override
    public void execute(ScriptEntry scriptEntry) throws CommandExecutionException {

        Location teleportLocation = (Location) scriptEntry.getObject("location");
        List<LivingEntity> teleportEntities = (List<LivingEntity>) scriptEntry.getObject("entities");
        List<NPC> teleportNPCs = (List<NPC>) scriptEntry.getObject("npcs");


        if (teleportLocation != null) {
            for (LivingEntity entity : teleportEntities) {
                entity.teleport(teleportLocation);
            }

            for (NPC npc : teleportNPCs) {
                npc.getBukkitEntity().teleport(teleportLocation, PlayerTeleportEvent.TeleportCause.COMMAND);
            }
        }
    }
}