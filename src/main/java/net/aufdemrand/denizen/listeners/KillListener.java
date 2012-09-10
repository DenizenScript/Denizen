package net.aufdemrand.denizen.listeners;

import java.util.ArrayList;
import java.util.List;

import net.aufdemrand.denizen.Denizen;
import net.aufdemrand.events.ListenerCompleteEvent;
import net.aufdemrand.events.ScriptFinishEvent;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;

public class KillListener extends AbstractListener {

	enum KillType {PLAYER, GROUP, ENTITY}
	
	KillType killType;
	List<String> killTargets;
	Integer killQty;
	
	Integer currentKills = 0;
	
	public KillListener(Player thePlayer, String[] args, String scriptName) {
		super(thePlayer, args, scriptName);

		killTargets = new ArrayList<String>();
		killQty = 1;
		
		for (String thisArg : args) {
			
			if (thisArg.toUpperCase().contains("TYPE:")) {
				killType = KillType.valueOf(thisArg.toUpperCase());
			}
			
			else if (thisArg.toUpperCase().contains("TARGET:")) {
				killTargets.add(thisArg.toUpperCase());
			}
			
			else if (aH.matchesQuantity(thisArg)) {
				killQty = aH.getIntegerModifier(thisArg);
			}
		}
	}


	@EventHandler
	public void listen(EntityDeathEvent event) {
		if (killType == KillType.ENTITY) {
			if (killTargets.contains(event.getEntityType().toString()))
			{ 
				currentKills++;
				complete();
			}
		}
	}

	@Override
	public void complete() {

		if (killQty == currentKills) {
			EntityDeathEvent.getHandlerList().unregister(this);

			// Finish event
			ListenerCompleteEvent event = new ListenerCompleteEvent(thePlayer, this);
			Bukkit.getServer().getPluginManager().callEvent(event);
		}
	}

	@Override
	public void cancel() {
		EntityDeathEvent.getHandlerList().unregister(this);
	}

	@Override
	public void save() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void load(Player thePlayer) {
		// TODO Auto-generated method stub
		
	}





}
