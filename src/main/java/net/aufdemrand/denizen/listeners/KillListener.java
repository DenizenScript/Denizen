package net.aufdemrand.denizen.listeners;

import java.util.Arrays;
import java.util.List;

import net.aufdemrand.denizen.commands.core.ListenCommand;
import net.citizensnpcs.api.CitizensAPI;

import org.bukkit.ChatColor;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDeathEvent;

public class KillListener extends AbstractListener {

	enum KillType {PLAYER, GROUP, ENTITY, NPC}

	KillType type;
	List<String> targets;
	List<String> targetIds;
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
			this.targets = Arrays.asList(args[1].toUpperCase().split(","));
			this.targetIds = Arrays.asList(args[2].split(","));
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

		if (event.getEntity().getKiller() == thePlayer) {
			if (type == KillType.ENTITY) {
				if (targets.contains(event.getEntityType().toString()))
				{ 
					currentKills++;
					aH.echoDebug(ChatColor.YELLOW + "// " + thePlayer.getName() + " killed a " + event.getEntityType().toString() + ".");
					complete(false);
				}
			}

			else if (type == KillType.NPC) {
				if (CitizensAPI.getNPCRegistry().isNPC(event.getEntity())) {
					if (targets.contains(CitizensAPI.getNPCRegistry().getNPC(event.getEntity()).getName().toUpperCase())
							|| targetIds.contains(String.valueOf(CitizensAPI.getNPCRegistry().getNPC(event.getEntity()).getId() ))) {
						currentKills++;
						aH.echoDebug(ChatColor.YELLOW + "// " + thePlayer.getName() + " killed " + String.valueOf(CitizensAPI.getNPCRegistry().getNPC(event.getEntity()).getId()) + "/" + CitizensAPI.getNPCRegistry().getNPC(event.getEntity()).getName() + ".");
						complete(false);
					}
				}
			}

			else if (type == KillType.PLAYER) {
				if (event.getEntityType() == EntityType.PLAYER) {
					if (targets.contains(((Player) event.getEntity()).getName().toUpperCase())) {
						currentKills++;
						aH.echoDebug(ChatColor.YELLOW + "// " + thePlayer.getName() + " killed " + ((Player) event.getEntity()).getName().toUpperCase() + ".");
						complete(false);
					}
				}
			}

			else if (type == KillType.GROUP) {
				if (event.getEntityType() == EntityType.PLAYER) {
					for (String group : plugin.perms.getPlayerGroups((Player) event.getEntity())) {
						if (targets.contains(group.toUpperCase())) {
							currentKills++;
							aH.echoDebug(ChatColor.YELLOW + "// " + thePlayer.getName() + " killed " + ((Player) event.getEntity()).getName().toUpperCase() + " of group " + group + ".");
							complete(false);
							break;
						}
					}
				}
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
			plugin.getSaves().set("Players." + thePlayer.getName() + ".Listeners.Saves." + listenerId + ".Targets", this.targets);
			plugin.getSaves().set("Players." + thePlayer.getName() + ".Listeners.Saves." + listenerId + ".Quantity", this.quantity);
			plugin.getSaves().set("Players." + thePlayer.getName() + ".Listeners.Saves." + listenerId + ".Current Kills", this.currentKills);
			plugin.getSaves().set("Players." + thePlayer.getName() + ".Listeners.Saves." + listenerId + ".Target NPCIds", this.targetIds);

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
			this.targets = plugin.getSaves().getStringList("Players." + thePlayer.getName() + ".Listeners.Saves." + listenerId + ".Target");
			this.quantity = plugin.getSaves().getInt("Players." + thePlayer.getName() + ".Listeners.Saves." + listenerId + ".Quantity");
			this.currentKills = plugin.getSaves().getInt("Players." + thePlayer.getName() + ".Listeners.Saves." + listenerId + ".Current Kills");
			this.targetIds = plugin.getSaves().getStringList("Players." + thePlayer.getName() + ".Listeners.Saves." + listenerId + ".Target NPCId");

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
