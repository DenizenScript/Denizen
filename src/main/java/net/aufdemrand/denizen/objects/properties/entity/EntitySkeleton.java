package net.aufdemrand.denizen.objects.properties.entity;

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
        return entity instanceof dEntity && ((dEntity) entity).getBukkitEntityType() == EntityType.SKELETON;
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
        // Can return NORMAL or WITHER.
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
        // Valid input: NORMAL, WITHER.
        // @tags
        // <e@entity.skeleton_type>
        // -->

        if (mechanism.matches("skeleton") && mechanism.requireEnum(false, Skeleton.SkeletonType.values())) {
            ((Skeleton) skeleton.getBukkitEntity()).setSkeletonType(
                    Skeleton.SkeletonType.valueOf(mechanism.getValue().asString().toUpperCase()));
        }
    }
}
