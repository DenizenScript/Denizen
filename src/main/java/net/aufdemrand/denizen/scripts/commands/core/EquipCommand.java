package net.aufdemrand.denizen.scripts.commands.core;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.entity.Player;

import net.aufdemrand.denizen.exceptions.CommandExecutionException;
import net.aufdemrand.denizen.exceptions.InvalidArgumentsException;
import net.aufdemrand.denizen.scripts.ScriptEntry;
import net.aufdemrand.denizen.scripts.commands.AbstractCommand;
import net.aufdemrand.denizen.utilities.arguments.dItem;
import net.aufdemrand.denizen.utilities.arguments.aH;
import net.aufdemrand.denizen.utilities.arguments.aH.ArgumentType;
import net.aufdemrand.denizen.utilities.debugging.dB;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.trait.trait.Equipment;

public class EquipCommand extends AbstractCommand{
	
    private enum TargetType { NPC, PLAYER }
	
	@Override
	public void parseArgs(ScriptEntry scriptEntry)
			throws InvalidArgumentsException {
		
	    TargetType targetType = TargetType.NPC;
		Map<String, dItem> equipment = new HashMap<String,dItem>();
		
		for (String arg : scriptEntry.getArguments()) {
			
			if (aH.matchesValueArg("ITEMINHAND, HAND, HOLDING", arg, ArgumentType.String)) {
				equipment.put("hand", getItem(arg));
			}
			else if (aH.matchesValueArg("HEAD, HELMET", arg, ArgumentType.String)) {
				equipment.put("head", getItem(arg));
			}
			else if (aH.matchesValueArg("CHEST, CHESTPLATE", arg, ArgumentType.String)) {
				equipment.put("chest", getItem(arg));
			}
			else if (aH.matchesValueArg("LEGS, LEGGINGS", arg, ArgumentType.String)) {
				equipment.put("legs", getItem(arg));
			}
			else if (aH.matchesValueArg("BOOTS, FEET", arg, ArgumentType.String)) {
				equipment.put("boots", getItem(arg));
			}
			else if (aH.matchesArg("PLAYER", arg)) {
        		targetType = TargetType.PLAYER;
                dB.echoDebug("... will be equipped by the player!");
			}
		}

		scriptEntry.addObject("equipment", equipment);
		scriptEntry.addObject("targetType", targetType);
	}

	@SuppressWarnings("unchecked")
	@Override
	public void execute(ScriptEntry scriptEntry)
			throws CommandExecutionException {
		
		Map<String, dItem> equipment = (Map<String, dItem>) scriptEntry.getObject("equipment");
		TargetType targetType = (TargetType) scriptEntry.getObject("targetType");
		
		if (targetType.toString().equals("NPC")) {
			
			NPC npc = scriptEntry.getNPC().getCitizen();
			
			if (npc != null) {
		
				if (!npc.hasTrait(Equipment.class)) npc.addTrait(Equipment.class);
		
				Equipment trait = npc.getTrait(Equipment.class);
		
				if (equipment.get("hand") != null) {
					trait.set(0, equipment.get("hand").getItemStack());
				}
				if (equipment.get("head") != null) {
					trait.set(1, equipment.get("head").getItemStack());
				}
				if (equipment.get("chest") != null) {
					trait.set(2, equipment.get("chest").getItemStack());
				}
				if (equipment.get("legs") != null) {
					trait.set(3, equipment.get("legs").getItemStack());
				}
				if (equipment.get("boots") != null) {
					trait.set(4, equipment.get("boots").getItemStack());
				}
			}
		}
		else {
			
			Player player = scriptEntry.getPlayer();
			
			if (player != null) {
		
				if (equipment.get("hand") != null) {
					player.getInventory().setItemInHand(equipment.get("hand").getItemStack());
				}
				if (equipment.get("head") != null) {
					player.getInventory().setHelmet(equipment.get("head").getItemStack());
				}
				if (equipment.get("chest") != null) {
					player.getInventory().setChestplate(equipment.get("chest").getItemStack());
				}
				if (equipment.get("legs") != null) {
					player.getInventory().setLeggings(equipment.get("legs").getItemStack());
				}
				if (equipment.get("boots") != null) {
					player.getInventory().setBoots(equipment.get("boots").getItemStack());
				}
			}
		}
		
	}
	
    public dItem getItem(String arg) {
    	
    	arg = "ITEM:" + aH.getStringFrom(arg);
		
		if (aH.matchesItem(arg)) {
			return (aH.getItemFrom(arg));
		}
		else {
			dB.echoApproval("Invalid item " + arg + "!");
		}
		
		return null;
    }

}
