package com.denizenscript.denizen.utilities.entity;

import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizen.scripts.containers.core.EntityScriptHelper;
import com.denizenscript.denizen.utilities.PaperAPITools;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.utilities.CoreUtilities;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.util.Consumer;

import java.util.ArrayList;

/**
 * Applies mechanisms to an entity before spawning it
 */
public class SpawnEntityHelper {

    public static <T extends Entity> Entity spawn(Location location, EntityType bukkitEntityType, final ArrayList<Mechanism> mechanisms, final String scriptName, final CreatureSpawnEvent.SpawnReason reason) {
        Consumer<? extends Entity> consumer = (Consumer<Entity>) bukkitEntity -> {
            if (scriptName != null) {
                EntityScriptHelper.setEntityScript(bukkitEntity, scriptName);
            }
            EntityTag entity = new EntityTag(bukkitEntity);
            for (Mechanism mechanism : new ArrayList<>(mechanisms)) {
                if (EntityTag.earlyValidMechanisms.contains(CoreUtilities.toLowerCase(mechanism.getName()))) {
                    entity.safeAdjustDuplicate(mechanism);
                    //mechanisms.remove(mechanism);
                }
            }
        };
        return PaperAPITools.instance.spawnEntity(location, (Class<T>) bukkitEntityType.getEntityClass(), (Consumer<T>) consumer, reason);
    }
}
