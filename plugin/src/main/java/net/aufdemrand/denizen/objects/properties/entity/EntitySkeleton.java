package net.aufdemrand.denizen.objects.properties.entity;

import net.aufdemrand.denizen.nms.NMSHandler;
import net.aufdemrand.denizen.nms.NMSVersion;
import net.aufdemrand.denizen.objects.dEntity;
import net.aufdemrand.denizencore.objects.Element;
import net.aufdemrand.denizencore.objects.Mechanism;
import net.aufdemrand.denizencore.objects.dObject;
import net.aufdemrand.denizencore.objects.properties.Property;
import net.aufdemrand.denizencore.tags.Attribute;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Skeleton;

public class EntitySkeleton implements Property {

    public static boolean describes(dObject entity) {
        return entity instanceof dEntity && ((dEntity) entity).getBukkitEntity() instanceof Skeleton;
    }

    public static EntitySkeleton getFrom(dObject entity) {
        if (!describes(entity)) {
            return null;
        }
        else {
            return new EntitySkeleton((dEntity) entity);
        }
    }

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
    // dObject Attributes
    ////////

    @Override
    public String getAttribute(Attribute attribute) {

        if (attribute == null) {
            return null;
        }

        // <--[tag]
        // @attribute <e@entity.skeleton_type>
        // @returns Element(Boolean)
        // @mechanism dEntity.skeleton
        // @group properties
        // @description
        // If the entity is a skeleton, returns what type of skeleton it is.
        // Can return: NORMAL, WITHER, STRAY.
        // -->
        if (attribute.startsWith("skeleton_type")) {
            return new Element(((Skeleton) skeleton.getBukkitEntity())
                    .getSkeletonType().name()).getAttribute(attribute.fulfill(1));
        }

        return null;
    }

    @Override
    public void adjust(Mechanism mechanism) {

        // <--[mechanism]
        // @object dEntity
        // @name skeleton
        // @input Element
        // @description
        // Changes whether a skeleton is a normal or wither type skeleton.
        // Valid input: NORMAL, WITHER, STRAY.
        // @tags
        // <e@entity.skeleton_type>
        // -->

        if (mechanism.matches("skeleton") && mechanism.requireEnum(false, Skeleton.SkeletonType.values())) {
            String skelGoal = mechanism.getValue().asString().toUpperCase();
            if (((Skeleton) skeleton.getBukkitEntity()).getSkeletonType().name().equals(skelGoal)) {
                return;
            }
            if (NMSHandler.getVersion().isAtLeast(NMSVersion.v1_11_R1)) {
                // TODO: improve this
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
            else {
                ((Skeleton) skeleton.getBukkitEntity()).setSkeletonType(Skeleton.SkeletonType.valueOf(skelGoal));
            }
        }
    }
}
