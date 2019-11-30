package com.denizenscript.denizen.utilities.entity;

import com.denizenscript.denizen.utilities.debugging.Debug;
import com.denizenscript.denizen.utilities.Settings;
import com.denizenscript.denizen.nms.NMSHandler;
import com.denizenscript.denizen.nms.enums.CustomEntityType;
import com.denizenscript.denizen.nms.interfaces.CustomEntity;
import com.denizenscript.denizen.nms.interfaces.CustomEntityHelper;
import com.denizenscript.denizen.objects.ItemTag;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.utilities.CoreUtilities;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class DenizenEntityType {

    private static final Map<String, DenizenEntityType> registeredTypes = new HashMap<>();
    private final EntityType bukkitEntityType;
    private final String name;
    private final String lowercaseName;
    private final double gravity;
    private final CustomEntityType customEntityType;

    static {
        for (EntityType entityType : EntityType.values()) {
            registeredTypes.put(entityType.name(), new DenizenEntityType(entityType));
        }
    }

    // <--[language]
    // @name Denizen Entity Types
    // @group Useful Lists
    // @description
    // Along with the default EntityTypes <@link url https://hub.spigotmc.org/javadocs/spigot/org/bukkit/entity/EntityType.html>,
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
        this.customEntityType = null;
    }

    private DenizenEntityType(String name, Class<? extends CustomEntity> entityType) {
        this(name, entityType, 0.115);
    }

    private DenizenEntityType(String name, Class<? extends CustomEntity> entityType, double gravity) {
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
        this.customEntityType = CustomEntityType.valueOf(name.toUpperCase());
    }

    public Entity spawnNewEntity(Location location, ArrayList<Mechanism> mechanisms, String scriptName) {
        try {
            if (name.equals("DROPPED_ITEM")) {
                ItemStack itemStack = new ItemStack(Material.STONE);
                for (Mechanism mechanism : mechanisms) {
                    if (mechanism.matches("item") && mechanism.requireObject(ItemTag.class)) {
                        itemStack = mechanism.valueAsType(ItemTag.class).getItemStack();
                        break;
                    }
                }
                return location.getWorld().dropItem(location, itemStack);
            }
            else if (!isCustom()) {
                return SpawnEntityHelper.spawn(location, bukkitEntityType, mechanisms, scriptName);
            }
            else {
                CustomEntityHelper customEntityHelper = NMSHandler.getCustomEntityHelper();
                switch (customEntityType) {
                    case FAKE_ARROW:
                        return customEntityHelper.spawnFakeArrow(location);
                    case FAKE_PLAYER:
                        if (Settings.packetInterception()) {
                            String name = null;
                            String skin = null;
                            for (Mechanism mechanism : mechanisms) {
                                if (mechanism.matches("name")) {
                                    name = mechanism.getValue().asString();
                                }
                                else if (mechanism.matches("skin")) {
                                    skin = mechanism.getValue().asString();
                                }
                                if (name != null && skin != null) {
                                    break;
                                }
                            }
                            return customEntityHelper.spawnFakePlayer(location, name, skin);
                        }
                        break;
                    case ITEM_PROJECTILE:
                        ItemStack itemStack = new ItemStack(Material.STONE);
                        for (Mechanism mechanism : mechanisms) {
                            if (mechanism.matches("item") && mechanism.requireObject(ItemTag.class)) {
                                itemStack = mechanism.valueAsType(ItemTag.class).getItemStack();
                            }
                        }
                        return customEntityHelper.spawnItemProjectile(location, itemStack);
                }
            }
        }
        catch (Exception e) {
            Debug.echoError(e);
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

    public static void registerEntityType(String name, Class<? extends CustomEntity> entityType) {
        registeredTypes.put(name.toUpperCase(), new DenizenEntityType(name, entityType));
    }

    public static boolean isRegistered(String name) {
        return registeredTypes.containsKey(name.toUpperCase());
    }

    public static DenizenEntityType getByName(String name) {
        return registeredTypes.get(name.toUpperCase());
    }

    public static DenizenEntityType getByEntity(Entity entity) {
        if (entity instanceof CustomEntity) {
            return getByName(((CustomEntity) entity).getEntityTypeName());
        }
        else {
            return getByName(entity.getType().name());
        }
    }

    public boolean isCustom() {
        return customEntityType != null;
    }
}
