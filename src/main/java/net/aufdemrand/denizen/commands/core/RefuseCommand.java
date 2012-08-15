package net.aufdemrand.denizen.commands.core;

import java.util.logging.Level;

import net.aufdemrand.denizen.commands.AbstractCommand;
import net.aufdemrand.denizen.scripts.ScriptEntry;
import net.citizensnpcs.command.exception.CommandException;

/**
 * Removes permissions from a Player via Vault.
 * 
 * @author Jeremy Schroeder
 */

public class RefuseCommand extends AbstractCommand {

	/* REFUSE [permission.node]|['GROUP:group name'] (WORLD)|(WORLD:'NAME') */

	/* 
	 * Arguments: [] - Required, () - Optional 
	 * [permission.node] specifies the node to remove from Permissions.
	 * [GROUP:[group name] or specified group Name.
	 * Note: Set only one or the other.
	 *   
	 * Modifiers:
	 * (WORLD) Makes the permission only apply to the current world.
	 * (WORLD:'WORLD') Specifies the world.
	 *   
	 * Example Usage:
	 * REFUSE permissions.node
	 * REFUSE group:groupname
	 * REFUSE permissions.node WORLD
	 * 
	 */

	@Override
	public boolean execute(ScriptEntry theEntry) throws CommandException {

		/* Initialize variables */ 

		// Typically initialized as null and filled as needed. Remember: theEntry
		// contains some information passed through the execution process.
		String permissionsNode = null;
		String worldName = null;
		String groupName = null;

		if (theEntry.arguments() == null)
			throw new CommandException("...Usage: REFUSE [permission.node]|['GROUP:group name'] (WORLD)|(WORLD:'NAME')");
		
		/* Match arguments to expected variables */
		for (String thisArgument : theEntry.arguments()) {

			if (plugin.debugMode) plugin.getLogger().info("Processing command " + theEntry.getCommand() + " argument: " + thisArgument);

			/* Match to GROUP:[group] */
			if (aH.matchesGroup(thisArgument)) {
				groupName = aH.getStringModifier(thisArgument);
				aH.echoDebug("...group specified as '%s'.", thisArgument);
			}

			/* Match to WORLD:[world] */
			else if (aH.matchesWorld(thisArgument)) {
				worldName = aH.getStringModifier(thisArgument);
				aH.echoDebug("...world specified as '%s'.", thisArgument);
			}

			/* Takes current World */
			else if (thisArgument.toUpperCase().contains("WORLD")) {
				worldName = theEntry.getPlayer().getWorld().getName();
				aH.echoDebug("...world specified as current world.", thisArgument);
			}

			else {
				aH.echoDebug("...specified '%s' permissions node.", thisArgument);
				permissionsNode = thisArgument;
			}

		}	


		/* Arguments all matched up... */

		if (permissionsNode != null) {
			if (plugin.perms != null) {

				if (worldName == null) {
					String nullString = null;
					plugin.perms.playerRemove(nullString, theEntry.getPlayer().getName(), groupName);
				} else {
					plugin.perms.playerRemove(plugin.getServer().getWorld(worldName), theEntry.getPlayer().getName(), permissionsNode);
				}

			} else {
				if (plugin.debugMode) plugin.getLogger().log(Level.INFO, "...no permissions loaded! Have you installed Vault and a compatible plugin?");
				return false;
			}

			return true;
		} 

		/* No permissions node specified, maybe Group name? */

		else if (groupName != null) {

			if (plugin.perms != null) {
				if (worldName == null) {
					String nullString = null;
					plugin.perms.playerAddGroup(nullString, theEntry.getPlayer().getName(), groupName);
				} else {
					plugin.perms.playerRemoveGroup(plugin.getServer().getWorld(worldName), theEntry.getPlayer().getName(), groupName);
				}

			} else {
				if (plugin.debugMode) plugin.getLogger().log(Level.INFO, "...no permissions loaded! Have you installed Vault and a compatible plugin?");
				return false;
			}

			return true;
		}
	
		return false;
	}

}