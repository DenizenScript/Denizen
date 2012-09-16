package net.aufdemrand.denizen.commands.core;

import net.aufdemrand.denizen.commands.AbstractCommand;
import net.aufdemrand.denizen.scripts.ScriptEntry;
import net.citizensnpcs.command.exception.CommandException;


/**
 * Gives permissions to a Player via Vault.
 * 
 * @author Jeremy Schroeder
 */

public class PermissCommand extends AbstractCommand {

	/* PERMISS [permission.node]|['GROUP:group name'] (WORLD)|(WORLD:'NAME') */

	/* 
	 * Arguments: [] - Required, () - Optional 
	 * [permission.node] specifies the node to add to Permissions.
	 * [GROUP:[group name] or specifies group Name.
	 * Note: Set only one or the other.
	 *   
	 * Modifiers:
	 * (WORLD) Makes the permission only apply to the current world.
	 * (WORLD:'WORLD') Specifies the world.
	 *   
	 * Example Usage:
	 * PERMISS permissions.node
	 * PERMISS group:groupname
	 * PERMISS permissions.node WORLD
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
			throw new CommandException("...Usage: PERMISS [permission.node]|['GROUP:group name'] (WORLD)|(WORLD:'NAME')");
		
		/* Match arguments to expected variables */
		for (String thisArg : theEntry.arguments()) {

			// Fill replaceables
			if (thisArg.contains("<")) thisArg = aH.fillReplaceables(theEntry.getPlayer(), theEntry.getDenizen(), thisArg, false);
			
			/* Match to GROUP:[group] */
			if (aH.matchesGroup(thisArg)) {
				groupName = aH.getStringModifier(thisArg);
				aH.echoDebug("...group specified as '%s'.", thisArg);
			}

			/* Match to WORLD:[world] */
			else if (aH.matchesWorld(thisArg)) {
				worldName = aH.getStringModifier(thisArg);
				aH.echoDebug("...world specified as '%s'.", thisArg);
			}

			/* Takes current World */
			else if (thisArg.toUpperCase().contains("WORLD")) {
				worldName = theEntry.getPlayer().getWorld().getName();
				aH.echoDebug("...world specified as current world.");
			}

			else {
				aH.echoDebug("...specified '%s' permissions node.", thisArg);
				permissionsNode = thisArg;
			}

		}	

		
		
		/* Arguments all matched up... */

		if (permissionsNode != null) {
			if (plugin.perms != null) {

				if (worldName == null) {
					String nullString = null;
					plugin.perms.playerAdd(nullString, theEntry.getPlayer().getName(), permissionsNode);
				} else {
					plugin.perms.playerAdd(plugin.getServer().getWorld(worldName), theEntry.getPlayer().getName(), permissionsNode);
				}

			} else {
				aH.echoError("No permissions loaded! Have you installed Vault and a compatible plugin?");
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
					plugin.perms.playerAddGroup(plugin.getServer().getWorld(worldName), theEntry.getPlayer().getName(), groupName);
				}

			} else {
				aH.echoError("No permissions loaded! Have you installed Vault and a compatible plugin?");
				return false;
			}

			return true;
		}

		return false;
	}

}