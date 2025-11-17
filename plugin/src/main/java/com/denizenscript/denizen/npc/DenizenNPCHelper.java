package com.denizenscript.denizen.npc;

import com.denizenscript.denizen.Denizen;
import com.denizenscript.denizen.events.entity.EntityDespawnScriptEvent;
import com.denizenscript.denizen.npc.actions.ActionHandler;
import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizen.objects.NPCTag;
import com.denizenscript.denizencore.utilities.debugging.Debug;
import com.denizenscript.denizen.utilities.depends.Depends;
import com.denizenscript.denizencore.objects.core.ElementTag;
import net.citizensnpcs.api.event.NPCDespawnEvent;
import net.citizensnpcs.api.event.NPCRemoveEvent;
import net.citizensnpcs.api.event.NPCSpawnEvent;
import net.citizensnpcs.api.npc.NPC;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

import java.util.*;

public class DenizenNPCHelper implements Listener {

    private ActionHandler actionHandler;

    public DenizenNPCHelper() {
        if (Depends.citizens != null) {
            Bukkit.getPluginManager().registerEvents(this, Denizen.getInstance());
        }
        actionHandler = new ActionHandler();
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
     * Returns a InventoryTag object from the Inventory trait of a valid NPC.
     *
     * @param npc the Citizens NPC
     * @return the NPC's InventoryTag
     */
    public static Inventory getInventory(NPC npc) {
        if (npc == null) {
            return null;
        }
        if (npc.isSpawned() && npc.getEntity() instanceof InventoryHolder) {
            return ((InventoryHolder) npc.getEntity()).getInventory();
        }
        else {
            try {
                NPCTag npcTag = new NPCTag(npc);
                Inventory inv = npcTag.getInventoryTrait().getInventoryView();
                if (inv != null) {
                    return inv;
                }
                else {
                    // TODO: ???
                    Inventory npcInventory = Bukkit.getServer().createInventory(npcTag, InventoryType.PLAYER);
                    npcInventory.setContents(Arrays.copyOf(npcTag.getInventoryTrait().getContents(), npcInventory.getSize()));
                    return npcInventory;
                }
            }
            catch (Exception e) {
                Debug.echoError(e);
                return null;
            }
        }
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
        NPCTag npc = new NPCTag(event.getNPC());
        if (npc.isValid()) {
            EntityDespawnScriptEvent.instance.entity = new EntityTag(event.getNPC().getEntity());
            EntityDespawnScriptEvent.instance.cause = new ElementTag("CITIZENS");
            EntityDespawnScriptEvent.instance.fire(event);
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
        new NPCTag(npc).action("remove", null);
    }
}
