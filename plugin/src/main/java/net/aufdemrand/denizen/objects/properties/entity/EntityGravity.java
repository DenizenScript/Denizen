package net.aufdemrand.denizen.objects.properties.entity;

import net.aufdemrand.denizen.nms.NMSHandler;
import net.aufdemrand.denizen.nms.NMSVersion;
import net.aufdemrand.denizen.objects.dEntity;
import net.aufdemrand.denizencore.objects.Element;
import net.aufdemrand.denizencore.objects.Mechanism;
import net.aufdemrand.denizencore.objects.dObject;
import net.aufdemrand.denizencore.objects.properties.Property;
import net.aufdemrand.denizencore.tags.Attribute;
import org.bukkit.entity.ArmorStand;

public class EntityGravity implements Property {

    public static boolean describes(dObject entity) {
        if (NMSHandler.getVersion().isAtLeast(NMSVersion.v1_10_R1)) {
            return entity instanceof dEntity;
        }
        return entity instanceof dEntity && ((dEntity) entity).getBukkitEntity() instanceof ArmorStand;
    }

    public static EntityGravity getFrom(dObject entity) {
        if (!describes(entity)) {
            return null;
        }
        else {
            return new EntityGravity((dEntity) entity);
        }
    }

    public static final String[] handledTags = new String[]{
            "gravity"
    };

    public static final String[] handledMechs = new String[] {
            "gravity"
    };


    ///////////////////
    // Instance Fields and Methods
    /////////////

    private EntityGravity(dEntity entity) {
        dentity = entity;
    }

    dEntity dentity;

    /////////
    // Property Methods
    ///////

    @Override
    public String getPropertyString() {
        if (NMSHandler.getVersion().isAtLeast(NMSVersion.v1_10_R1)) {
            if (dentity.getBukkitEntity().hasGravity()) {
                return null;
            }
            else {
                return "false";
            }
        }

        if (((ArmorStand) dentity.getBukkitEntity()).hasGravity()) {
            return null;
        }
        else {
            return "false";
        }
    }

    @Override
    public String getPropertyId() {
        return "gravity";
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
        // @attribute <e@entity.gravity>
        // @returns Element(Boolean)
        // @mechanism dEntity.gravity
        // @group properties
        // @description
        // Returns whether the entity has gravity.
        // -->
        if (attribute.startsWith("gravity")) {
            if (NMSHandler.getVersion().isAtLeast(NMSVersion.v1_10_R1)) {
                return new Element(dentity.getBukkitEntity().hasGravity())
                        .getAttribute(attribute.fulfill(1));
            }
            return new Element(((ArmorStand) dentity.getBukkitEntity()).hasGravity())
                    .getAttribute(attribute.fulfill(1));
        }

        return null;
    }

    @Override
    public void adjust(Mechanism mechanism) {

        // <--[mechanism]
        // @object dEntity
        // @name gravity
        // @input Element(Boolean)
        // @description
        // Changes the gravity state of an entity.
        // @tags
        // <e@entity.gravity>
        // -->

        if (mechanism.matches("gravity") && mechanism.requireBoolean()) {
            if (NMSHandler.getVersion().isAtLeast(NMSVersion.v1_10_R1)) {
                dentity.getBukkitEntity().setGravity(mechanism.getValue().asBoolean());
            }
            else {
                ((ArmorStand) dentity.getBukkitEntity()).setGravity(mechanism.getValue().asBoolean());
            }
        }
    }
}
