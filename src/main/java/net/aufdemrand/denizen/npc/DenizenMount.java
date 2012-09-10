package net.aufdemrand.denizen.npc;

import net.aufdemrand.denizen.Denizen;
import net.citizensnpcs.trait.Controllable;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

public class DenizenMount implements Listener {

	Player thePlayer;
	DenizenNPC theDenizen;
	Controllable theController;
	Denizen plugin;
	
	public DenizenMount(Denizen plugin, Player thePlayer, DenizenNPC theDenizen, Controllable theController) {
		this.thePlayer = thePlayer;
		this.theDenizen = theDenizen;
		this.theController = theController;
		this.plugin = plugin;
	}
	
	
	
	@EventHandler
	public void LocateDestination(PlayerInteractEvent event) {
		if (event.getPlayer() == thePlayer) {
			if (event.getAction() == Action.RIGHT_CLICK_BLOCK
					|| event.getAction() == Action.RIGHT_CLICK_AIR) {
				
				
				
			}
		}
	}
}
