package net.aufdemrand.denizen.utilities.entity;

import net.aufdemrand.denizen.objects.dEntity;
import net.aufdemrand.denizen.scripts.containers.core.EntityScriptHelper;
import net.aufdemrand.denizencore.objects.Element;
import net.aufdemrand.denizencore.objects.Mechanism;
import net.aufdemrand.denizencore.utilities.CoreUtilities;
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
        Consumer<? extends Entity> consumer = new Consumer<Entity>() {
            @Override
            public void accept(Entity bukkitEntity) {
                if (scriptName != null) {
                    EntityScriptHelper.setEntityScript(bukkitEntity, scriptName);
                }
                dEntity entity = new dEntity(bukkitEntity);
                for (Mechanism mechanism : new ArrayList<Mechanism>(mechanisms)) {
                    if (dEntity.earlyValidMechanisms.contains(CoreUtilities.toLowerCase(mechanism.getName()))) {
                        entity.safeAdjust(new Mechanism(new Element(mechanism.getName()), mechanism.getValue(), mechanism.context));
                        mechanisms.remove(mechanism);
                    }
                }
            }
        };
        return location.getWorld().spawn(location, (Class<T>) bukkitEntityType.getEntityClass(), (Consumer<T>) consumer);
    }
}
