package net.aufdemrand.denizen.listeners;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import net.aufdemrand.denizen.objects.dNPC;
import net.aufdemrand.denizen.objects.dPlayer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import net.aufdemrand.denizen.Denizen;
import net.aufdemrand.denizen.utilities.debugging.dB;

public abstract class AbstractListener {

	protected Denizen denizen;

	protected String listenerType;
	public String listenerId;
	protected dPlayer player;
	protected String scriptName;
    protected dNPC npc;
	protected Map<String, Object> saveable = new HashMap<String, Object>();

	public AbstractListener() {
		this.denizen = (Denizen) Bukkit.getServer().getPluginManager().getPlugin("Denizen");
	}

	public void build(dPlayer player, String listenerId, String listenerType, List<String> args, String finishScript, dNPC npc) {
		this.player = player;
		this.listenerId = listenerId;
		this.listenerType = listenerType;
		this.scriptName = finishScript;
        this.npc = npc;
		onBuild(args);
		save();
		constructed();
	}

	public void cancel() {
		onCancel();
		denizen.getListenerRegistry().cancel(player,listenerId, this);
		deconstructed();
	}

	public abstract void constructed();

	public abstract void deconstructed();

	public void finish() {
		onFinish();
		denizen.getListenerRegistry().finish(player, npc, listenerId, scriptName, this);
		deconstructed();
	}

	/**
	 * Gets an Object that was store()d away.
	 *
	 * @param key
	 * 		the name (key) of the Object requested
	 * @return the Object associated with the key
	 *
	 */
	public Object get(String key) {
		return denizen.getSaves().get("Listeners." + player.getName() + "." + listenerId + "." + key);
	}

	public String getListenerId() {
		return listenerId != null ? listenerId : "";
	}

	public String getListenerType() {
		return listenerType != null ? listenerType : "";
	}

	public void load(dPlayer player, dNPC npc, String listenerId, String listenerType) {
		this.player = player;
		this.listenerId = listenerId;
		this.listenerType = listenerType;
		this.scriptName = (String) get("Finish Script");
        this.npc = npc;
		try { onLoad(); } catch (Exception e) {
			dB.echoError("Problem loading saved listener '" + listenerId + "' for " + player.getName() + "!");
		}
		constructed();
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

	public abstract void onCancel();

	public abstract void onFinish();

	/**
	 * Called when a Player logs on if an instance of this quest listener was saved
	 * with progress. Any variables that were saved with the store(stringKey, object) method
	 * should be called and restored.
	 *
	 */
	public abstract void onLoad();

	/**
	 * When a Player logs off, the quest listener's progress is stored to saves.yml.
	 * This method should use the store(stringKey, object) method to save the fields needed to
	 * successfully reload the current state of this quest listener when the onLoad()
	 * method is called. The fields for player, type, and listenerId are done automatically.
	 *
	 */
	public abstract void onSave();

	/**
	 * Sums up the current status of a this AbstractListenerInstance in a way that would be
	 * useful by itself to a Player or Console administrator.
	 *
	 * Called by the '/denizen listener --report listenerId' bukkit command.
	 *
	 * This should include all applicable variables when reporting. Suggested format would
	 * follow suit with core Listeners. For example:
	 *
	 * return player.getName() + " currently has quest listener '" + listenerId
	 *		+ "' active and must kill " + Arrays.toString(targets.toArray())
	 *		+ " '" + type.name() + "'(s). Current progress '" + currentKills
	 *		+ "/" + quantity + "'.";
	 *
	 * Output:
	 * aufdemrand currently has quest listener 'example_quest' active and must kill
	 *  '[ZOMBIE, SKELETON] ENTITY'(s). Current progress '10/15'.
	 *
	 * Note: This is not intended to be a 'Quest Log' for a Player, rather is used when
	 * administrators/server operators are checking up on this Listener. Ideally,
	 * that kind of information should be handled with the use of replaceable tags.
	 *
	 * @return a 'formatted' String that contains current progress
	 *
	 */
	public abstract String report();

	public void save() {
		denizen.getSaves().set("Listeners." + player.getName() + "." + listenerId + ".Listener Type", listenerType);
		denizen.getSaves().set("Listeners." + player.getName() + "." + listenerId + ".Finish Script", scriptName);
        denizen.getSaves().set("Listeners." + player.getName() + "." + listenerId + ".Linked NPCID", npc.getId());
		onSave();
		try {
			if (!saveable.isEmpty())
				for (Entry<String, Object> entry : saveable.entrySet()) 
					denizen.getSaves().set("Listeners." + player.getName() + "." + listenerId + "." + entry.getKey(), entry.getValue());
		} catch (Exception e) {
			dB.echoError("Problem saving listener '" + listenerId + "' for " + player.getName() + "!");
		}
		deconstructed();
	}
	
	/**
	 * Stores a field away for retrieving later. Should be used in the onSave() method.
	 * 
	 */
	public void store(String key, Object object) {
		saveable.put(key, object);
	}

}


