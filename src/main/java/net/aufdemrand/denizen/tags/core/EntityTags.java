package net.aufdemrand.denizen.tags.core;

import net.aufdemrand.denizen.Denizen;
import net.aufdemrand.denizen.events.bukkit.ReplaceableTagEvent;
import net.aufdemrand.denizen.objects.dEntity;
import net.aufdemrand.denizen.objects.dPlayer;
import net.aufdemrand.denizen.tags.Attribute;
import net.aufdemrand.denizen.utilities.DenizenAPI;
import net.aufdemrand.denizen.utilities.debugging.dB;
import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.metadata.FixedMetadataValue;

public class EntityTags implements Listener {

    public EntityTags(Denizen denizen) {
        denizen.getServer().getPluginManager().registerEvents(this, denizen);
    }

    ///////////
    // Entity Spawn Handling
    /////////

    @EventHandler(priority = EventPriority.MONITOR)
    public void creatureSpawn(CreatureSpawnEvent event) {

        String reason = event.getSpawnReason().name();
        Entity entity = event.getEntity();

        // Write the entity's SpawnReason to it's metadata
        entity.setMetadata("spawnreason", new FixedMetadataValue(DenizenAPI.getCurrentInstance(), reason));
    }

    //////////
    //  ReplaceableTagEvent handler
    ////////

    @EventHandler
    public void playerTags(ReplaceableTagEvent event) {

        if (!event.matches("entity, e") || event.replaced()) return;

        // Build a new attribute out of the raw_tag supplied in the script to be fulfilled
        Attribute attribute = new Attribute(event.raw_tag, event.getScriptEntry());

        dEntity e = null;

        // Entity tag may specify a new entity in the <entity[context]...> portion of the tag.
        if (attribute.hasContext(1))
            // Check if this is a valid player and update the dPlayer object reference.
            if (attribute.getIntContext(1) >= 1)
               e = dEntity.valueOf(attribute.getContext(1));
            else {
                dB.echoDebug(event.getScriptEntry(), "Could not match '"
                        + attribute.getContext(1) + "' to a valid entity!");
                return;
            }

        if (e == null || !e.isValid()) {
            dB.echoDebug(event.getScriptEntry(), "Invalid or missing entity for tag <" + event.raw_tag + ">!");
            event.setReplaced("null");
            return;
        }

        event.setReplaced(e.getAttribute(attribute.fulfill(1)));
    }
}
