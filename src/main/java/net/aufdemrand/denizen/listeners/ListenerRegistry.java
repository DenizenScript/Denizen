package net.aufdemrand.denizen.listeners;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import net.aufdemrand.denizen.Denizen;
import net.aufdemrand.denizen.interfaces.DenizenRegistry;
import net.aufdemrand.denizen.interfaces.RegistrationableInstance;
import net.aufdemrand.denizen.listeners.core.BlockListenerInstance;
import net.aufdemrand.denizen.listeners.core.BlockListenerType;
import net.aufdemrand.denizen.listeners.core.ItemListenerInstance;
import net.aufdemrand.denizen.listeners.core.ItemListenerType;
import net.aufdemrand.denizen.listeners.core.KillListenerInstance;
import net.aufdemrand.denizen.listeners.core.KillListenerType;
import net.aufdemrand.denizen.utilities.debugging.dB;

public class ListenerRegistry implements DenizenRegistry, Listener {

	private Map<String, Map<String, AbstractListener>> listeners = new ConcurrentHashMap<String, Map<String, AbstractListener>>();
	private Map<String, AbstractListenerType> types = new ConcurrentHashMap<String, AbstractListenerType>();

	private Denizen denizen;

	public ListenerRegistry(Denizen denizen) {
		this.denizen = denizen;
	}

	@Override
	public void registerCoreMembers() {
		new BlockListenerType().activate().as("BLOCK").withClass(BlockListenerInstance.class);
		new ItemListenerType().activate().as("ITEM").withClass(ItemListenerInstance.class);
		new KillListenerType().activate().as("KILL").withClass(KillListenerInstance.class);
		denizen.getServer().getPluginManager().registerEvents(this, denizen);
	}

	@Override
	public boolean register(String registrationName, RegistrationableInstance listenerType) {
		types.put(registrationName, (AbstractListenerType) listenerType);
		return false;
	}

	@Override
	public Map<String, AbstractListenerType> list() {
		return types;
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

	public Map<String, AbstractListener> getListenersFor(Player player) {
		if (listeners.containsKey(player.getName())) {
			Map<String, AbstractListener> playerListeners = listeners.get(player.getName());
			return playerListeners;
		}
		return null;
	}

	public void addListenerFor(Player player, AbstractListener instance, String listenerId) {
		Map<String, AbstractListener> playerListeners;
		if (listeners.containsKey(player.getName())) {
			playerListeners = listeners.get(player.getName());
		} else playerListeners = new HashMap<String, AbstractListener>();
		playerListeners.put(listenerId.toLowerCase(), instance);
		listeners.put(player.getName(), playerListeners);
	}

	public void removeListenerFor(Player player, String listenerId) {
		Map<String, AbstractListener> playerListeners;
		if (listeners.containsKey(player.getName())) {
			playerListeners = listeners.get(player.getName());
		} else return;
		playerListeners.remove(listenerId.toLowerCase());
		listeners.put(player.getName(), playerListeners);

	}

	@EventHandler
	public void playerQuit(PlayerQuitEvent event) {

		// Clear previous MemorySection in saves
		denizen.getSaves().set("Listeners." + event.getPlayer().getName(), null);

		// If no quest listeners in progress, nothing else to do.
		if (!listeners.containsKey(event.getPlayer().getName())) {
			return;
		}

		// If there are quest listeners, invoke save() for each of them.
		for (Entry<String, AbstractListener> entry : getListenersFor(event.getPlayer()).entrySet()) {
			dB.log(event.getPlayer().getName() + " has a LISTENER in progress. Saving '" + entry.getKey() + "'.");
			entry.getValue().save();
		}

		// Remove all listeners from memory for Player
		listeners.remove(event.getPlayer());
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
				if (get(type) == null) return;
				dB.log(event.getPlayer().getName() + " has a LISTENER in progress. Loading '" + listenerId + "'.");
				get(type).createInstance(event.getPlayer(), listenerId).load(event.getPlayer(), listenerId, type);
			} catch (Exception e) {
				dB.log(event.getPlayer() + " has a saved listener named '" + listenerId + "' that may be corrupt. Skipping for now, but perhaps check the contents of your saves.yml for problems?");
			}
		}
	}

	public void finish(Player player, String listenerId, String finishScript, AbstractListener instance) {
		if (finishScript != null) 
			denizen.getScriptEngine().getScriptBuilder().runTaskScript(player, finishScript);
		// Remove listener instance from the player
		removeListenerFor(player, listenerId);
	}

	public void cancel(Player player, String listenerId, AbstractListener instance) {
		removeListenerFor(player, listenerId);
	}





}
