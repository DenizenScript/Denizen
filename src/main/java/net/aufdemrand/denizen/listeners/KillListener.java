package net.aufdemrand.denizen.listeners;

import java.util.List;

import net.aufdemrand.denizen.commands.core.ListenCommand;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDeathEvent;

public class KillListener extends AbstractListener {

	enum KillType {PLAYER, GROUP, ENTITY}
	
	KillType killType;
	List<String> killTargets;
	Integer killQty;
	String killId;
	
	Integer currentKills = 0;
	
	// new String[] { killType, killName, killNPCId, killListenerId, killQty }
	
	@Override
	public void build(Player thePlayer, String[] args, String scriptName) {
		
		try {
		
		this.thePlayer = thePlayer;
		this.args = args;
		this.scriptName = scriptName;
	
		
	
		plugin.getServer().getPluginManager().registerEvents(this, plugin);

		} catch (Exception e) { cancel(); }
		
	}


	@EventHandler
	public void listen(EntityDeathEvent event) {
		if (killType == KillType.ENTITY) {
			if (killTargets.contains(event.getEntityType().toString()))
			{ 
				currentKills++;
				complete(false);
			}
		}
	}

	@Override
	public void complete(boolean forceable) {

		if (killQty == currentKills || forceable) {
			EntityDeathEvent.getHandlerList().unregister(this);

			// Call script
			plugin.getCommandRegistry().getCommand(ListenCommand.class).finish(thePlayer, killId, scriptName, this);
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
