package net.aufdemrand.denizen.listeners;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
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
import net.aufdemrand.denizen.listeners.core.ItemListenerInstance;
import net.aufdemrand.denizen.listeners.core.ItemListenerType;
import net.aufdemrand.denizen.listeners.core.KillListenerInstance;
import net.aufdemrand.denizen.listeners.core.KillListenerType;
import net.aufdemrand.denizen.utilities.debugging.Debugger;

public class ListenerRegistry implements DenizenRegistry, Listener {

	private Map<String, List<AbstractListener>> listeners = new ConcurrentHashMap<String, List<AbstractListener>>();
	private Map<String, AbstractListenerType> types = new ConcurrentHashMap<String, AbstractListenerType>();

	private Denizen denizen;
	private Debugger dB;

	public ListenerRegistry(Denizen denizen) {
		this.denizen = denizen;
		dB = denizen.getDebugger();
	}

	@Override
	public void registerCoreMembers() {
		new KillListenerType().activate().as("KILL").withClass(KillListenerInstance.class);
		new ItemListenerType().activate().as("ITEM").withClass(ItemListenerInstance.class);
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

	public List<? extends AbstractListener> getListenersFor(Player player) {
		if (listeners.containsKey(player.getName())) {
			List<AbstractListener> returnable = new ArrayList<AbstractListener>();
			returnable.addAll(listeners.get(player.getName()));
			return returnable;
		}
		else return Collections.emptyList();
	}
	
	public void addInstanceOfListener(Player player, AbstractListener instance) {
		if (!listeners.containsKey(player.getName())) {
			listeners.put(player.getName(), new ArrayList<AbstractListener>());
		}
		List<AbstractListener> working = listeners.get(player.getName());
		working.add(instance);
		listeners.put(player.getName(), working);
		dB.log("Added. Size now: " + listeners.get(player.getName()).size() + " | " + listeners.get(player.getName()).get(0).listenerId);
	}
	
	@EventHandler
	public void playerQuit(PlayerQuitEvent event) {

		// Clear previous MemorySection in saves
		denizen.getSaves().set("Listeners." + event.getPlayer().getName(), null);
		
		dB.log(String.valueOf(listeners.size()) + " .. " + String.valueOf(listeners.containsKey(event.getPlayer())));
		
		if (!listeners.containsKey(event.getPlayer().getName())) {
			dB.log("TEST");
			return;
		}
		
		for (AbstractListener instance : getListenersFor(event.getPlayer())) {
			dB.log(event.getPlayer().getName() + " has a LISTENER in progress. Saving " + instance.listenerId + ".");
			instance.save();
		}
		
		listeners.remove(event.getPlayer());
	}
		

	@EventHandler
	public void playerJoin(PlayerJoinEvent event) {

		// Any saves quest listeners in progress?
		if (!denizen.getSaves().contains("Listeners." + event.getPlayer().getName())) return;
		Set<String> inProgress = denizen.getSaves().getConfigurationSection("Listeners." + event.getPlayer().getName()).getKeys(false);
		if (inProgress.isEmpty()) return;

		String path = "Listeners." + event.getPlayer().getName() + ".";
		
		for (String listenerId : inProgress) {
			String type = denizen.getSaves().getString(path + listenerId + ".Listener Type");
			if (get(type) == null) return;
			get(type).createInstance(event.getPlayer()).load(event.getPlayer(), listenerId, type);
		}
	}

	
	// Called when a listener is finished.
	public void finish(Player player, String listenerId, String finishScript, AbstractListener instance) {
		if (finishScript != null) 
			denizen.getScriptEngine().getScriptBuilder().runTaskScript(player, finishScript);
		List<AbstractListener> working = listeners.get(player.getName());
		working.remove(instance);
		listeners.put(player.getName(), working);

	}

	// Called when a listener is cancelled.
	public void cancel(Player player, String listenerId, AbstractListener instance) {
		List<AbstractListener> working = listeners.get(player.getName());
		working.remove(instance);
		listeners.put(player.getName(), working);
	}





}
