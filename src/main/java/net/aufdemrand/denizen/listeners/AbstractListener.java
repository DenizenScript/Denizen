package net.aufdemrand.denizen.listeners;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import net.aufdemrand.denizen.Denizen;
import net.aufdemrand.denizen.scripts.helpers.ArgumentHelper;
import net.aufdemrand.denizen.utilities.debugging.Debugger;

public abstract class AbstractListener {

	protected Denizen denizen;
	protected ArgumentHelper aH;
	protected Debugger dB;

	protected String listenerType;
	protected String listenerId;
	protected Player player;
	protected String scriptName;
	protected Map<String, Object> saveable = new HashMap<String, Object>();


	public AbstractListener() {
		this.denizen = (Denizen) Bukkit.getServer().getPluginManager().getPlugin("Denizen");
		this.aH = denizen.getScriptEngine().getArgumentHelper();
		this.dB = denizen.getDebugger();
	}

	public String getListenerId() {
		return listenerId != null ? listenerId : "";
	}

	public String getListenerType() {
		return listenerType != null ? listenerType : "";
	}
	
	/**
	 * Method to handle building a new quest listener List<String> of arguments.
	 * Most likely called from a LISTEN dScript command. The player and listenerId fields
	 * are non-null at this point.
	 * 
	 * @param args  
	 * 			a list of dScript arguments
	 * 
	 */
	public abstract void onBuild(List<String> args);


	/**
	 * When a Player logs off, the quest listener's progress is stored to saves.yml.
	 * This method should use the store(stringKey, object) method to save the fields needed to
	 * successfully reload the current state of this quest listener when the onLoad()
	 * method is called. The fields for player, type, and listenerId are done automatically.
	 * 
	 */
	public abstract void onSave();


	/**
	 * Called when a Player logs on if an instance of this quest listener was saved
	 * with progress. Any variables that were saved with the store(stringKey, object) method 
	 * should be called and restored.
	 * 
	 */
	public abstract void onLoad();


	/**
	 * Stores a field away for retrieving later. Should be used in the onSave() method.
	 * 
	 */
	public void store(String key, Object object) {
		saveable.put(key.toLowerCase(), object);
	}

	/**
	 * Gets an Object that was store()d away.
	 * 
	 * @param key
	 * 		the name (key) of the Object requested
	 * @returns the Object associated with the key
	 * 
	 */
	public Object get(String key) {
		return denizen.getSaves().get("Listeners." + player.getName() + "." + listenerId + "." + key);
	}

	public void build(Player player, String listenerId, String listenerType, List<String> args, String finishScript) {
		this.player = player;
		this.listenerId = listenerId;
		this.listenerType = listenerType;
		this.scriptName = finishScript;
		onBuild(args);
	}

	public void save() {
		denizen.getSaves().set("Listeners." + player.getName() + "." + listenerId + ".Listener Type", listenerType);
		denizen.getSaves().set("Listeners." + player.getName() + "." + listenerId + ".Finish Script", scriptName);
		onSave();
		try {
			if (!saveable.isEmpty())
				for (Entry<String, Object> entry : saveable.entrySet()) 
					denizen.getSaves().set("Listeners." + player.getName() + "." + listenerId + "." + entry.getKey(), entry.getValue());
		} catch (Exception e) {
			dB.echoError("Problem saving listener '" + listenerId + "' for " + player.getName() + "!");
		}
	}

	public void load(Player player, String listenerId, String listenerType) {
		this.player = player;
		this.listenerId = listenerId;
		this.listenerType = listenerType;
		this.scriptName = (String) get("Finish Script");
		try { onLoad(); } catch (Exception e) {
			dB.echoError("Problem loading saved listener '" + listenerId + "' for " + player.getName() + "!");
		}
	}

	public abstract void onFinish();

	public void finish() {
		onFinish();
		denizen.getListenerRegistry().finish(player, listenerId, scriptName, this);
	}

	public abstract void onCancel();

	public void cancel() {
		onCancel();
		denizen.getListenerRegistry().cancel(player, listenerId, this);
	}

	public abstract String report();

}


