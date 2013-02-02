package net.aufdemrand.denizen.listeners.core;

import net.aufdemrand.denizen.events.ReplaceableTagEvent;
import net.aufdemrand.denizen.listeners.AbstractListener;
import net.aufdemrand.denizen.listeners.core.KillListenerType.KillType;
import net.aufdemrand.denizen.utilities.Depends;
import net.aufdemrand.denizen.utilities.WorldGuardUtilities;
import net.aufdemrand.denizen.utilities.arguments.aH;
import net.aufdemrand.denizen.utilities.arguments.aH.ArgumentType;
import net.aufdemrand.denizen.utilities.debugging.dB;
import net.aufdemrand.denizen.utilities.debugging.dB.Messages;

import net.citizensnpcs.api.CitizensAPI;

import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class KillListenerInstance extends AbstractListener implements Listener {

	KillType type = null;
	List<String> targets = new ArrayList<String>();
	Integer quantity = 1;
	Integer currentKills = 0;
	String region = null;


	@Override
	public String report() {
		// Called by the '/denizen listener --report listenerId' command, meant to give information
		// to server-operators about the current status of this listener.
		return player.getName() + " current has quest listener '" + listenerId 
				+ "' active and must kill " + Arrays.toString(targets.toArray())
				+ " '" + type.name() + "'(s). Current progress '" + currentKills + "/" + quantity + "'.";
	}


	@Override
	public void onBuild(List<String> args) {
		// Build the listener from script arguments. onBuild() is called when a new listener is
		// made with the LISTEN command. All arguments except listenerType, listenerId, and script
		// are passed through to here.
		for (String arg : args) {

			if (aH.matchesValueArg("TYPE", arg, ArgumentType.Custom)) {
				// Note: Not to be confused with listenerType (which in this case is KILL). This is the
				// KillType, which is an argument unique to the KillListener.
				try { 
					this.type = KillType.valueOf(aH.getStringFrom(arg).toUpperCase()); 
					dB.echoDebug(Messages.DEBUG_SET_TYPE, this.type.name());
				} catch (Exception e) { }

			} else if (aH.matchesQuantity(arg)) {
				this.quantity = aH.getIntegerFrom(arg);
				dB.echoDebug(Messages.DEBUG_SET_QUANTITY, String.valueOf(quantity));

			} else if (aH.matchesValueArg("TARGETS, TARGET, NAME, NAMES", arg, ArgumentType.Custom)) {
				targets = aH.getListFrom(arg.toUpperCase());
				dB.echoDebug("...set TARGETS: " + Arrays.toString(targets.toArray()));

			} else if (aH.matchesValueArg("REGION", arg, ArgumentType.Custom)) {
				region = aH.getStringFrom(arg);
				dB.echoDebug("...set REGION.");
			}
		}

		// Need targets, need type
		if (targets.isEmpty()) {
			dB.echoError("Missing TARGETS argument!");
			cancel();
		}

		if (type == null) {
			dB.echoError("Missing TYPE argument! Valid: NPC, ENTITY, PLAYER, GROUP");
			cancel();
		}

		// At this point, constructed() is called.
	}


	@SuppressWarnings("unchecked")
	@Override
	public void onLoad() {
		// Build the listener from saved data. listenerId and listenerType are saved automatically.
		// onBuild() will not be called, this should handle everything onBuild() would with the
		// saved data from onSave().
		type = KillType.valueOf(((String) get("Type")));
		targets = (List<String>) get("Targets");
		quantity = (Integer) get("Quantity");
		currentKills = (Integer) get("Current Kills");
		region = (String) get("Region");

		// At this point, constructed() is called.
	}


	@Override
	public void onSave() {
		// If the player leaves the game while a listener is in progress, save the information
		// so that it can be rebuilt onLoad(). listenerId and listenerType are done automatically.
		store("Type", type.name());
		store("Targets", this.targets);
		store("Quantity", this.quantity);
		store("Current Kills", this.currentKills);
		store("Region", region);

		// At this point, deconstructed() is called.
	}


	@Override
	public void onFinish() {
		// Nothing to do here for now, but this is called when the quest listener is
		// finished, after the script is run, and right before deconstructed().

		// At this point, deconstructed() is called.
	}


	@Override
	public void onCancel() {
		// Nothing to do here for now, but this is called when the quest listener is
		// cancelled, right before deconstructed().

		// At this point, deconstructed() is called.
	}


	@Override
	public void constructed() {
		// Called after build and load methods. Perfect place to register
		// any bukkit events!
		denizen.getServer().getPluginManager().registerEvents(this, denizen);
	}


	@Override
	public void deconstructed() {
		// Called when the instance is deconstructed due to either it being
		// saved, finished, or cancelled.
		// This is the perfect place to unregister any bukkit events so it
		// can be cleanly removed from memory.
		EntityDeathEvent.getHandlerList().unregister(this);
	}


	@EventHandler
	public void listen(EntityDeathEvent event) {
		// Only continue if the event is an event for the player that owns this listener.
		if (event.getEntity().getKiller() != player) return;

		// If REGION argument specified, check. If not in region, don't count kill!
		if (region != null) 
			if (!WorldGuardUtilities.checkPlayerWGRegion(player, region)) return;

		// Check type!
		if (type == KillType.ENTITY) {
			if (targets.contains(event.getEntityType().toString()) || targets.contains(event.getEntityType().toString().toLowerCase()) || targets.contains("*")) { 
				currentKills++;
				dB.log(player.getName() + " killed a " + event.getEntityType().toString() + ". Current progress '" + currentKills + "/" + quantity + "'.");
				check();
			}

		} else if (type == KillType.NPC) {
			if (CitizensAPI.getNPCRegistry().isNPC(event.getEntity())) 
				if (targets.contains(CitizensAPI.getNPCRegistry().getNPC(event.getEntity()).getName().toUpperCase()) || targets.contains("*")
						|| targets.contains(String.valueOf(CitizensAPI.getNPCRegistry().getNPC(event.getEntity()).getId() ))) {
					currentKills++;
					dB.log(player.getName() + " killed " + String.valueOf(CitizensAPI.getNPCRegistry().getNPC(event.getEntity()).getId()) + "/" + CitizensAPI.getNPCRegistry().getNPC(event.getEntity()).getName() + ". Current progress '" + currentKills + "/" + quantity + "'.");
					check();
				}

		} else if (type == KillType.PLAYER) {
			if (event.getEntityType() == EntityType.PLAYER) 
				if (targets.contains(((Player) event.getEntity()).getName().toUpperCase()) || targets.contains("*")) {
					currentKills++;
					dB.log(player.getName() + " killed " + ((Player) event.getEntity()).getName().toUpperCase() + ". Current progress '" + currentKills + "/" + quantity + "'.");
					check();
				}

		} else if (type == KillType.GROUP) {
			if (event.getEntityType() == EntityType.PLAYER) 
				for (String group : Depends.permissions.getPlayerGroups((Player) event.getEntity())) 
					if (targets.contains(group.toUpperCase())) {
						currentKills++;
						dB.log(player.getName() + " killed " + ((Player) event.getEntity()).getName().toUpperCase() + " of group " + group + ".");
						check();
						break;
					}
		}

	}


	public void check() {
		// Check current kills vs. required kills; finish() if necessary.
		if (currentKills >= quantity) {
			finish();
		}
	}
	
	@EventHandler
    public void listenTag(ReplaceableTagEvent event) {
		
		if (!event.matches("LISTENER")) return;
		if (!event.getType().equalsIgnoreCase(listenerId)) return;
		
		if (event.getValue().equalsIgnoreCase("region")) {
			event.setReplaced(region);
		}
		
		else if (event.getValue().equalsIgnoreCase("quantity")) {
			event.setReplaced(quantity.toString());
		}
		
		else if (event.getValue().equalsIgnoreCase("currentkills")) {
			event.setReplaced(currentKills.toString());
		}
		
		else if (event.getValue().equalsIgnoreCase("targets")) {
			String targetList = "";
			for (String curTar : targets){
				targetList = targetList + curTar + ", ";
				targetList = targetList.substring(0, targetList.length() - 1);
			}
			event.setReplaced(targetList);
		}
	}
}
