package net.aufdemrand.denizen.utilities;

import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.exception.NPCLoadException;
import net.citizensnpcs.api.npc.character.Character;
import net.citizensnpcs.api.util.DataKey;
import net.citizensnpcs.api.npc.NPC;

import net.aufdemrand.denizen.Denizen;
import net.aufdemrand.denizen.utilities.GetListener;

import org.bukkit.Bukkit;
import org.bukkit.entity.*;

public class GetCharacter extends Character {

	Denizen plugin = (Denizen) Bukkit.getPluginManager().getPlugin("Denizen");
	GetListener denizenListener = new GetListener();
	GetDenizen getDenizen = new GetDenizen();
	
	
	@Override
	public void load(DataKey arg0) throws NPCLoadException {
	}

	@Override
	public void save(DataKey arg0) {
	}


	
	/*
	 * onRightClick/onLeftClick
	 * 
	 * Handles the event when clicking on a Denizen
	 * 
	 */
	
    @Override
    public void onRightClick(NPC npc, Player player) {
  		if(npc.getCharacter() == CitizensAPI.getCharacterManager().getCharacter("denizen") && getDenizen.checkCooldown(player)) {
			Denizen.interactCooldown.put(player, System.currentTimeMillis() + 2000);
			denizenListener.DenizenClicked(npc, player);
		}
		
    }
	

    @Override
    public void onLeftClick(NPC npc, Player player) {
  		if(npc.getCharacter() == CitizensAPI.getCharacterManager().getCharacter("denizen") && getDenizen.checkCooldown(player)) {
			Denizen.interactCooldown.put(player, System.currentTimeMillis() + 2000);
			denizenListener.DenizenClicked(npc, player);
		}
    
    }



    
}

