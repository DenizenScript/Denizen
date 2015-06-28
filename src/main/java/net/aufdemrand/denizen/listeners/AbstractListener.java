package net.aufdemrand.denizen.listeners;

import net.aufdemrand.denizen.Denizen;
import net.aufdemrand.denizen.objects.dNPC;
import net.aufdemrand.denizen.objects.dPlayer;
import net.aufdemrand.denizen.utilities.debugging.dB;
import net.aufdemrand.denizencore.objects.aH;
import net.aufdemrand.denizencore.objects.dScript;
import org.bukkit.Bukkit;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public abstract class AbstractListener {

    protected Denizen denizen;

    protected String type;
    public String id;
    protected dPlayer player;
    protected dScript scriptName;
    protected dNPC npc;

    protected Map<String, Object> savable = new HashMap<String, Object>();

    public AbstractListener() {
        this.denizen = (Denizen) Bukkit.getServer().getPluginManager().getPlugin("Denizen");
    }

    public void build(dPlayer player,
                      String listenerId,
                      String listenerType,
                      List<aH.Argument> args,
                      dScript finishScript,
                      dNPC npc) {

        this.player = player;
        this.id = listenerId;
        this.type = listenerType;
        this.scriptName = finishScript;
        this.npc = npc;
        onBuild(args);
        save();
        constructed();
    }

    public void cancel() {
        onCancel();
        denizen.getListenerRegistry().cancel(player, id);
        deconstructed();
    }

    public abstract void constructed();

    public abstract void deconstructed();

    public void finish() {
        onFinish();
        denizen.getListenerRegistry().finish(player, npc, id, scriptName);
        deconstructed();
    }

    /**
     * Gets an Object that was store()d away.
     *
     * @param key the name (key) of the Object requested
     * @return the Object associated with the key
     */
    public Object get(String key) {
        return denizen.getSaves().get("Listeners." + player.getSaveName() + "." + id + "." + key);
    }

    public String getListenerId() {
        return id != null ? id : "";
    }

    public String getListenerType() {
        return type != null ? type : "";
    }

    public void load(dPlayer player, dNPC npc, String id, String listenerType) {
        this.player = player;
        this.id = id;
        this.type = listenerType;
        this.scriptName = dScript.valueOf((String) get("Finish Script"));
        this.npc = npc;
        try {
            onLoad();
        }
        catch (Exception e) {
            dB.echoError("Problem loading saved listener '" + id + "' for " + player.getName() + "!");
        }
        constructed();
    }

    /**
     * Method to handle building a new quest listener List<String> of arguments.
     * Most likely called from a LISTEN dScript command. The player and id fields
     * are non-null at this point.
     *
     * @param args a list of dScript arguments
     */
    public abstract void onBuild(List<aH.Argument> args);

    public abstract void onCancel();

    public abstract void onFinish();

    /**
     * Called when a Player logs on if an instance of this quest listener was saved
     * with progress. Any variables that were saved with the store(stringKey, object) method
     * should be called and restored.
     */
    public abstract void onLoad();

    /**
     * When a Player logs off, the quest listener's progress is stored to saves.yml.
     * This method should use the store(stringKey, object) method to save the fields needed to
     * successfully reload the current state of this quest listener when the onLoad()
     * method is called. The fields for player, type, and id are done automatically.
     */
    public abstract void onSave();

    /**
     * Sums up the current status of a this AbstractListenerInstance in a way that would be
     * useful by itself to a Player or Console administrator.
     * <p/>
     * Called by the '/denizen listener --report id' bukkit command.
     * <p/>
     * This should include all applicable variables when reporting. Suggested format would
     * follow suit with core Listeners. For example:
     * <p/>
     * return player.getName() + " currently has quest listener '" + id
     * + "' active and must kill " + Arrays.toString(targets.toArray())
     * + " '" + type.name() + "'(s). Current progress '" + currentKills
     * + "/" + quantity + "'.";
     * <p/>
     * Output:
     * aufdemrand currently has quest listener 'example_quest' active and must kill
     * '[ZOMBIE, SKELETON] ENTITY'(s). Current progress '10/15'.
     * <p/>
     * Note: This is not intended to be a 'Quest Log' for a Player, rather is used when
     * administrators/server operators are checking up on this Listener. Ideally,
     * that kind of information should be handled with the use of replaceable tags.
     *
     * @return a 'formatted' String that contains current progress
     */
    public abstract String report();

    public void save() {
        denizen.getSaves().set("Listeners." + player.getSaveName() + "." + id
                + ".Listener Type", type);
        denizen.getSaves().set("Listeners." + player.getSaveName() + "." + id
                + ".Finish Script", scriptName.toString());
        if (npc != null) denizen.getSaves().set("Listeners." + player.getSaveName() + "."
                + id + ".Linked NPCID", npc.getId());

        onSave();

        try {
            if (!savable.isEmpty())
                for (Entry<String, Object> entry : savable.entrySet())
                    denizen.getSaves().set("Listeners." + player.getSaveName() + "." + id + "." + entry.getKey(), entry.getValue());
        }
        catch (Exception e) {
            dB.echoError("Problem saving listener '" + id + "' for " + player.getSaveName() + "!");
        }

        deconstructed();
    }

    /**
     * Stores a field away for retrieving later. Should be used in the onSave() method.
     */
    public void store(String key, Object object) {
        savable.put(key, object);
    }
}


