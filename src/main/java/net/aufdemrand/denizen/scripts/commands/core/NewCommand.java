package net.aufdemrand.denizen.scripts.commands.core;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

import net.aufdemrand.denizen.events.ReplaceableTagEvent;
import net.aufdemrand.denizen.exceptions.CommandExecutionException;
import net.aufdemrand.denizen.exceptions.InvalidArgumentsException;
import net.aufdemrand.denizen.scripts.ScriptEntry;
import net.aufdemrand.denizen.scripts.commands.AbstractCommand;
import net.aufdemrand.denizen.scripts.helpers.ArgumentHelper.ArgumentType;
import net.aufdemrand.denizen.utilities.debugging.dB;
import net.aufdemrand.denizen.utilities.debugging.dB.Messages;

/**
 * Builds new objects for use with in scripts.
 * 
 * @author Jeremy Schroeder
 */

public class NewCommand extends AbstractCommand implements Listener {

	@Override
	public void onEnable() {
		denizen.getServer().getPluginManager().registerEvents(this, denizen);
	}

	public Map<String, ItemStack> itemStacks = new ConcurrentHashMap<String, ItemStack>();

	private enum ObjectType { ITEMSTACK, ENTITY, NPC }

	ObjectType objectType;
	String ID;
	long timeout = 0;
	ItemStack item;
	LivingEntity entity;

	@Override
	public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {

		item = null;
		entity = null;
		objectType = null;
		int qty = 1;

		for (String arg : scriptEntry.getArguments()) {

			if (aH.matchesArg("ITEMSTACK, ENTITY, NPC", arg)) {
				objectType = ObjectType.valueOf(arg.toUpperCase());
				dB.echoDebug("...set NEW object type: '%s'", arg.toUpperCase());
				continue;

			}   else if (aH.matchesValueArg("ID", arg, ArgumentType.String)) {
				ID = aH.getStringFrom(arg);
				dB.echoDebug("...set ID: '%s'", ID);
				continue;
				
				// Arguments for ObjectType.ITEMSTACK
			} else if (aH.matchesItem(arg)) {
				item = aH.getItemFrom(arg);
				dB.echoDebug("...set ITEM: '%s'", aH.getStringFrom(arg));
				continue;
				
			} else if (aH.matchesQuantity(arg)) {
				qty = aH.getIntegerFrom(arg);
				dB.echoDebug(Messages.DEBUG_SET_QUANTITY, String.valueOf(qty));
				continue;
				
				// Arguments for ObjectType.ENTITY
			} else if (aH.matchesEntity(arg)) {
				

			} else {

			}
		}

		if (objectType == ObjectType.ITEMSTACK)
			item.setAmount(qty);
	}

	@Override
	public void execute(String commandName) throws CommandExecutionException {

		if (objectType == ObjectType.ITEMSTACK) {
			itemStacks.put(ID.toUpperCase(), item);
			dB.echoApproval("New ItemStack created and saved as 'ITEMSTACK." + ID + "'");
		}
	}

	@EventHandler
	public void replaceableItemStacks(ReplaceableTagEvent e) {
		//
	}

}