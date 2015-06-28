package net.aufdemrand.denizen.listeners;

import net.aufdemrand.denizen.BukkitScriptEntryData;
import net.aufdemrand.denizen.Denizen;
import net.aufdemrand.denizen.events.bukkit.ListenerCancelEvent;
import net.aufdemrand.denizen.events.bukkit.ListenerFinishEvent;
import net.aufdemrand.denizen.listeners.core.*;
import net.aufdemrand.denizen.objects.dNPC;
import net.aufdemrand.denizen.objects.dPlayer;
import net.aufdemrand.denizen.utilities.DenizenAPI;
import net.aufdemrand.denizen.utilities.debugging.dB;
import net.aufdemrand.denizencore.interfaces.RegistrationableInstance;
import net.aufdemrand.denizencore.interfaces.dRegistry;
import net.aufdemrand.denizencore.objects.dScript;
import net.aufdemrand.denizencore.scripts.containers.core.TaskScriptContainer;
import net.citizensnpcs.api.CitizensAPI;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;


/**
 * Keeps track of 'player listener' types and instances for the other various parts
 * of the API. Also provides some methods for adding/finishing/removing listeners
 * for players.
 *
 * @author Jeremy Schroeder
 * @version 1.0
 */
public class ListenerRegistry implements dRegistry, Listener {


    //
    // Keeps track of active listeners. Keyed by player name.
    // Value contains name of the listener, and the instance
    // associated.
    //
    private Map<String, Map<String, AbstractListener>>
            listeners = new ConcurrentHashMap<String, Map<String, AbstractListener>>();


    //
    // Stores registered listener types. Keyed by type name. Value
    // contains the ListenerType instance used to create new
    // listener instances.
    //
    private Map<String, AbstractListenerType>
            types = new ConcurrentHashMap<String, AbstractListenerType>();


    /**
     * Adds a new listener to the 'listeners' hash-map.
     *
     * @param player   the dPlayer
     * @param instance the listener instance
     * @param id       the id of the listener instance
     */
    public void addListenerFor(dPlayer player,
                               AbstractListener instance,
                               String id) {
        if (player == null || id == null) return;
        // Get current instances
        Map<String, AbstractListener> playerListeners;
        if (listeners.containsKey(player.getName()))
            playerListeners = listeners.get(player.getName());
        else
            playerListeners = new HashMap<String, AbstractListener>();
        // Insert instance into hash-map
        playerListeners.put(id.toLowerCase(), instance);
        listeners.put(player.getName(), playerListeners);
    }


    /**
     * Removes a listener instance from a Player. Cancelling an already-in-progress
     * instance? Use cancel() instead.
     *
     * @param player the dPlayer
     * @param id     the id of the listener instance
     */
    public void removeListenerFor(dPlayer player, String id) {
        if (player == null || id == null) return;
        // Get current instances
        Map<String, AbstractListener> playerListeners;
        if (listeners.containsKey(player.getName()))
            playerListeners = listeners.get(player.getName());
        else
            return;
        // Remove instance from hash-map
        playerListeners.remove(id.toLowerCase());
        listeners.put(player.getName(), playerListeners);
    }


    /**
     * Cancels a listener, effectively removing and destroying the instance.
     * Listeners in progress that are needing to be 'cancelled' should call
     * this method as it fires a bukkit/world script event, as well.
     *
     * @param player the dPlayer
     * @param id     id of the listener to cancel
     */
    public void cancel(dPlayer player, String id) {
        if (player == null || id == null) return;
        // Removes listener
        removeListenerFor(player, id);
        // Fires bukkit event
        Bukkit.getPluginManager()
                .callEvent(new ListenerCancelEvent(player, id));
    }


    /**
     * Finishes a listener, effectively removing and destroying the instance, but
     * calls the optional finish_script and bukkit/world script events associated.
     *
     * @param player    the dPlayer
     * @param npc       dNPC attached from the listen command (can be null)
     * @param id        id of the listener
     * @param on_finish dScript to run on finish
     */
    public void finish(dPlayer player,
                       dNPC npc,
                       String id,
                       dScript on_finish) {
        if (player == null || id == null) return;
        // Remove listener instance from the player
        removeListenerFor(player, id);
        // Run finish script
        if (on_finish != null)
            try {
                // TODO: Add context to this
                ((TaskScriptContainer) on_finish.getContainer())
                        .runTaskScript(new BukkitScriptEntryData(player, npc), null);
            }
            catch (Exception e) {
                // Hm, not a valid task script?
                dB.echoError("Tried to run the finish task for: " + id + "/" + player.getName() + ","
                        + "but it seems not to be valid!");
            }

        Bukkit.getPluginManager().callEvent(new ListenerFinishEvent(player, id));
    }

