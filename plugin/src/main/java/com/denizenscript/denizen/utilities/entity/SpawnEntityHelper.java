package com.denizenscript.denizen.utilities.entity;

import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizen.scripts.containers.core.EntityScriptHelper;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.utilities.CoreUtilities;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.util.Consumer;

import java.util.ArrayList;

/**
 * Applies mechanisms to an entity before spawning it
 */
public class SpawnEntityHelper {

    @SuppressWarnings("unchecked")
    public static <T extends Entity> Entity spawn(Location location, EntityType bukkitEntityType, final ArrayList<Mechanism> mechanisms, final String scriptName) {
        Consumer<? extends Entity> consumer = (Consumer<Entity>) bukkitEntity -> {
            if (scriptName != null) {
                EntityScriptHelper.setEntityScript(bukkitEntity, scriptName);
            }
            EntityTag entity = new EntityTag(bukkitEntity);
            for (Mechanism mechanism : new ArrayList<>(mechanisms)) {
                if (EntityTag.earlyValidMechanisms.contains(CoreUtilities.toLowerCase(mechanism.getName()))) {
                    entity.safeAdjust(new Mechanism(mechanism.getName(), mechanism.value, mechanism.context));
                    //mechanisms.remove(mechanism);
                }
            }
        };
        return location.getWorld().spawn(location, (Class<T>) bukkitEntityType.getEntityClass(), (Consumer<T>) consumer);
    }
}
