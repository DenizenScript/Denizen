package net.aufdemrand.denizen.tags.core;

import net.aufdemrand.denizen.Denizen;
import net.aufdemrand.denizen.tags.BukkitTagContext;
import net.aufdemrand.denizen.utilities.debugging.dB;
import net.aufdemrand.denizencore.tags.ReplaceableTagEvent;
import net.aufdemrand.denizencore.tags.TagManager;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.trait.Anchors;
import org.bukkit.Location;
import org.bukkit.event.Listener;

@Deprecated
public class AnchorTags implements Listener {

    public AnchorTags(Denizen denizen) {
        denizen.getServer().getPluginManager().registerEvents(this, denizen);
        TagManager.registerTagEvents(this);
    }

    @TagManager.TagEvents
    public void anchorTags(ReplaceableTagEvent event) {
        if (!event.matches("ANCHOR")) {
            return;
        }

        dB.echoError(event.getAttributes().getScriptEntry().getResidingQueue(), "anchor: tags are deprecated! Use <npc.anchor[]>!");
        NPC npc = null;
        if (event.getType() != null
                && event.getType().matches("\\d+")) {
            npc = CitizensAPI.getNPCRegistry().getById(Integer.valueOf(event.getType()));
        }
        else if (((BukkitTagContext) event.getContext()).npc != null) {
            npc = ((BukkitTagContext) event.getContext()).npc.getCitizen();
        }
        if (npc == null) {
            return;
        }

        if (npc.getTrait(Anchors.class).getAnchor(event.getValue()) != null) {
            Location anchor = npc.getTrait(Anchors.class).getAnchor(event.getValue()).getLocation();
            event.setReplaced(anchor.getX() + "," + anchor.getY() + "," + anchor.getZ() + "," + anchor.getWorld().getName());
        }
    }
}
