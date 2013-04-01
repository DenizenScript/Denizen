package net.aufdemrand.denizen.scripts.commands.core;

import net.aufdemrand.denizen.exceptions.CommandExecutionException;
import net.aufdemrand.denizen.exceptions.InvalidArgumentsException;
import net.aufdemrand.denizen.scripts.ScriptEntry;
import net.aufdemrand.denizen.scripts.commands.AbstractCommand;
import net.aufdemrand.denizen.utilities.arguments.Item;
import net.aufdemrand.denizen.utilities.arguments.aH;
import net.aufdemrand.denizen.utilities.arguments.aH.ArgumentType;
import net.aufdemrand.denizen.utilities.debugging.dB;
import net.aufdemrand.denizen.utilities.debugging.dB.Messages;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Builds new objects for use with in scripts.
 * 
 * @author Jeremy Schroeder
 * 
 */
public class NewCommand extends AbstractCommand implements Listener {

	@Override
	public void onEnable() {
		denizen.getServer().getPluginManager().registerEvents(this, denizen);
	}

	private Map<String, Item> itemStacks = new ConcurrentHashMap<String, Item>();
	private Map<String, LivingEntity> entities = new ConcurrentHashMap<String, LivingEntity>();
	private Map<String, Integer> npcs = new ConcurrentHashMap<String, Integer>();
	
	/**
	 * Gets a currently saved ItemStack, created with Denizen's NEW command, given a
	 * case-insensitive 'id'. Note: These do not persist through a restart and may
	 * return null in such a case. This is meant for working with ItemStacks in the
	 * short-term, ie. creating an item (with NEW) and applying enchants, lore, etc.
	 * directly after.
	 * 
	 * @param id ID specified upon creation
	 * @return the saved ItemStack, or null if not found
	 * 
	 */
	public ItemStack getItem(String id) {
		if (itemStacks.containsKey(id.toUpperCase())) return itemStacks.get(id.toUpperCase()).getItemStack();
		else return null;
	}

	/**
	 * Gets a currently saved LivingEntity, created with Denizen's NEW command, given a
	 * case-insensitive 'id'. Note: Saved entities DO persist through a restart, but still
	 * may return null if removed by Bukkit, ie. the Entity dies. If the Bukkit LivingEntity
	 * returns null, it is removed from this registry as well.
	 * 
	 * @param id ID specified upon creation
	 * @return the saved LivingEntity, or null if not found
	 * 
	 */
	public LivingEntity getEntity(String id) {
		if (entities.containsKey(id.toUpperCase())) return entities.get(id.toUpperCase());
		else return null;
	}
	
	/**
	 * Gets a currently saved C2 NPC, created with Denizen's NEW command, given a case-
	 * insensitive 'id'. Note: Saved NPCs DO persist through a restart, but still may
	 * return null if removed by C2. Unlike 'entities', NPCs can die without being lost
	 * through this method. If the NPCID associated with the 'id' returns null, it is
	 * removed from this registry as well. Note: NPCs may also be valid, but unspawned.
	 * 
	 * @param id ID specified upon creation. This is different than the NPCID.
	 * @return the saved NPC, or null if not found
	 * 
	 */
	public NPC getNPC(String id) {
		if (npcs.containsKey(id.toUpperCase()) && CitizensAPI.getNPCRegistry().getById(npcs.get(id.toUpperCase())) != null)
			return CitizensAPI.getNPCRegistry().getById(npcs.get(id.toUpperCase()));
		return null;
	}
	
	/**
	 * Used by the NewCommand for differentiating which object to create.
	 * 
	 */
	private enum ObjectType { ITEMSTACK, ENTITY, NPC }

	@SuppressWarnings("unused")
	@Override
	public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {

		// Initialize required fields
		ObjectType objectType = null;
		String id = null;
		
		// Fields required for ITEMSTACK
		Item item = null;
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