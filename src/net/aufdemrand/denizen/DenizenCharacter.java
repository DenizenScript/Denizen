package net.aufdemrand.denizen;

import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.exception.NPCLoadException;
import net.citizensnpcs.api.npc.character.Character;
import net.citizensnpcs.api.util.DataKey;
import net.citizensnpcs.api.npc.NPC;


import org.bukkit.entity.*;

public class DenizenCharacter extends Character {

	
	
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
	 * Handles the event when clicking on a Denizen
	 * 
	 */
	
    @Override
    public void onRightClick(NPC npc, Player player) {
  		if(npc.getCharacter() == CitizensAPI.getCharacterManager().getCharacter("denizen") && Denizen.getDenizen.checkCooldown(player)) {
			Denizen.interactCooldown.put(player, System.currentTimeMillis() + 2000);
			Denizen.getListener.DenizenClicked(npc, player);
		}
    }
	
    

    @Override
    public void onLeftClick(NPC npc, Player player) {
  		if(npc.getCharacter() == CitizensAPI.getCharacterManager().getCharacter("denizen") && Denizen.getDenizen.checkCooldown(player)) {
			Denizen.interactCooldown.put(player, System.currentTimeMillis() + 2000);
			Denizen.getListener.DenizenClicked(npc, player);
		}
    }



}

