package com.denizenscript.denizen.objects.properties.entity;

import com.denizenscript.denizen.nms.NMSHandler;
import com.denizenscript.denizen.objects.dEntity;
import com.denizenscript.denizencore.objects.ElementTag;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.properties.Property;
import com.denizenscript.denizencore.tags.Attribute;

public class EntitySilent implements Property {

    public static boolean describes(ObjectTag entity) {
        return entity instanceof dEntity;
    }

    public static EntitySilent getFrom(ObjectTag entity) {
        if (!describes(entity)) {
            return null;
        }
        else {
            return new EntitySilent((dEntity) entity);
        }
    }

    public static final String[] handledTags = new String[] {
            "silent"
    };

    public static final String[] handledMechs = new String[] {
            "silent"
    };


    ///////////////////
    // Instance Fields and Methods
    /////////////

    private EntitySilent(dEntity ent) {
        entity = ent;
    }

    dEntity entity;

    /////////
    // Property Methods
    ///////

    @Override
    public String getPropertyString() {
        return NMSHandler.getInstance().getEntityHelper().isSilent(entity.getBukkitEntity()) ? "true" : null;
    }

    @Override
    public String getPropertyId() {
        return "silent";
    }


    ///////////
    // ObjectTag Attributes
    ////////

    @Override
    public String getAttribute(Attribute attribute) {

        if (attribute == null) {
            return null;
        }

        // <--[tag]
        // @attribute <e@entity.silent>
        // @returns ElementTag(Boolean)
        // @group attributes
        // @description
        // Returns whether the entity is silent. (Plays no sounds)
        // -->
        if (attribute.startsWith("silent")) {
            return new ElementTag(NMSHandler.getInstance().getEntityHelper().isSilent(entity.getBukkitEntity()))
                    .getAttribute(attribute.fulfill(1));
        }


        return null;
    }

    @Override
    public void adjust(Mechanism mechanism) {

        // <--[mechanism]
        // @object dEntity
        // @name silent
        // @input Element(Boolean)
        // @description
        // Sets whether this entity is silent. (Plays no sounds)
        // @tags
        // <e@entity.silent>
        // -->
        if (mechanism.matches("silent") && mechanism.requireBoolean()) {
            NMSHandler.getInstance().getEntityHelper().setSilent(entity.getBukkitEntity(), mechanism.getValue().asBoolean());
        }
    }
}
