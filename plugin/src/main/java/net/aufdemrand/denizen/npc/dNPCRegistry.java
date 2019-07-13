package net.aufdemrand.denizen.npc;

import net.aufdemrand.denizen.BukkitScriptEntryData;
import net.aufdemrand.denizen.Denizen;
import net.aufdemrand.denizen.flags.FlagManager;
import net.aufdemrand.denizen.nms.util.ReflectionHelper;
import net.aufdemrand.denizen.npc.actions.ActionHandler;
import net.aufdemrand.denizen.objects.dNPC;
import net.aufdemrand.denizen.utilities.DenizenAPI;
import net.aufdemrand.denizen.utilities.debugging.dB;
import net.aufdemrand.denizen.utilities.depends.Depends;
import com.denizenscript.denizencore.events.OldEventManager;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.event.NPCDespawnEvent;
import net.citizensnpcs.api.event.NPCRemoveEvent;
import net.citizensnpcs.api.event.NPCSpawnEvent;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.trait.trait.Equipment;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Used for keeping track and retrieving dNPC objects which offer some Denizen-specific methods when dealing with NPCs.
 */
public class dNPCRegistry implements Listener {

    private static Map<Integer, dNPC> denizenNPCs = new ConcurrentHashMap<>(8, 0.9f, 1);
    //private static Map<Integer, Inventory> npcInventories = new ConcurrentHashMap<Integer, Inventory>(8, 0.9f, 1);

    public static dNPCRegistry getCurrentInstance() {
        return DenizenAPI.getCurrentInstance().getNPCRegistry();
    }

    private Denizen plugin;
    private ActionHandler actionHandler;

    public dNPCRegistry(Denizen denizen) {
        plugin = denizen;
        if (Depends.citizens != null) {
            plugin.getServer().getPluginManager().registerEvents(this, plugin);
            INVENTORY_TRAIT_VIEW = ReflectionHelper.getFields(net.citizensnpcs.api.trait.trait.Inventory.class).get("view");
        }
        actionHandler = new ActionHandler(plugin);
    }

    /**
     * Gets the currently loaded instance of the ActionHandler
     *
     * @return ActionHandler
     */
    public ActionHandler getActionHandler() {
        return actionHandler;
    }

    public static boolean _isRegistered(NPC npc) {
        return denizenNPCs.containsKey(npc.getId());
    }

    public static boolean _isRegistered(int id) {
        return denizenNPCs.containsKey(id);
    }

    public static void _registerNPC(dNPC denizenNPC) {
        if (denizenNPC == null || !denizenNPC.isValid()) {
            return;
        }
        int id = denizenNPC.getId();
        if (!denizenNPCs.containsKey(id)) {
            denizenNPCs.put(id, denizenNPC);
            //Inventory npcInventory = Bukkit.getServer().createInventory(denizenNPC, InventoryType.PLAYER);
            //npcInventory.setContents(Arrays.copyOf(denizenNPC.getInventoryTrait().getContents(), npcInventory.getSize()));
            //npcInventories.put(id, npcInventory);
        }
    }

    private static void _registerNPC(NPC npc) {
        if (npc == null) {
            return;
        }
        if (!denizenNPCs.containsKey(npc.getId())) {
            _registerNPC(new dNPC(npc));
        }
    }

    /**
     * Returns a dNPC object when given a valid NPC. DenizenNPCs have some methods
     * specific to Denizen functionality as well as easy access to the attached NPC and LivingEntity.
     *
     * @param npc the Citizens NPC
     * @return a dNPC
     */
    public static dNPC getDenizen(NPC npc) {
        return new dNPC(npc);
    }

    public static dNPC getDenizen(int id) {
        return new dNPC(CitizensAPI.getNPCRegistry().getById(id));
    }

