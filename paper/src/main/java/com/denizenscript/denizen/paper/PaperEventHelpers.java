package com.denizenscript.denizen.paper;

import com.denizenscript.denizen.objects.InventoryTag;
import com.denizenscript.denizen.scripts.containers.core.InventoryScriptContainer;
import com.denizenscript.denizen.utilities.inventory.InventoryViewUtil;
import com.denizenscript.denizencore.objects.core.ScriptTag;
import com.destroystokyo.paper.event.player.PlayerRecipeBookClickEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class PaperEventHelpers implements Listener {

    @EventHandler
    public void onRecipeBookClick(PlayerRecipeBookClickEvent event) {
        InventoryTag inventory = InventoryTag.mirrorBukkitInventory(InventoryViewUtil.getTopInventory(event.getPlayer().getOpenInventory()));
        if (inventory.getIdHolder() instanceof ScriptTag) {
            if (((InventoryScriptContainer) ((ScriptTag) inventory.getIdHolder()).getContainer()).gui) {
                event.setCancelled(true);
            }
        }
    }
}
