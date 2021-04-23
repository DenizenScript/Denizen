package com.denizenscript.denizen.objects.properties.entity;

import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.properties.Property;
import com.denizenscript.denizencore.tags.Attribute;
import com.denizenscript.denizencore.utilities.CoreUtilities;

public class EntityCustomName implements Property {

    public static boolean describes(ObjectTag entity) {
        return entity instanceof EntityTag;
    }

    public static EntityCustomName getFrom(ObjectTag entity) {
        if (!describes(entity)) {
            return null;
        }
        return new EntityCustomName((EntityTag) entity);
    }

    public static final String[] handledTags = new String[] {
            "custom_name"
    };

    public static final String[] handledMechs = new String[] {
            "custom_name"
    };

    private EntityCustomName(EntityTag ent) {
        entity = ent;
    }

    EntityTag entity;

    @Override
    public String getPropertyString() {
        return entity.getBukkitEntity().getCustomName();
    }

    @Override
    public String getPropertyId() {
        return "custom_name";
    }

    @Override
    public ObjectTag getObjectAttribute(Attribute attribute) {

        if (attribute == null) {
            return null;
        }

        // <--[tag]
        // @attribute <EntityTag.custom_name>
        // @returns ElementTag
        // @mechanism EntityTag.custom_name
        // @group attributes
        // @description
        // Returns the entity's custom name (as set by plugin or name tag item), if any.
        // -->
        else if (attribute.startsWith("custom_name")) {
            String name = entity.getBukkitEntity().getCustomName();
            if (name == null) {
                return null;
            }
            else {
                return new ElementTag(name).getObjectAttribute(attribute.fulfill(1));
            }
        }
        else {
            return null;
        }
    }

    @Override
    public void adjust(Mechanism mechanism) {

        // <--[mechanism]
        // @object EntityTag
        // @name custom_name
        // @input ElementTag
        // @description
        // Sets the custom name (equivalent to a name tag item) of the entity.
        // @tags
        // <EntityTag.custom_name>
        // -->
        if (mechanism.matches("custom_name")) {
            entity.getBukkitEntity().setCustomName(CoreUtilities.clearNBSPs(mechanism.getValue().asString()));
        }

    }
}
