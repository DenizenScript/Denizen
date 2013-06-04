package net.aufdemrand.denizen.scripts.commands.core;

import net.aufdemrand.denizen.exceptions.CommandExecutionException;
import net.aufdemrand.denizen.exceptions.InvalidArgumentsException;
import net.aufdemrand.denizen.scripts.ScriptEntry;
import net.aufdemrand.denizen.scripts.commands.AbstractCommand;
import net.aufdemrand.denizen.utilities.arguments.dItem;
import net.aufdemrand.denizen.utilities.arguments.aH;
import net.aufdemrand.denizen.utilities.arguments.aH.ArgumentType;
import net.aufdemrand.denizen.utilities.debugging.dB;
import net.aufdemrand.denizen.utilities.debugging.dB.Messages;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.trait.trait.Equipment;

public class EquipCommand extends AbstractCommand{
	
	@Override
	public void parseArgs(ScriptEntry scriptEntry)
			throws InvalidArgumentsException {
		
		dItem hand = null;
		dItem head = null;
		dItem chest = null;
		dItem legs = null;
		dItem boots = null;
		
		for (String arg : scriptEntry.getArguments()) {
			
			if (aH.matchesValueArg("ITEMINHAND, HAND, HOLDING", arg, ArgumentType.String)) {
				
				arg = "ITEM:" + arg.split(":")[1];
				
				if (aH.matchesItem(arg)) {
					hand = aH.getItemFrom(arg);
				}
			}
			else if (aH.matchesValueArg("HEAD, HELMET", arg, ArgumentType.String)) {
				
				arg = "ITEM:" + arg.split(":")[1];
				
				if (aH.matchesItem(arg)) {
					head = aH.getItemFrom(arg);
				}
			}
			else if (aH.matchesValueArg("CHEST, CHESTPLATE", arg, ArgumentType.String)) {
				
				arg = "ITEM:" + arg.split(":")[1];
				
				if (aH.matchesItem(arg)) {
					chest = aH.getItemFrom(arg);
				}
			}
			else if (aH.matchesValueArg("LEGS, LEGGINGS", arg, ArgumentType.String)) {
				
				arg = "ITEM:" + arg.split(":")[1];
				
				if (aH.matchesItem(arg)) {
					legs = aH.getItemFrom(arg);
				}
			}
			else if (aH.matchesValueArg("BOOTS", arg, ArgumentType.String)) {
				
				arg = "ITEM:" + arg.split(":")[1];
				
				if (aH.matchesItem(arg)) {
					boots = aH.getItemFrom(arg);
				}
			}
		}
				
		scriptEntry.addObject("hand", hand)
				   .addObject("head", head)
				   .addObject("chest", chest)
				   .addObject("legs", legs)
				   .addObject("boots", boots);
	}

	@Override
	public void execute(ScriptEntry scriptEntry)
			throws CommandExecutionException {
		
		dItem hand = (dItem) scriptEntry.getObject("hand");
		dItem head = (dItem) scriptEntry.getObject("head");
		dItem chest = (dItem) scriptEntry.getObject("chest");
		dItem legs = (dItem) scriptEntry.getObject("legs");
		dItem boots = (dItem) scriptEntry.getObject("boots");
		NPC npc = scriptEntry.getNPC().getCitizen();
		
		if (!npc.hasTrait(Equipment.class)) npc.addTrait(Equipment.class);
		Equipment trait = npc.getTrait(Equipment.class);
		
		if (hand != null) {
			trait.set(0, hand.getItemStack());
		}
		if (head != null) {
			trait.set(1, head.getItemStack());
		}
		if (chest != null) {
			trait.set(2, chest.getItemStack());
		}
		if (legs != null) {
			trait.set(3, legs.getItemStack());
		}
		if (boots != null) {
			trait.set(4, boots.getItemStack());
		}
		
	}

}
