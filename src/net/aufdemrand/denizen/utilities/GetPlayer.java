package net.aufdemrand.denizen.utilities;

import java.util.ArrayList;
import java.util.List;

import net.aufdemrand.denizen.Denizen;
import net.citizensnpcs.api.npc.NPC;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public class GetPlayer {

	Plugin plugin = (Denizen) Bukkit.getPluginManager().getPlugin("Denizen");	

	
	
	/*
	 * getPlayersInRange
	 * 
	 * Returns a List<Player> of players within a range to the specified Denizen.
	 * 
	 */

	public List<Player> getInRange (NPC theDenizen, int theRange) {

		List<Player> PlayersWithinRange = new ArrayList<Player>();
		Player[] DenizenPlayers = plugin.getServer().getOnlinePlayers();

		for (Player aPlayer : DenizenPlayers) {
			if (aPlayer.isOnline() 
					&& aPlayer.getWorld().equals(theDenizen.getBukkitEntity().getWorld()) 
					&& aPlayer.getLocation().distance(theDenizen.getBukkitEntity().getLocation()) < theRange)

				PlayersWithinRange.add(aPlayer);

		}

		return PlayersWithinRange;
	}


	
	/* talkToDenizen
	 *
	 * Sends the message from Player to Denizen with the formatting
	 * as specified in the config.yml talk_to_npc_string.
	 *
	 * <NPC> and <TEXT> are replaced with corresponding information.
	 */

	public void talkToDenizen(NPC theDenizen, Player thePlayer, String theMessage) {
		thePlayer.sendMessage(plugin.getConfig().getString("player_chat_to_npc", "You say to <NPC>, <TEXT>")
				.replace("<NPC>", theDenizen.getName())
				.replace("<TEXT>", theMessage)
				.replace("<PLAYER>", thePlayer.getName()));
		
	}
	
}
