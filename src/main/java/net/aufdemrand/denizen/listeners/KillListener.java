package net.aufdemrand.denizen.listeners;

import java.util.List;

import net.aufdemrand.denizen.commands.core.ListenCommand;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDeathEvent;

public class KillListener extends AbstractListener {

	enum KillType {PLAYER, GROUP, ENTITY, NPC}

	KillType type;
	String target;
	Integer targetId;
	Integer quantity;
	String listenerId;

	Integer currentKills = 0;

	// new String[] { killType, killName, killNPCId, killListenerId, killQty }

	@Override
	public void build(String listenerId, Player thePlayer, String[] args, String scriptName) {

		this.listenerId = listenerId;
		this.thePlayer = thePlayer;
		this.scriptName = scriptName;

		try {
			this.type = KillType.valueOf(args[0]);
			this.target = args[1];
			// this.targetId = Integer.valueOf(args[2]);
			this.quantity = Integer.valueOf(args[3]);

			plugin.getServer().getPluginManager().registerEvents(this, plugin);

		} catch (Exception e) { 
			aH.echoError("Unable to build KILL listener for '%s'!", thePlayer.getName());
			e.printStackTrace();
			cancel();
		}

	}


	@EventHandler
	public void listen(EntityDeathEvent event) {
		if (type == KillType.ENTITY) {
			if (target.toUpperCase().equals(event.getEntityType().toString())
					&& event.getEntity().getKiller() == thePlayer)
			{ 
				currentKills++;
				aH.echoDebug(ChatColor.YELLOW + "// " + thePlayer.getName() + " killed a " + event.getEntityType().toString() + ".");
				complete(false);
			}
		}
	}


	@Override
	public void complete(boolean forceable) {

		if (quantity == currentKills || forceable) {
			EntityDeathEvent.getHandlerList().unregister(this);

			// Call script
			plugin.getCommandRegistry().getCommand(ListenCommand.class).finish(thePlayer, listenerId, scriptName, this);
		}
	}


	@Override
	public void cancel() {
		EntityDeathEvent.getHandlerList().unregister(this);
		plugin.getCommandRegistry().getCommand(ListenCommand.class).cancel(thePlayer, listenerId);
	}


	@Override
	public void save() {

		try {
			plugin.getSaves().set("Players." + thePlayer.getName() + ".Listeners.Saves." + listenerId + ".Listen Type", "KILL");
			plugin.getSaves().set("Players." + thePlayer.getName() + ".Listeners.Saves." + listenerId + ".Script", this.scriptName);
			plugin.getSaves().set("Players." + thePlayer.getName() + ".Listeners.Saves." + listenerId + ".Type", this.type.toString());
			plugin.getSaves().set("Players." + thePlayer.getName() + ".Listeners.Saves." + listenerId + ".Target", this.target);
			plugin.getSaves().set("Players." + thePlayer.getName() + ".Listeners.Saves." + listenerId + ".Quantity", this.quantity);
			plugin.getSaves().set("Players." + thePlayer.getName() + ".Listeners.Saves." + listenerId + ".Current Kills", this.currentKills);
			plugin.getSaves().set("Players." + thePlayer.getName() + ".Listeners.Saves." + listenerId + ".Target NPCId", this.targetId);
			
		} catch (Exception e) { 
			aH.echoError("Unable to save KILL listener for '%s'!", thePlayer.getName());
		}
	}


	@Override
	public void load(Player thePlayer, String listenerId) {

		try { 
			this.thePlayer = thePlayer;
			this.listenerId = listenerId;
			this.scriptName = plugin.getSaves().getString("Players." + thePlayer.getName() + ".Listeners.Saves." + listenerId + ".Script"); 

			this.type = KillType.valueOf(plugin.getSaves().getString("Players." + thePlayer.getName() + ".Listeners.Saves." + listenerId + ".Type"));
			this.target = plugin.getSaves().getString("Players." + thePlayer.getName() + ".Listeners.Saves." + listenerId + ".Target");
			this.quantity = plugin.getSaves().getInt("Players." + thePlayer.getName() + ".Listeners.Saves." + listenerId + ".Quantity");
			this.currentKills = plugin.getSaves().getInt("Players." + thePlayer.getName() + ".Listeners.Saves." + listenerId + ".Current Kills");
			this.targetId = plugin.getSaves().getInt("Players." + thePlayer.getName() + ".Listeners.Saves." + listenerId + ".Target NPCId");

			plugin.getServer().getPluginManager().registerEvents(this, plugin);

		} catch (Exception e) { 
			aH.echoError("Unable to load KILL listener for '%s'!", thePlayer.getName());
			cancel();
		}


	}


	@Override
	public void report() {


	}

}
