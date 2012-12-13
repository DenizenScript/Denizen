package net.aufdemrand.denizen.listeners.core;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;

import net.aufdemrand.denizen.listeners.AbstractListener;
import net.aufdemrand.denizen.listeners.core.KillListenerType.KillType;
import net.aufdemrand.denizen.scripts.helpers.ArgumentHelper.ArgumentType;
import net.aufdemrand.denizen.utilities.debugging.Debugger.Messages;
import net.citizensnpcs.api.CitizensAPI;

public class KillListenerInstance extends AbstractListener implements Listener {

	KillType type;
	List<String> targets = new ArrayList<String>();
	int quantity = 1;
	int currentKills = 0;

	@Override
	public void onBuild(List<String> args) {

		for (String arg : args) {
			if (aH.matchesValueArg("TYPE", arg, ArgumentType.Custom)) {
				try { 
					this.type = KillType.valueOf(arg); 
					dB.echoDebug(Messages.DEBUG_SET_TYPE, this.type.name());
				} catch (Exception e) { }

			} else if (aH.matchesQuantity(arg)) {
				this.quantity = aH.getIntegerFrom(arg);
				dB.echoDebug(Messages.DEBUG_SET_QUANTITY, String.valueOf(quantity));

			} else if (aH.matchesValueArg("TARGETS", arg, ArgumentType.Custom)) {
				targets = aH.getListFrom(arg);
				dB.echoDebug("...set TARGETS.");
			}
		}

		if (targets.isEmpty()) {
			dB.echoError("Missing TARGETS argument!");
			cancel();
		}
		
		if (type == null) {
			dB.echoError("Missing TYPE argument! Valid: NPC, ENTITY, PLAYER, GROUP");
			cancel();
		}
		
		denizen.getServer().getPluginManager().registerEvents((Listener) this, denizen);
	}

	@Override
	public void onSave() {
		store("Type", type.name());
		store("Targets", this.targets);
		store("Quantity", this.quantity);
		store("Current Kills", this.currentKills);
	}

	@Override
	public void onLoad() {
		type = KillType.valueOf(((String) get("Type")));
		targets = (List<String>) get("Targets");
		quantity = (Integer) get("Quantity");
		currentKills = (Integer) get("Current Kills");
	}

	public void check() {
		if (currentKills >= quantity) {
			finish();
		}
	}
	
	@EventHandler
	public void listen(EntityDeathEvent event) {
		if (event.getEntity().getKiller() != player) return;
		if (type == KillType.ENTITY) {
			if (targets.contains(event.getEntityType().toString()) || targets.contains("*"))
			{ 
				currentKills++;
				dB.log(player.getName() + " killed a " + event.getEntityType().toString() + ".");
				check();
			}
			
		} else if (type == KillType.NPC) {
			if (CitizensAPI.getNPCRegistry().isNPC(event.getEntity())) {
				if (targets.contains(CitizensAPI.getNPCRegistry().getNPC(event.getEntity()).getName().toUpperCase()) || targets.contains("*")
						|| targets.contains(String.valueOf(CitizensAPI.getNPCRegistry().getNPC(event.getEntity()).getId() ))) {
					currentKills++;
					dB.log(player.getName() + " killed " + String.valueOf(CitizensAPI.getNPCRegistry().getNPC(event.getEntity()).getId()) + "/" + CitizensAPI.getNPCRegistry().getNPC(event.getEntity()).getName() + ".");
					check();
				}
			}
		
		} else if (type == KillType.PLAYER) {
			if (event.getEntityType() == EntityType.PLAYER) {
				if (targets.contains(((Player) event.getEntity()).getName().toUpperCase()) || targets.contains("*")) {
					currentKills++;
					dB.log(player.getName() + " killed " + ((Player) event.getEntity()).getName().toUpperCase() + ".");
					check();
				}
			}
		}
		
		// Will put this back in ASAP.
		
		//	else if (type == KillType.GROUP) {
		//		if (event.getEntityType() == EntityType.PLAYER) {
		//			for (String group : plugin.perms.getPlayerGroups((Player) event.getEntity())) {
		//				if (targets.contains(group.toUpperCase())) {
		//					currentKills++;
		//					aH.echoDebug(ChatColor.YELLOW + "// " + thePlayer.getName() + " killed " + ((Player) event.getEntity()).getName().toUpperCase() + " of group " + group + ".");
		//					complete(false);
		//					break;
		//				}
		//			}
		//		}
		//	}

	}

	@Override
	public void onCancel() {
		EntityDeathEvent.getHandlerList().unregister(this);
	}

	@Override
	public String report() {
		return player.getName() + " current has quest listener '" + listenerId 
				+ "' active and must kill " + Arrays.toString(targets.toArray())
				+ " '" + type.name() + "'(s). Current progress '" + currentKills + "/" + quantity + "'.";
	}

	@Override
	public void onFinish() {
		// Nothing to do here, the finishScript will automatically execute at this point.
	}


}
