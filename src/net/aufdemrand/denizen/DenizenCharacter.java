package net.aufdemrand.denizen;

import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.exception.NPCLoadException;
import net.citizensnpcs.api.npc.character.Character;
import net.citizensnpcs.api.util.DataKey;
import net.citizensnpcs.api.npc.NPC;

import org.bukkit.entity.*;
import org.bukkit.event.Listener;

public class DenizenCharacter extends Character implements Listener  {

	
	
	/* 
	 * Citizens2 Load/Unload
	 * 
	 */

	@Override
	public void load(DataKey arg0) throws NPCLoadException {

		/* Nothing to do here, yet. */
		
	}

	@Override
	public void save(DataKey arg0) {

		/* Nothing to do here, yet. */

	}

	
	
	/*
	 * onRightClick/onLeftClick
	 * 
	 * Initiates the Click Trigger event when clicking on a Denizen.
	 * 
	 * Note: Soon turning left click into a Damage trigger, if Denizen is set to 'damageable'.
	 * If Denizen is not 'damageable', left click will mimic right click, that is, trigger the
	 * Click Trigger for the script.
	 * 
	 * Right click will remain Click Trigger.
	 * 
	 */

	@Override
	public void onRightClick(NPC npc, Player player) {

		if(npc.getCharacter() == CitizensAPI.getCharacterManager().getCharacter("denizen") 
				&& Denizen.checkCooldown(player)
				&& !Denizen.engagedNPC.contains(npc)) {
			Denizen.interactCooldown.put(player, System.currentTimeMillis() + 2000);
			Denizen.denizenInteracter.DenizenClicked(npc, player);
		}
	}



	@Override
	public void onLeftClick(NPC npc, Player player) {

		if(npc.getCharacter() == CitizensAPI.getCharacterManager().getCharacter("denizen") 
				&& Denizen.checkCooldown(player)
				&& !Denizen.engagedNPC.contains(npc)) {
			Denizen.interactCooldown.put(player, System.currentTimeMillis() + 2000);
			Denizen.denizenInteracter.DenizenClicked(npc, player);

		}
	}





}

