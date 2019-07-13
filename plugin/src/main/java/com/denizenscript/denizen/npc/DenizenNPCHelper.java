package com.denizenscript.denizen.npc;

import com.denizenscript.denizen.Denizen;
import com.denizenscript.denizen.flags.FlagManager;
import com.denizenscript.denizen.npc.actions.ActionHandler;
import com.denizenscript.denizen.objects.NPCTag;
import com.denizenscript.denizen.utilities.DenizenAPI;
import com.denizenscript.denizen.utilities.debugging.Debug;
import com.denizenscript.denizen.utilities.depends.Depends;
import com.denizenscript.denizen.BukkitScriptEntryData;
import com.denizenscript.denizen.nms.util.ReflectionHelper;
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

public class DenizenNPCHelper implements Listener {

    public static DenizenNPCHelper getCurrentInstance() {
        return DenizenAPI.getCurrentInstance().getNPCHelper();
    }

    private Denizen plugin;
    private ActionHandler actionHandler;

    public DenizenNPCHelper(Denizen denizen) {
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


    /**
     * Returns a NPCTag object when given a valid NPC. DenizenNPCs have some methods
     * specific to Denizen functionality as well as easy access to the attached NPC and LivingEntity.
     *
     * @param npc the Citizens NPC
     * @return a NPCTag
     */
    public static NPCTag getDenizen(NPC npc) {
        return new NPCTag(npc);
    }

    public static NPCTag getDenizen(int id) {
        return new NPCTag(CitizensAPI.getNPCRegistry().getById(id));
    }

    /**
     * Returns a InventoryTag object from the Inventory trait of a valid NPC.
     *
     * @param npc the Citizens NPC
     * @return the NPC's InventoryTag
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
                Debug.echoError(e);
                return null;
            }
        }
    }

    public static Field INVENTORY_TRAIT_VIEW;

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
            Debug.echoError("Null NPC spawned!");
            return;
        }
        // On Spawn action
        new NPCTag(event.getNPC()).action("spawn", null);
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
        NPCTag npc = getDenizen(event.getNPC());

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
        FlagManager.clearNPCFlags(npc.getId());
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        Inventory inventory = event.getInventory();
        if (inventory.getHolder() instanceof NPCTag) {
            NPCTag npc = (NPCTag) inventory.getHolder();
            npc.getInventory().setContents(inventory.getContents());
            Equipment equipment = npc.getEquipmentTrait();
            for (int i = 0; i < 5; i++) {
                equipment.set(i, inventory.getItem(i));
            }
        }
    }
}
