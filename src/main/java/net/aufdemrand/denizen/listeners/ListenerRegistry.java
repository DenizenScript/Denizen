package net.aufdemrand.denizen.listeners;

import net.aufdemrand.denizen.Denizen;
import net.aufdemrand.denizen.events.ListenerCancelEvent;
import net.aufdemrand.denizen.events.ListenerFinishEvent;
import net.aufdemrand.denizen.interfaces.DenizenRegistry;
import net.aufdemrand.denizen.interfaces.RegistrationableInstance;
import net.aufdemrand.denizen.listeners.core.*;
import net.aufdemrand.denizen.npc.dNPC;
import net.aufdemrand.denizen.scripts.ScriptRegistry;
import net.aufdemrand.denizen.scripts.containers.core.TaskScriptContainer;
import net.aufdemrand.denizen.utilities.DenizenAPI;
import net.aufdemrand.denizen.utilities.debugging.dB;
import net.citizensnpcs.api.CitizensAPI;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class ListenerRegistry implements DenizenRegistry, Listener {

	private Map<String, Map<String, AbstractListener>> listeners = new ConcurrentHashMap<String, Map<String, AbstractListener>>();
	private Map<String, AbstractListenerType> types = new ConcurrentHashMap<String, AbstractListenerType>();

	private Denizen denizen;

	public ListenerRegistry(Denizen denizen) {
		this.denizen = denizen;
	}

	public void addListenerFor(Player player, AbstractListener instance, String listenerId) {
		Map<String, AbstractListener> playerListeners;
		if (listeners.containsKey(player.getName())) {
			playerListeners = listeners.get(player.getName());
		} else playerListeners = new HashMap<String, AbstractListener>();
		playerListeners.put(listenerId.toLowerCase(), instance);
		listeners.put(player.getName(), playerListeners);
	}

	public void cancel(Player player, String listenerId, AbstractListener instance) {
		removeListenerFor(player, listenerId);
        Bukkit.getPluginManager().callEvent(new ListenerCancelEvent(player, listenerId));
	}

	@Override
	public void disableCoreMembers() {
		// Note: This runs a onDisable() for each AbstractListenerType, NOT each
		// AbstractListener, which should be fine considering in-progress
		// AbstractListeners deconstruct automatically based on PlayerLogoutEvent
		// which is also run on a server disable or restart.
		for (RegistrationableInstance member : types.values())
			try {
				member.onDisable();
			} catch (Exception e) {
				dB.echoError("Unable to disable '" + member.getClass().getName() + "'!");
				if (dB.showStackTraces) e.printStackTrace();
			}
	}

	public void finish(Player player, dNPC npc, String listenerId, String finishScript, AbstractListener instance) {
		if (finishScript != null)
            try {
                // TODO: Add context to this
                ScriptRegistry.getScriptContainerAs(finishScript, TaskScriptContainer.class)
                        .runTaskScript(player, npc, null);
            } catch (Exception e) {
                // Hrm, not a valid task script?
            }

		// Remove listener instance from the player
		removeListenerFor(player, listenerId);
        Bukkit.getPluginManager().callEvent(new ListenerFinishEvent(player, listenerId));
	}

	@Override
	public <T extends RegistrationableInstance> T get(Class<T> clazz) {
		if (types.containsValue(clazz)) {
			for (RegistrationableInstance asl : types.values())
				if (asl.getClass() == clazz)
					return (T) clazz.cast(asl);
		}
		return null;
	}

	@Override
	public AbstractListenerType get(String listenerType) {
		if (types.containsKey(listenerType.toUpperCase())) return types.get(listenerType.toUpperCase());
		return null;
	}

	public AbstractListener getListenerFor(Player player, String listenerId) {
		if (listeners.containsKey(player.getName())) {
			Map<String, AbstractListener> playerListeners = listeners.get(player.getName());
			if (playerListeners.containsKey(listenerId.toLowerCase())) return playerListeners.get(listenerId.toLowerCase());
		}
		return null;
	}

	public Map<String, AbstractListener> getListenersFor(OfflinePlayer player) {
		if (listeners.containsKey(player.getName())) {
			Map<String, AbstractListener> playerListeners = listeners.get(player.getName());
			return playerListeners;
		}
		return null;
	}

	@Override
	public Map<String, AbstractListenerType> list() {
		return types;
	}

	@EventHandler
	public void playerJoin(PlayerJoinEvent event) {

		// Any saves quest listeners in progress?
		if (!denizen.getSaves().contains("Listeners." + event.getPlayer().getName())) return;
		Set<String> inProgress = denizen.getSaves().getConfigurationSection("Listeners." + event.getPlayer().getName()).getKeys(false);
		// If empty, no quest listeners to load.
		if (inProgress.isEmpty()) return;

		String path = "Listeners." + event.getPlayer().getName() + ".";

		// If not empty, let's do the loading process for each.
		for (String listenerId : inProgress) {
			// People tend to worry when they see long-ass stacktraces.. let's catch them.
			try {
				String type = denizen.getSaves().getString(path + listenerId + ".Listener Type");
                dNPC npc = DenizenAPI.getDenizenNPC(CitizensAPI.getNPCRegistry().getById(denizen.getSaves().getInt(path + listenerId + ".Linked NPCID")));
                if (get(type) == null) return;
				dB.log(event.getPlayer().getName() + " has a LISTENER in progress. Loading '" + listenerId + "'.");
				get(type).createInstance(event.getPlayer(), listenerId).load(event.getPlayer(), npc, listenerId, type);
			} catch (Exception e) {
				dB.log(event.getPlayer() + " has a saved listener named '" + listenerId + "' that may be corrupt. Skipping for now, but perhaps check the contents of your saves.yml for problems?");
			}
		}
	}

    public void deconstructPlayer(OfflinePlayer player ) {

        // Clear previous MemorySection in saves
        denizen.getSaves().set("Listeners." + player.getName(), null);

        // If no quest listeners in progress, nothing else to do.
        if (!listeners.containsKey(player.getName())) {
            return;
        }

        // If there are quest listeners, invoke save() for each of them.
        for (Map.Entry<String, AbstractListener> entry : getListenersFor((OfflinePlayer) player).entrySet()) {
            dB.log(player.getName() + " has a LISTENER in progress. Saving '" + entry.getKey() + "'.");
            entry.getValue().save();
        }

        // Remove all listeners from memory for Player
        listeners.remove(player);
    }

	@EventHandler
	public void playerQuit(PlayerQuitEvent event) {
        deconstructPlayer(event.getPlayer());
    }

	@Override
	public boolean register(String registrationName, RegistrationableInstance listenerType) {
		types.put(registrationName, (AbstractListenerType) listenerType);
		return false;
	}

	@Override
	public void registerCoreMembers() {
		new BlockListenerType().activate().as("BLOCK").withClass(BlockListenerInstance.class);
		new ItemListenerType().activate().as("ITEM").withClass(ItemListenerInstance.class);
		new KillListenerType().activate().as("KILL").withClass(KillListenerInstance.class);
		new ItemDropListenerType().activate().as("ITEMDROP").withClass(ItemDropListenerInstance.class);
		new TravelListenerType().activate().as("TRAVEL").withClass(TravelListenerInstance.class);
		denizen.getServer().getPluginManager().registerEvents(this, denizen);
	}

	public void removeListenerFor(Player player, String listenerId) {
		Map<String, AbstractListener> playerListeners;
		if (listeners.containsKey(player.getName())) {
			playerListeners = listeners.get(player.getName());
		} else return;
		playerListeners.remove(listenerId.toLowerCase());
		listeners.put(player.getName(), playerListeners);

	}

}
