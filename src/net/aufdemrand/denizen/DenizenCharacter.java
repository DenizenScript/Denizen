package net.aufdemrand.denizen;

import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.event.NPCClickEvent;
import net.citizensnpcs.api.exception.NPCLoadException;
import net.citizensnpcs.api.npc.character.Character;
import net.citizensnpcs.api.npc.character.CharacterFactory;
import net.citizensnpcs.api.util.DataKey;
import net.citizensnpcs.api.npc.NPC;

import net.aufdemrand.denizen.Denizen;
import net.aufdemrand.denizen.ScriptEngine;
import net.aufdemrand.denizen.DenizenListener;

import org.bukkit.Bukkit;
import org.bukkit.entity.*;

public class DenizenCharacter extends Character {

	static Denizen plugin = (Denizen) Bukkit.getPluginManager().getPlugin("Denizen");
	
	@Override
	public void load(DataKey arg0) throws NPCLoadException {
		// TODO Auto-generated method stub
	}

	@Override
	public void save(DataKey arg0) {
		// TODO Auto-generated method stub
	}

    @Override
    public void onRightClick(NPC npc, Player player) {
  	
		if(npc.getCharacter() == CitizensAPI.getCharacterManager().getCharacter("denizen") && checkDenizenCooldown(player)) {
		
			Denizen.interactCooldown.put(player, System.currentTimeMillis() + 3000);
			
			DenizenListener.DenizenClicked(npc, player);
			
		}
    }
	

    @Override
    public void onLeftClick(NPC npc, Player player) {
  	
    	
    	
		if(npc.getCharacter() == CitizensAPI.getCharacterManager().getCharacter("denizen") && checkDenizenCooldown(player)) {
			
			Denizen.interactCooldown.put(player, System.currentTimeMillis() + 3000);
			
			DenizenListener.DenizenClicked(npc, player);
			
		}
    }


    public static boolean checkDenizenCooldown(Player thePlayer) {
    	
    	if (!Denizen.interactCooldown.containsKey(thePlayer)) return true;
    	if (System.currentTimeMillis() >= Denizen.interactCooldown.get(thePlayer)) return true;
    	
    	else return false;
    	
    }
    
}

