package net.aufdemrand.denizen;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import net.citizensnpcs.api.exception.NPCLoadException;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.npc.character.Character;
import net.citizensnpcs.api.trait.trait.Owner;
import net.citizensnpcs.api.util.DataKey;

public class DenizenCharacter extends Character {

	private Denizen plugin;


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
		
	    plugin = (Denizen) Bukkit.getServer().getPluginManager().getPlugin("Denizen");

	
		
		return;
	}

		
}

