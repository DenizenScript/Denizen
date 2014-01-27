package net.aufdemrand.denizen.tags.core;

import net.aufdemrand.denizen.Denizen;
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
}
