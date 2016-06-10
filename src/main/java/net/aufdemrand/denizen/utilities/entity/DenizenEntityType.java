package net.aufdemrand.denizen.utilities.entity;

import net.aufdemrand.denizen.objects.dItem;
import net.aufdemrand.denizen.utilities.debugging.dB;
import net.aufdemrand.denizencore.objects.Mechanism;
import net.aufdemrand.denizencore.utilities.CoreUtilities;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class DenizenEntityType {

    private static final Map<String, DenizenEntityType> registeredTypes = new HashMap<String, DenizenEntityType>();
    private final EntityType bukkitEntityType;
    private final String name;
    private final String lowercaseName;
    private final double gravity;
    private final Method createMethod;

    static {
        for (EntityType entityType : EntityType.values()) {
            registeredTypes.put(entityType.name(), new DenizenEntityType(entityType));
        }
    }

    // <--[language]
    // @name Denizen Entity Types
    // @group Entities
    // @description
    // Along with the default EntityTypes (https://hub.spigotmc.org/javadocs/spigot/org/bukkit/entity/EntityType.html),
    // Denizen also adds in a few altered entities:
    // - FAKE_ARROW: For use when you want an arrow to stay spawned at a location for any reason.
    // - FAKE_PLAYER: Spawns a fake player (non-Citizens NPC).
    //   Use with the mechanisms "name" and "skin" to alter the respective properties.
    // - ITEM_PROJECTILE: Use this when you want to fire any item as if it were a normal projectile.
    //   It will have the same physics (although sometimes not clientside) as arrows, and will fire the
    //   projectile hit event.
    // -->

    private DenizenEntityType(EntityType entityType) {
        this.bukkitEntityType = entityType;
        this.name = entityType.name();
        this.lowercaseName = CoreUtilities.toLowerCase(name);
        this.gravity = Gravity.getGravity(entityType);
        this.createMethod = null;
    }

    private DenizenEntityType(String name, Class<? extends DenizenCustomEntity> entityType) {
        this(name, entityType, 0.115);
    }

    private DenizenEntityType(String name, Class<? extends DenizenCustomEntity> entityType, double gravity) {
        EntityType bukkitEntityType = EntityType.UNKNOWN;
        if (entityType != null) {
            for (EntityType type : EntityType.values()) {
                Class<? extends Entity> clazz = type.getEntityClass();
                if (clazz != null && clazz.isAssignableFrom(entityType)) {
                    bukkitEntityType = type;
                    break;
                }
            }
        }
        this.bukkitEntityType = bukkitEntityType;
        this.name = name.toUpperCase();
        this.lowercaseName = CoreUtilities.toLowerCase(name);
        this.gravity = gravity;
        Method finalMethod = null;
        if (entityType != null) {
            for (Method method : entityType.getDeclaredMethods()) {
                if (method.isAnnotationPresent(DenizenCustomEntity.CreateEntity.class)) {
                    finalMethod = method;
                    break;
                }
            }
        }
        this.createMethod = finalMethod;
    }

    public Entity spawnNewEntity(Location location, ArrayList<Mechanism> mechanisms) {
        try {
            if (name.equals("DROPPED_ITEM")) {
                ItemStack itemStack = new ItemStack(Material.STONE);
                for (Mechanism mechanism : mechanisms) {
                    if (mechanism.matches("item") && mechanism.requireObject(dItem.class)) {
                        itemStack = mechanism.getValue().asType(dItem.class).getItemStack();
                        break;
                    }
                }
                return location.getWorld().dropItem(location, itemStack);
            }
            else if (!isCustom()) {
                return location.getWorld().spawnEntity(location, bukkitEntityType);
            }
            else {
                return (Entity) createMethod.invoke(null, location, mechanisms);
            }
        }
        catch (Exception e) {
            dB.echoError(e);
        }
        return null;
    }

    public String getName() {
        return this.name;
    }

    public String getLowercaseName() {
        return this.lowercaseName;
    }

    public double getGravity() {
        return this.gravity;
    }

    public EntityType getBukkitEntityType() {
        return bukkitEntityType;
    }

    public static void registerEntityType(String name, Class<? extends DenizenCustomEntity> entityType) {
        registeredTypes.put(name.toUpperCase(), new DenizenEntityType(name, entityType));
    }

    public static boolean isRegistered(String name) {
        return registeredTypes.containsKey(name.toUpperCase());
    }

    public static DenizenEntityType getByName(String name) {
        return registeredTypes.get(name.toUpperCase());
    }

    public static DenizenEntityType getByEntity(Entity entity) {
        if (entity instanceof DenizenCustomEntity) {
            return getByName(((DenizenCustomEntity) entity).getEntityTypeName());
        }
        else {
            return getByName(entity.getType().name());
        }
    }

    public boolean isCustom() {
        return createMethod != null;
    }
}
