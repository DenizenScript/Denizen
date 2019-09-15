package com.denizenscript.denizen.objects.properties.entity;

import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.properties.Property;
import com.denizenscript.denizencore.tags.Attribute;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Guardian;

public class EntityElder implements Property {

    public static boolean describes(ObjectTag entity) {
        return entity instanceof EntityTag && ((EntityTag) entity).getBukkitEntityType() == EntityType.GUARDIAN;
    }

    public static EntityElder getFrom(ObjectTag entity) {
        if (!describes(entity)) {
            return null;
        }
        else {
            return new EntityElder((EntityTag) entity);
        }
    }

    public static final String[] handledTags = new String[] {
            "elder"
    };

    public static final String[] handledMechs = new String[] {
            "elder"
    };


    ///////////////////
    // Instance Fields and Methods
    /////////////

    private EntityElder(EntityTag entity) {
        this.entity = entity;
    }

    EntityTag entity;

    private boolean getElder() {
        return ((Guardian) (entity.getBukkitEntity())).isElder();
    }

    private void setElder(boolean elder) {
        if (entity == null) {
            return;
        }

        ((Guardian) (entity.getBukkitEntity())).setElder(elder);
    }

    /////////
    // Property Methods
    ///////

    @Override
    public String getPropertyString() {
        if (!getElder()) {
            return null;
        }
        else {
            return "true";
        }
    }

    @Override
    public String getPropertyId() {
        return "elder";
    }

    ///////////
    // ObjectTag Attributes
    ////////

    @Override
    public ObjectTag getObjectAttribute(Attribute attribute) {

        if (attribute == null) {
            return null;
        }

        // <--[tag]
        // @attribute <EntityTag.elder>
        // @returns ElementTag(Boolean)
        // @mechanism EntityTag.elder
        // @group properties
        // @description
        // If the entity is a guardian, returns whether it is elder.
        // -->
        if (attribute.startsWith("elder")) {
            return new ElementTag(getElder())
                    .getObjectAttribute(attribute.fulfill(1));
        }

        return null;
    }

    @Override
    public void adjust(Mechanism mechanism) {

        // <--[mechanism]
        // @object EntityTag
        // @name elder
        // @input Element(Boolean)
        // @description
        // Changes the elder state of a Guardian.
        // @tags
        // <EntityTag.elder>
        // -->

        if (mechanism.matches("elder") && mechanism.requireBoolean()) {
            setElder(mechanism.getValue().asBoolean());
        }
    }
}
