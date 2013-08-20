package net.aufdemrand.denizen.tags.core;

import net.aufdemrand.denizen.Denizen;
import net.aufdemrand.denizen.events.ReplaceableTagEvent;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.trait.Anchors;

import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

@Deprecated
public class AnchorTags implements Listener {

    public AnchorTags(Denizen denizen) {
        denizen.getServer().getPluginManager().registerEvents(this, denizen);
    }

    @EventHandler
    public void constantTags(ReplaceableTagEvent event) {
        if (!event.matches("ANCHOR")) return;

        NPC npc = null;
        if (event.getType() != null
                && event.getType().matches("\\d+"))
            npc = CitizensAPI.getNPCRegistry().getById(Integer.valueOf(event.getType()));
        else if (event.getNPC() != null)
            npc = event.getNPC().getCitizen();
        if (npc == null) return;

        if (npc.getTrait(Anchors.class).getAnchor(event.getValue()) != null) {
            Location anchor = npc.getTrait(Anchors.class).getAnchor(event.getValue()).getLocation();
            event.setReplaced(anchor.getX() + "," + anchor.getY() + "," + anchor.getZ() + "," + anchor.getWorld().getName());
        }
    }
}