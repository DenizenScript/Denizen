package net.aufdemrand.denizen.scripts.commands.core;

import net.aufdemrand.denizen.exceptions.CommandExecutionException;
import net.aufdemrand.denizen.exceptions.InvalidArgumentsException;
import net.aufdemrand.denizen.scripts.ScriptEntry;
import net.aufdemrand.denizen.scripts.commands.AbstractCommand;
import net.aufdemrand.denizen.utilities.arguments.dItem;
import net.aufdemrand.denizen.utilities.arguments.aH;
import net.aufdemrand.denizen.utilities.debugging.dB;
import net.aufdemrand.denizen.utilities.debugging.dB.Messages;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.trait.trait.Equipment;

public class EquipCommand extends AbstractCommand{
	public enum EquipType { HAND, BOOTS, LEGS, CHEST, HEAD }
	
	@Override
	public void parseArgs(ScriptEntry scriptEntry)
			throws InvalidArgumentsException {
		
		EquipType equipType = EquipType.HAND;
		dItem item = null;
		
		for (String arg : scriptEntry.getArguments()) {
			if (aH.matchesItem(arg)) {
				item = aH.getItemFrom(arg);
				dB.echoDebug(Messages.DEBUG_SET_ITEM, arg);
			} else if (aH.matchesArg("ITEMINHAND, HAND, HOLDING", arg)) {
				try {
					equipType = EquipType.valueOf(arg);
					dB.echoDebug("... equipping for " + equipType.name());
				} catch (Exception e) { throw new InvalidArgumentsException(Messages.ERROR_UNKNOWN_ARGUMENT, arg); }
			} 
		}
		
		if (item == null) {
			dB.echoError("...no item specified to equip!");
		}
		
		scriptEntry.addObject("item", item)
			.addObject("equipType", equipType);
	}

	@Override
	public void execute(ScriptEntry scriptEntry)
			throws CommandExecutionException {
		
		EquipType equipType = (EquipType) scriptEntry.getObject("equipType");
		dItem item = (dItem) scriptEntry.getObject("item");
		NPC npc = scriptEntry.getNPC().getCitizen();
		
		if (!npc.hasTrait(Equipment.class)) npc.addTrait(Equipment.class);
		Equipment trait = npc.getTrait(Equipment.class);
		
		switch (equipType) {
		case BOOTS:
			trait.set(4, item.getItemStack());
			break;
		case CHEST:
			trait.set(2, item.getItemStack());
			break;
		case HAND:
			trait.set(0, item.getItemStack());
			break;
		case HEAD:
			trait.set(1, item.getItemStack());
			break;
		case LEGS:
			trait.set(3, item.getItemStack());
			break;
		}
		
	}

}
