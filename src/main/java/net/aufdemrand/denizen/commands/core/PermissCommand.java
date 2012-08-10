package net.aufdemrand.denizen.commands.core;

import java.util.logging.Level;

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

		/* Match arguments to expected variables */
		if (theEntry.arguments() != null) {
			for (String thisArgument : theEntry.arguments()) {

				if (plugin.debugMode) plugin.getLogger().info("Processing command " + theEntry.getCommand() + " argument: " + thisArgument);

				/* Match to GROUP:[group] */
				if (thisArgument.matches("(?:GROUP|group)(:)([a-zA-Z0-9]+?)")) {
					if (plugin.debugMode) plugin.getLogger().log(Level.INFO, "...specified Group.'.");
					groupName = thisArgument.split(":")[1];
				}

				/* Match to WORLD:[world] */
				else if (thisArgument.matches("(?:WORLD|world)(:)([a-zA-Z0-9]+?)")) {
					if (plugin.debugMode) plugin.getLogger().log(Level.INFO, "...specified World.'.");
					worldName = thisArgument.split(":")[1];
				}

				/* Takes current World */
				else if (thisArgument.toUpperCase().contains("WORLD")) {
					if (plugin.debugMode) plugin.getLogger().log(Level.INFO, "...specified World.'.");
					worldName = theEntry.getPlayer().getWorld().getName();
				}

				else {
					if (plugin.debugMode) plugin.getLogger().log(Level.INFO, "...specified permissions node.'.");
					permissionsNode = thisArgument;
				}

			}	
		}

		/* Arguments all matched up... */
		
		if (permissionsNode != null) {
			if (plugin.perms != null) {

				if (worldName == null) {
					plugin.perms.playerAdd(theEntry.getPlayer(), permissionsNode);
				} else {
					plugin.perms.playerAdd(plugin.getServer().getWorld(worldName), theEntry.getPlayer().getName(), permissionsNode);
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
					plugin.perms.playerAddGroup(theEntry.getPlayer(), groupName);
				} else {
					plugin.perms.playerAddGroup(plugin.getServer().getWorld(worldName), theEntry.getPlayer().getName(), groupName);
				}

			} else {
				if (plugin.debugMode) plugin.getLogger().log(Level.INFO, "...no permissions loaded! Have you installed Vault and a compatible plugin?");
				return false;
			}
			
			return true;
		}

		/* Error processing */
		if (plugin.debugMode) if (theEntry.arguments() == null)
			throw new CommandException("...not enough arguments! Usage:PERMISS [permission.node]|['GROUP:group name'] (WORLD)|(WORLD:'NAME')");

		return false;
	}

}