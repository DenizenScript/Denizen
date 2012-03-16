package net.aufdemrand.denizen;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.Random;

import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChatEvent;
import org.bukkit.event.player.PlayerPortalEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;

public class DenizenListener implements Listener {

	Denizen plugin;
	public DenizenListener(Denizen instance) { plugin = instance; }
	
	DenizenParser parser;
	public DenizenListener(DenizenParser instance) { parser = instance; }
		

	@EventHandler
	public void PlayerChatListener(PlayerChatEvent event) {

		List<net.citizensnpcs.api.npc.NPC> DenizenList = parser.GetDenizensWithinRange(event.getPlayer().getLocation(), event.getPlayer().getWorld(), Denizen.PlayerChatRangeInBlocks);
		if (DenizenList.isEmpty()) { return; }

		for (net.citizensnpcs.api.npc.NPC thisDenizen : DenizenList) {

			String theScript = parser.GetInteractScript(thisDenizen, event.getPlayer());
			if (theScript.isEmpty()) { 

				// Let's parse the script!
				
				parser.ParseScript(event.getMessage(), event.getPlayer(), theScript, "Chat");
				
			}
		}
	}

	
	
	
	

}