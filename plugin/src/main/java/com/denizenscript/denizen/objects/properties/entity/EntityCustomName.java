package com.denizenscript.denizen.objects.properties.entity;

import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizen.utilities.PaperAPITools;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.properties.Property;
import com.denizenscript.denizencore.objects.properties.PropertyParser;
import com.denizenscript.denizencore.utilities.CoreUtilities;

public class EntityCustomName implements Property {

    public static boolean describes(ObjectTag entity) {
        return entity instanceof EntityTag;
    }

    public static EntityCustomName getFrom(ObjectTag entity) {
        if (!describes(entity)) {
            return null;
        }
        else {
            return new EntityCustomName((EntityTag) entity);
        }
    }

    public EntityCustomName(EntityTag ent) {
        entity = ent;
    }

    EntityTag entity;

    @Override
    public String getPropertyString() {
        return PaperAPITools.instance.getCustomName(entity.getBukkitEntity());
    }

    @Override
    public String getPropertyId() {
        return "custom_name";
    }

    public static void register() {

        // <--[tag]
        // @attribute <EntityTag.custom_name>
        // @returns ElementTag
        // @mechanism EntityTag.custom_name
        // @group attributes
        // @description
        // Returns the entity's custom name (as set by plugin or name tag item), if any.
        // -->
        PropertyParser.registerTag(EntityCustomName.class, ElementTag.class, "custom_name", (attribute, object) -> {
            String name = PaperAPITools.instance.getCustomName(object.entity.getBukkitEntity());
            if (name == null) {
                return null;
            }
            return new ElementTag(name, true);
        });

        // <--[mechanism]
        // @object EntityTag
        // @name custom_name
        // @input ElementTag
        // @description
        // Sets the custom name (equivalent to a name tag item) of the entity.
        // Provide no input to remove the custom name.
        // @tags
        // <EntityTag.custom_name>
        // -->
        PropertyParser.registerMechanism(EntityCustomName.class, "custom_name", (object, mechanism) -> {
            PaperAPITools.instance.setCustomName(object.entity.getBukkitEntity(), mechanism.value != null ? CoreUtilities.clearNBSPs(mechanism.getValue().asString()) : null);
        });
    }
}