    /**
     * Returns a dInventory object from the Inventory trait of a valid NPC.
     *
     * @param npc the Citizens NPC
     * @return the NPC's dInventory
     */
    public static Inventory getInventory(NPC npc) {
        if (npc == null) {
            return null;
        }
        /*if (!npcInventories.containsKey(npc.getId())) {
            _registerNPC(npc);
        }
        return npcInventories.get(npc.getId());*/
        if (npc.isSpawned() && npc.getEntity() instanceof InventoryHolder) {
            return ((InventoryHolder) npc.getEntity()).getInventory();
        }
        else {
            try {
                Inventory inv = (Inventory) INVENTORY_TRAIT_VIEW.get(getDenizen(npc).getInventoryTrait());
                if (inv != null) {
                    return inv;
                }
                else {
                    // TODO: ???
                    Inventory npcInventory = Bukkit.getServer().createInventory(getDenizen(npc), InventoryType.PLAYER);
                    npcInventory.setContents(Arrays.copyOf(getDenizen(npc).getInventoryTrait().getContents(), npcInventory.getSize()));
                    return npcInventory;
                }
            }
            catch (Exception e) {
                dB.echoError(e);
                return null;
            }
        }
    }

    public static Field INVENTORY_TRAIT_VIEW;

    /**
     * Similar to getting NPCs from Citizens' NPCRegistry, but this will filter out
     * unspawned NPCs
     */
    public static Set<dNPC> getSpawnedNPCs() {
        Iterator<Map.Entry<Integer, dNPC>> it = denizenNPCs.entrySet().iterator();
        Set<dNPC> npcs = new HashSet<>();
        while (it.hasNext()) {
            Map.Entry<Integer, dNPC> npc = it.next();
            if (npc.getValue().getCitizen() == null) {
                try {
                    denizenNPCs.remove(npc.getKey());
                }
                catch (Exception e) {
                    dB.echoError("Report this error to the Denizen team! Err: posconcurrency1");
                }
            }
            else if (npc.getValue().isSpawned()) {
                npcs.add(npc.getValue());
            }
        }
        return npcs;
    }

    // <--[action]
    // @Actions
    // spawn
    // @Triggers when the NPC is spawned.
    // This will fire whenever an NPC's chunk is loaded, or a spawn command is issued.
    //
    // @Context
    // None
    // -->

    /**
     * Fires the 'On Spawn:' action in an NPCs Assignment, if set.
     *
     * @param event NPCSpawnEvent
     */
    @EventHandler
    public void onSpawn(NPCSpawnEvent event) {
        if (event.getNPC() == null) {
            dB.echoError("Null NPC spawned!");
            return;
        }
        _registerNPC(event.getNPC());
        // Do world script event 'On NPC spawns'
        OldEventManager.doEvents(Arrays.asList
                        ("npc spawns"),
                new BukkitScriptEntryData(null, dNPC.mirrorCitizensNPC(event.getNPC())), null);
        // On Spawn action
        new dNPC(event.getNPC()).action("spawn", null);
    }


    // <--[action]
    // @Actions
    // despawn
    // @Triggers when the NPC is despawned.
    // This can be because a command was issued, or a chunk has been unloaded.
    //
    // @Context
    // None
    // -->

    /**
     * Fires a world script event and then NPC action when the NPC despawns.
     *
     * @param event NPCDespawnEvent
     */
    @EventHandler
    public void despawn(NPCDespawnEvent event) {
        dNPC npc = getDenizen(event.getNPC());

        // Do world script event 'On NPC Despawns'
        if (npc != null && npc.isValid()) {
            OldEventManager.doEvents(Arrays.asList("npc despawns"), new BukkitScriptEntryData(null, npc), null);
        }

        if (npc != null && npc.isValid()) {
            npc.action("despawn", null);
        }
    }


    // <--[action]
    // @Actions
    // remove
    // @Triggers when the NPC is removed.
    //
    // @Context
    // None
    // -->

    /**
     * Removes an NPC from the Registry when removed from Citizens.
     *
     * @param event NPCRemoveEvent
     */
    @EventHandler
    public void onRemove(NPCRemoveEvent event) {
        NPC npc = event.getNPC();
        getDenizen(npc).action("remove", null);
        if (_isRegistered(npc)) {
            denizenNPCs.remove(npc.getId());
            //npcInventories.remove(npc.getId());
        }
        FlagManager.clearNPCFlags(npc.getId());
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        Inventory inventory = event.getInventory();
        if (inventory.getHolder() instanceof dNPC) {
            dNPC npc = (dNPC) inventory.getHolder();
            npc.getInventory().setContents(inventory.getContents());
            Equipment equipment = npc.getEquipmentTrait();
            for (int i = 0; i < 5; i++) {
                equipment.set(i, inventory.getItem(i));
            }
        }
    }
}
