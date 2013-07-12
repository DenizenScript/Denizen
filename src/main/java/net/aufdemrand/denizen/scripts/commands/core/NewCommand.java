package net.aufdemrand.denizen.scripts.commands.core;

import net.aufdemrand.denizen.exceptions.CommandExecutionException;
import net.aufdemrand.denizen.exceptions.InvalidArgumentsException;
import net.aufdemrand.denizen.scripts.ScriptEntry;
import net.aufdemrand.denizen.scripts.commands.AbstractCommand;
import net.aufdemrand.denizen.objects.dItem;
import net.aufdemrand.denizen.objects.aH;
import net.aufdemrand.denizen.objects.aH.ArgumentType;
import net.aufdemrand.denizen.utilities.debugging.dB;
import net.aufdemrand.denizen.utilities.debugging.dB.Messages;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Listener;

/**
 * Builds new objects for use within scripts.
 * 
 * @author Jeremy Schroeder
 * 
 */
public class NewCommand extends AbstractCommand implements Listener {

	@Override
	public void onEnable() {
		denizen.getServer().getPluginManager().registerEvents(this, denizen);
	}

	/**
	 * Used by the NewCommand for differentiating which object to create.
	 * 
	 */
	private enum ObjectType { LOCATION, ITEMSTACK, ENTITY, NPC }

	@SuppressWarnings("unused")
	@Override
	public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {

        for (aH.Argument arg : aH.interpret(scriptEntry.getArguments())) {

            if (!scriptEntry.hasObject("type")
                    && arg.matchesPrefix("location, loc, l")) {
                // new dLocation arg

            }

        }

		// Initialize required fields
		ObjectType objectType = null;
		String id = null;
		
		// Fields required for ITEMSTACK
		dItem item = null;
		int qty = 1;
		// Fields required for ENTITY
		LivingEntity entity = null;
		// Fields required for NPC
		//String npcName = null;
		//String npcType = null;

		for (String arg : scriptEntry.getArguments()) {

			if (aH.matchesArg("ITEMSTACK, ENTITY, NPC", arg)) {
				objectType = ObjectType.valueOf(arg.toUpperCase());
				dB.echoDebug("...set NEW object type: '%s'", arg.toUpperCase());

            }   else if (aH.matchesValueArg("ID", arg, ArgumentType.String)) {
				id = aH.getStringFrom(arg);
				dB.echoDebug("...set ID: '%s'", id);

                // Arguments for ObjectType.ITEMSTACK
			} else if (aH.matchesItem(arg)) {
				item = aH.getItemFrom(arg);
				dB.echoDebug("...set ITEM: '%s'", aH.getStringFrom(arg));

            } else if (aH.matchesQuantity(arg)) {
				qty = aH.getIntegerFrom(arg);
				dB.echoDebug(Messages.DEBUG_SET_QUANTITY, String.valueOf(qty));

                // Arguments for ObjectType.ENTITY
			//  } else if (aH.matchesEntity(arg)) {
				

			} else {

			}
		}

		if (objectType == null) 
			throw new InvalidArgumentsException("Must define an ObjectType. Valid: ITEMSTACK, ENTITY, NPC");
		if (id == null) 
			throw new InvalidArgumentsException("Must define an ID.");

		// Add objects that need to be passed to execute() to the scriptEntry
		scriptEntry.addObject("type", objectType);
		scriptEntry.addObject("id", id);
		
		if (objectType == ObjectType.ITEMSTACK) {
			if (item == null) 
				throw new InvalidArgumentsException("Must specify a valid ITEM.");
			// Set quantity on the ItemStack
			item.getItemStack().setAmount(qty);
			// Save objects to the scriptEntry that are required for ItemStack creation
			scriptEntry.addObject("itemstack", item);

        } else if (objectType == ObjectType.ENTITY) {
			if (entity == null) 
				throw new InvalidArgumentsException("Must specify a valid ENTITY.");
			// Save objects to the scriptEntry that are required for Entity creation
			scriptEntry.addObject("entity", entity);

        } else if (objectType == ObjectType.NPC) {
			//scriptEntry.addObject(npcType, object)

		}
	}

	@Override
	public void execute(ScriptEntry scriptEntry) throws CommandExecutionException {

		if ((ObjectType) scriptEntry.getObject("type") == ObjectType.ITEMSTACK) {
			String id = (String) scriptEntry.getObject("id");
			//itemStacks.put(id.toUpperCase(), item);
			dB.echoApproval("New ItemStack created and saved as 'ITEMSTACK." + id + "'");
		}
		
		
	}

}