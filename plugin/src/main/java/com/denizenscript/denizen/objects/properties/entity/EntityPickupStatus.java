package com.denizenscript.denizen.objects.properties.entity;

import com.denizenscript.denizen.nms.NMSHandler;
import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.properties.Property;
import com.denizenscript.denizencore.tags.Attribute;
import org.bukkit.entity.Arrow;

public class EntityPickupStatus implements Property {

    public static boolean describes(ObjectTag entity) {
        return entity instanceof EntityTag && ((EntityTag) entity).getBukkitEntity() instanceof Arrow;
    }

    public static EntityPickupStatus getFrom(ObjectTag entity) {
        if (!describes(entity)) {
            return null;
        }
        return new EntityPickupStatus((EntityTag) entity);
    }

    public static final String[] handledTags = {
            "pickup_status"
    };

    public static final String[] handledMechs = {
            "pickup_status"
    };

    private EntityPickupStatus(EntityTag entity) {
        dentity = entity;
    }

    EntityTag dentity;

    @Override
    public String getPropertyString() {
        return NMSHandler.getEntityHelper().getArrowPickupStatus(dentity.getBukkitEntity());
    }

    @Override
    public String getPropertyId() {
        return "pickup_status";
    }

    @Override
    public ObjectTag getObjectAttribute(Attribute attribute) {

        if (attribute == null) {
            return null;
        }

        // <--[tag]
        // @attribute <EntityTag.pickup_status>
        // @returns ElementTag
        // @mechanism EntityTag.pickup_status
        // @group properties
        // @description
        // If the entity is an arrow or trident, returns the pickup status of the arrow/trident.
        // Available pickup statuses can be found here: <@link url https://hub.spigotmc.org/javadocs/spigot/org/bukkit/entity/AbstractArrow.PickupStatus.html>.
        // -->
        if (attribute.startsWith("pickup_status")) {
            return new ElementTag(NMSHandler.getEntityHelper().getArrowPickupStatus(dentity.getBukkitEntity()))
                    .getObjectAttribute(attribute.fulfill(1));
        }

        return null;
    }

    @Override
    public void adjust(Mechanism mechanism) {

        // <--[mechanism]
        // @object EntityTag
        // @name pickup_status
        // @input ElementTag
        // @description
        // Changes the pickup status of an arrow/trident.
        // Available pickup statuses can be found here: <@link url https://hub.spigotmc.org/javadocs/spigot/org/bukkit/entity/AbstractArrow.PickupStatus.html>.
        // @tags
        // <EntityTag.pickup_status>
        // -->
        if (mechanism.matches("pickup_status")) {
            NMSHandler.getEntityHelper().setArrowPickupStatus(dentity.getBukkitEntity(), mechanism.getValue().asString().toUpperCase());
        }
    }
}
