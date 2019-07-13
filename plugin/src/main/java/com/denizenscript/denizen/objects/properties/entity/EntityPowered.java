package com.denizenscript.denizen.objects.properties.entity;

import com.denizenscript.denizen.objects.dEntity;
import com.denizenscript.denizencore.objects.Element;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.dObject;
import com.denizenscript.denizencore.objects.properties.Property;
import com.denizenscript.denizencore.tags.Attribute;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.EntityType;

public class EntityPowered implements Property {

    public static boolean describes(dObject entity) {
        return entity instanceof dEntity && ((dEntity) entity).getBukkitEntityType() == EntityType.CREEPER;
    }

    public static EntityPowered getFrom(dObject entity) {
        if (!describes(entity)) {
            return null;
        }
        else {
            return new EntityPowered((dEntity) entity);
        }
    }

    public static final String[] handledTags = new String[] {
            "powered"
    };

    public static final String[] handledMechs = new String[] {
            "powered"
    };


    ///////////////////
    // Instance Fields and Methods
    /////////////

    private EntityPowered(dEntity entity) {
        powered = entity;
    }

    dEntity powered;

    private boolean getPowered() {
        return ((Creeper) (powered.getBukkitEntity())).isPowered();
    }

    private void setPowered(boolean power) {
        if (powered == null) {
            return;
        }

        ((Creeper) (powered.getBukkitEntity())).setPowered(power);
    }

    /////////
    // Property Methods
    ///////

    @Override
    public String getPropertyString() {
        if (!getPowered()) {
            return null;
        }
        else {
            return "true";
        }
    }

    @Override
    public String getPropertyId() {
        return "powered";
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
        // @attribute <e@entity.powered>
        // @returns Element(Boolean)
        // @mechanism dEntity.powered
        // @group properties
        // @description
        // If the entity is a creeper, returns whether the creeper is powered.
        // -->
        if (attribute.startsWith("powered")) {
            return new Element(getPowered())
                    .getAttribute(attribute.fulfill(1));
        }

        return null;
    }

    @Override
    public void adjust(Mechanism mechanism) {

        // <--[mechanism]
        // @object dEntity
        // @name powered
        // @input Element(Boolean)
        // @description
        // Changes the powered state of a Creeper.
        // @tags
        // <e@entity.powered>
        // -->

        if (mechanism.matches("powered") && mechanism.requireBoolean()) {
            setPowered(mechanism.getValue().asBoolean());
        }
    }
}
