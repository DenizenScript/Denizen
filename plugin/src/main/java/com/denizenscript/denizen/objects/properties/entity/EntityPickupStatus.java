package com.denizenscript.denizen.objects.properties.entity;

import com.denizenscript.denizen.nms.NMSHandler;
import com.denizenscript.denizen.objects.dEntity;
import com.denizenscript.denizencore.objects.Element;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.dObject;
import com.denizenscript.denizencore.objects.properties.Property;
import com.denizenscript.denizencore.tags.Attribute;
import org.bukkit.entity.Arrow;

public class EntityPickupStatus implements Property {

    public static boolean describes(dObject entity) {
        return entity instanceof dEntity && ((dEntity) entity).getBukkitEntity() instanceof Arrow;
    }

    public static EntityPickupStatus getFrom(dObject entity) {
        if (!describes(entity)) {
            return null;
        }
        return new EntityPickupStatus((dEntity) entity);
    }

    public static final String[] handledTags = {
            "pickup_status"
    };

    public static final String[] handledMechs = {
            "pickup_status"
    };


    ///////////////////
    // Instance Fields and Methods
    /////////////

    private EntityPickupStatus(dEntity entity) {
        dentity = entity;
    }

    dEntity dentity;

    /////////
    // Property Methods
    ///////

    @Override
    public String getPropertyString() {
        return NMSHandler.getInstance().getEntityHelper().getArrowPickupStatus(dentity.getBukkitEntity());
    }

    @Override
    public String getPropertyId() {
        return "pickup_status";
    }

    ///////////
    // dObject Attributes
    ////////

    @Override
    public String getAttribute(Attribute attribute) {

        if (attribute == null) {
            return "null";
        }

        // <--[tag]
        // @attribute <e@entity.pickup_status>
        // @returns Element
        // @mechanism dEntity.pickup_status
        // @group properties
        // @description
        // If the entity is an arrow or trident, returns the pickup status of the arrow/trident.
        // -->
        if (attribute.startsWith("pickup_status")) {
            return new Element(NMSHandler.getInstance().getEntityHelper().getArrowPickupStatus(dentity.getBukkitEntity()))
                    .getAttribute(attribute.fulfill(1));
        }

        return null;
    }

    @Override
    public void adjust(Mechanism mechanism) {

        // <--[mechanism]
        // @object dEntity
        // @name pickup_status
        // @input Element
        // @description
        // Changes the pickup status of an arrow/trident.
        // Available pickup statuses can be found here: <@link url https://hub.spigotmc.org/javadocs/spigot/org/bukkit/entity/Arrow.PickupStatus.html>
        // @tags
        // <e@entity.pickup_status>
        // -->

        if (mechanism.matches("pickup_status")) {
            NMSHandler.getInstance().getEntityHelper().setArrowPickupStatus(dentity.getBukkitEntity(), mechanism.getValue().asString().toUpperCase());
        }
    }
}