    public AbstractListener getListenerFor(dPlayer player, String listenerId) {
        if (listeners.containsKey(player.getName())) {
            Map<String, AbstractListener> playerListeners = listeners.get(player.getName());
            if (playerListeners.containsKey(listenerId.toLowerCase()))
                return playerListeners.get(listenerId.toLowerCase());
        }
        return null;
    }

    public Map<String, AbstractListener> getListenersFor(dPlayer player) {
        if (listeners.containsKey(player.getName())) {
            return listeners.get(player.getName());
        }
        return null;
    }


    ////////////////
    // dRegistry
    ////////////////


    @Override
    public boolean register(String registrationName, RegistrationableInstance listenerType) {
        // Registers a new ListenerType
        types.put(registrationName, (AbstractListenerType) listenerType);
        return false;
    }

    @Override
    public void registerCoreMembers() {
        // Registers all core listener types.
        new BlockListenerType().activate().as("BLOCK").withClass(BlockListenerInstance.class);
        new ItemListenerType().activate().as("ITEM").withClass(ItemListenerInstance.class);
        new KillListenerType().activate().as("KILL").withClass(KillListenerInstance.class);
        new ItemDropListenerType().activate().as("ITEMDROP").withClass(ItemDropListenerInstance.class);
        new TravelListenerType().activate().as("TRAVEL").withClass(TravelListenerInstance.class);
        // Registers this class with bukkit's event api
        DenizenAPI.getCurrentInstance().getServer().getPluginManager()
                .registerEvents(this, DenizenAPI.getCurrentInstance());
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
            }
            catch (Exception e) {
                dB.echoError("Unable to disable '" + member.getClass().getName() + "'!");
                dB.echoError(e);
            }
    }

    @Override
    public <T extends RegistrationableInstance> T get(Class<T> clazz) {
        if (types.containsValue(clazz)) {
            for (RegistrationableInstance ri : types.values())
                if (ri.getClass() == clazz)
                    return clazz.cast(ri);
        }
        return null;
    }

    @Override
    public AbstractListenerType get(String listenerType) {
        if (types.containsKey(listenerType.toUpperCase())) return types.get(listenerType.toUpperCase());
        return null;
    }

    @Override
    public Map<String, AbstractListenerType> list() {
        return types;
    }

    @EventHandler
    public void playerJoin(PlayerJoinEvent event) {

        Denizen denizen = DenizenAPI.getCurrentInstance();
        dPlayer player = new dPlayer(event.getPlayer());

        // Any saves quest listeners in progress?
        if (!denizen.getSaves().contains("Listeners." + player.getSaveName())) return;
        Set<String> inProgress = denizen.getSaves().getConfigurationSection("Listeners." + player.getSaveName()).getKeys(false);
        // If empty, no quest listeners to load.
        if (inProgress.isEmpty()) return;

        // TODO: Players.SAVENAME.Listeners?
        String path = "Listeners." + player.getSaveName() + ".";

        // If not empty, let's do the loading process for each.
        for (String listenerId : inProgress) {
            // People tend to worry when they see long-ass stacktraces.. let's catch them.
            try {
                String type = denizen.getSaves().getString(path + listenerId + ".Listener Type");
                dNPC npc = null;
                if (denizen.getSaves().contains(path + listenerId + ".Linked NPCID"))
                    npc = DenizenAPI.getDenizenNPC(CitizensAPI.getNPCRegistry().getById(denizen.getSaves().getInt(path + listenerId + ".Linked NPCID")));
                if (get(type) == null) return;
                dB.log(event.getPlayer().getName() + " has a LISTENER in progress. Loading '" + listenerId + "'.");
                get(type).createInstance(dPlayer.mirrorBukkitPlayer(event.getPlayer()), listenerId).load(dPlayer.mirrorBukkitPlayer(event.getPlayer()), npc, listenerId, type);
            }
            catch (Exception e) {
                dB.log(event.getPlayer() + " has a saved listener named '" + listenerId + "' that may be corrupt. Skipping for now, but perhaps check the contents of your saves.yml for problems?");
            }
        }
    }


    public void deconstructPlayer(dPlayer player) {
        Denizen denizen = DenizenAPI.getCurrentInstance();

        // Clear previous MemorySection in saves
        denizen.getSaves().set("Listeners." + player.getSaveName(), null);

        // If no quest listeners in progress, nothing else to do.
        if (!listeners.containsKey(player.getName())) {
            return;
        }

        // If there are quest listeners, invoke save() for each of them.
        for (Map.Entry<String, AbstractListener> entry : getListenersFor(player).entrySet()) {
            dB.log(player.getName() + " has a LISTENER in progress. Saving '" + entry.getKey() + "'.");
            entry.getValue().save();
        }

        // Remove all listeners from memory for Player
        listeners.remove(player); // TODO: this seems invalid
    }


    @EventHandler
    public void playerQuit(PlayerQuitEvent event) {
        deconstructPlayer(dPlayer.mirrorBukkitPlayer(event.getPlayer()));
    }
}
