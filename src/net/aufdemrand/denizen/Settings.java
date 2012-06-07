package net.aufdemrand.denizen;

import org.bukkit.Bukkit;

public class Settings {

	public boolean ChatGloballyIfNoChatTriggers() {
		Denizen plugin = (Denizen) Bukkit.getPluginManager().getPlugin("Denizen");		
		return plugin.getConfig().getBoolean("chat_globally_if_no_chat_triggers", false);
	}
	
	public int PlayerToNpcChatRangeInBlocks() {
		Denizen plugin = (Denizen) Bukkit.getPluginManager().getPlugin("Denizen");		
		return plugin.getConfig().getInt("player_to_npc_chat_range_in_blocks", 2);
	}
	
	
	
	player_to_npc_chat_range_in_blocks
	
	
	
	
	
	
	
	
	
}
