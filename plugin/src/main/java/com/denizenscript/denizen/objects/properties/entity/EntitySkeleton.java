package com.denizenscript.denizen.objects.properties.entity;

import com.denizenscript.denizen.utilities.debugging.Debug;
import com.denizenscript.denizen.objects.dEntity;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.properties.Property;
import com.denizenscript.denizencore.tags.Attribute;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Skeleton;

public class EntitySkeleton implements Property {

    public static boolean describes(ObjectTag entity) {
        return entity instanceof dEntity && ((dEntity) entity).getBukkitEntity() instanceof Skeleton;
    }

    public static EntitySkeleton getFrom(ObjectTag entity) {
        if (!describes(entity)) {
            return null;
        }
        else {
            return new EntitySkeleton((dEntity) entity);
        }
    }

    public static final String[] handledTags = new String[] {
            "skeleton_type"
    };

    public static final String[] handledMechs = new String[] {
            "skeleton"
    };


    ///////////////////
    // Instance Fields and Methods
    /////////////

    private EntitySkeleton(dEntity entity) {
        skeleton = entity;
    }

    dEntity skeleton;

    /////////
    // Property Methods
    ///////

    @Override
    public String getPropertyString() {
        return ((Skeleton) skeleton.getBukkitEntity()).getSkeletonType().name();
    }

    @Override
    public String getPropertyId() {
        return "skeleton";
    }

    ///////////
    // ObjectTag Attributes
    ////////

    @Override
    public String getAttribute(Attribute attribute) {

        if (attribute == null) {
            return null;
        }

        if (attribute.startsWith("skeleton_type")) {
            Debug.echoError("Different skeleton types are represented by different entity types. Please remove usage of the 'skeleton_type' tag.");
            return new ElementTag(((Skeleton) skeleton.getBukkitEntity())
                    .getSkeletonType().name()).getAttribute(attribute.fulfill(1));
        }

        return null;
    }

    @Override
    public void adjust(Mechanism mechanism) {

        if (mechanism.matches("skeleton") && mechanism.requireEnum(false, Skeleton.SkeletonType.values())) {
            Debug.echoError("Different skeleton types are represented by different entity types. Please remove usage of the 'skeleton:<type>' mechanism.");
            String skelGoal = mechanism.getValue().asString().toUpperCase();
            if (((Skeleton) skeleton.getBukkitEntity()).getSkeletonType().name().equals(skelGoal)) {
                return;
            }
            Skeleton.SkeletonType skeletonType = Skeleton.SkeletonType.valueOf(skelGoal);
            Skeleton current = (Skeleton) skeleton.getBukkitEntity();
            Skeleton newSkeleton = null;
            switch (skeletonType) {
                case NORMAL:
                    newSkeleton = (Skeleton) current.getLocation().getWorld().spawnEntity(current.getLocation(), EntityType.SKELETON);
                    break;
                case WITHER:
                    newSkeleton = (Skeleton) current.getLocation().getWorld().spawnEntity(current.getLocation(), EntityType.WITHER_SKELETON);
                    break;
                case STRAY:
                    newSkeleton = (Skeleton) current.getLocation().getWorld().spawnEntity(current.getLocation(), EntityType.STRAY);
                    break;
            }
            newSkeleton.setHealth(current.getHealth());
            newSkeleton.getEquipment().setArmorContents(current.getEquipment().getArmorContents());
            current.remove();
            skeleton.setEntity(newSkeleton);
        }
    }
}
