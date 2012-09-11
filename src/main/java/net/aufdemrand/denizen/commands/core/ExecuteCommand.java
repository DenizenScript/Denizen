package net.aufdemrand.denizen.commands.core;

import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

import net.aufdemrand.denizen.commands.AbstractCommand;
import net.aufdemrand.denizen.npc.DenizenNPC;
import net.aufdemrand.denizen.scripts.ScriptEntry;
import net.citizensnpcs.command.exception.CommandException;

/**
 * Executes a bukkit command
 * 
 * @author Jeremy Schroeder
 */

public class ExecuteCommand extends AbstractCommand {

	/* EXECUTE [ASPLAYER|ASSERVER|ASNPC] '[command with arguments]' */

	/* 
	 * Arguments: [] - Required, () - Optional 
	 * [ASPLAYER|ASSERVER|ASNPC] defines how the command should be executed.
	 * [command with arguments] is the command to run, without a /. Can also
	 *   use <PLAYER> and <WORLD> variables.
	 *   
	 * Example Usage:
	 * EXECUTE ASSERVER 'gamemode <PLAYER> 2'
	 * EXECUTE ASNPC 'toggledownfall'
	 * 
	 */

	enum ExecuteType { ASSERVER, ASDENIZEN, ASPLAYER, ASOPPLAYER }

	@Override
	public boolean execute(ScriptEntry theEntry) throws CommandException {

		/* Initialize variables */ 

		String commandtoExecute = null;
		ExecuteType executeType = null;
		DenizenNPC theDenizen = null;

		if (theEntry.arguments() == null)
			throw new CommandException("...Usage: EXECUTE [AS_SERVER|AS_NPC|AS_PLAYER] '[Command to execute]'");

		/* Match arguments to expected variables */
		for (String thisArg : theEntry.arguments()) {

			/* If argument is ASPLAYER */
			if (thisArg.equalsIgnoreCase("ASPLAYER")
					|| thisArg.equalsIgnoreCase("AS_PLAYER")) {
				executeType = ExecuteType.ASPLAYER;
				aH.echoDebug("...executing '%s'.", thisArg);
			}
			
			if (thisArg.equalsIgnoreCase("ASOPPLAYER")
					|| thisArg.equalsIgnoreCase("AS_OP_PLAYER")) {
				executeType = ExecuteType.ASOPPLAYER;
				aH.echoDebug("...executing '%s'.", thisArg);
			}

			// If argument is a NPCID: modifier
			else if (aH.matchesNPCID(thisArg)) {
				theDenizen = aH.getNPCIDModifier(thisArg);
				if (theDenizen != null)
					aH.echoDebug("...now referencing '%s'.", thisArg);
			}
			
			/* If argument is ASNPC */
			else if (thisArg.equalsIgnoreCase("ASNPC")
					|| thisArg.equalsIgnoreCase("AS_NPC")) {
				executeType = ExecuteType.ASDENIZEN;
				aH.echoDebug("...executing '%s'.", thisArg);
			}

			/* If argument is ASSERVER */
			else if (thisArg.equalsIgnoreCase("ASSERVER")
					|| thisArg.equalsIgnoreCase("AS_SERVER")) {
				executeType = ExecuteType.ASSERVER;
				aH.echoDebug("...executing '%s'.", thisArg);
			}

			else {
				commandtoExecute = aH.fillFlags(theEntry.getPlayer(), thisArg)
						.replace("<PLAYER>", theEntry.getPlayer().getName())
						.replace("<WORLD>", theEntry.getPlayer().getWorld().getName());
			}
		}	

		if (theDenizen == null && theEntry.getDenizen() != null) theDenizen = theEntry.getDenizen();

		if (theEntry.getTexts()[0] != null) {
			commandtoExecute = commandtoExecute.replace("<*>", theEntry.getTexts()[0]);
		}
		
		if (theDenizen != null) {
			commandtoExecute = commandtoExecute.replace("<NPC>", theEntry.getDenizen().getName())
					.replace("<NPCID>", "" + theEntry.getDenizen().getCitizensEntity().getId());
			aH.echoDebug("...command: '%s'", commandtoExecute);
		} else aH.echoDebug("...command: '%s'", commandtoExecute);
		

		
		/* Execute the command, if all required variables are filled. */
		if (commandtoExecute != null && executeType != null) {

			switch (executeType) {

			case ASPLAYER:
				theEntry.getPlayer().performCommand(commandtoExecute);
				break;

			case ASOPPLAYER:
				boolean isOp = false;
				if (theEntry.getPlayer().isOp()) isOp = true;
				if (!isOp) theEntry.getPlayer().setOp(true);
				theEntry.getPlayer().performCommand(commandtoExecute);
				if (!isOp) theEntry.getPlayer().setOp(false);
				break;
			
			case ASDENIZEN:
				// Catch TASK-type script usage.
				if (theDenizen == null) {
					aH.echoError("Seems this was sent from a TASK-type script. Must use NPCID:# to specify a Denizen NPC when executing AS_DENIZEN!");
					return false;
				}

				if (theDenizen.getCitizensEntity().getBukkitEntity().getType() == EntityType.PLAYER) {
					((Player) theDenizen.getEntity()).setOp(true);
					((Player) theDenizen.getEntity()).performCommand(commandtoExecute);
					((Player) theDenizen.getEntity()).setOp(false);
				} else {
					aH.echoError("Cannot EXECUTE AS_NPC unless NPC is Human!");
				}
				break;

			case ASSERVER:
				plugin.getServer().dispatchCommand(plugin.getServer().getConsoleSender(), commandtoExecute);
			}

			return true;
		}

		return false;
	}

}