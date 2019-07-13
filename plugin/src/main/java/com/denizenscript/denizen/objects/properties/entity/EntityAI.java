package com.denizenscript.denizen.objects.properties.entity;

import com.denizenscript.denizen.nms.NMSHandler;
import com.denizenscript.denizen.objects.dEntity;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.properties.Property;
import com.denizenscript.denizencore.tags.Attribute;

public class EntityAI implements Property {

    public static boolean describes(ObjectTag entity) {
        return entity instanceof dEntity
                && ((dEntity) entity).isLivingEntity();
    }

    public static EntityAI getFrom(ObjectTag entity) {
        if (!describes(entity)) {
            return null;
        }
        else {
            return new EntityAI((dEntity) entity);
        }
    }

    public static final String[] handledTags = new String[] {
            "has_ai"
    };

    public static final String[] handledMechs = new String[] {
            "has_ai", "toggle_ai"
    };


    ///////////////////
    // Instance Fields and Methods
    /////////////

    private EntityAI(dEntity ent) {
        entity = ent;
    }

    dEntity entity;

    /////////
    // Property Methods
    ///////

    @Override
    public String getPropertyString() {
        return String.valueOf(!NMSHandler.getInstance().getEntityHelper().isAIDisabled(entity.getBukkitEntity()));
    }

    @Override
    public String getPropertyId() {
        return "has_ai";
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
        // @attribute <e@entity.has_ai>
        // @returns ElementTag(Boolean)
        // @group attributes
        // @description
        // Returns whether the entity uses the default Minecraft
        // AI to roam and look around.
        // -->
        if (attribute.startsWith("has_ai")) {
            return new ElementTag(!NMSHandler.getInstance().getEntityHelper().isAIDisabled(entity.getBukkitEntity()))
                    .getAttribute(attribute.fulfill(1));
        }


        return null;
    }

    @Override
    public void adjust(Mechanism mechanism) {

        // <--[mechanism]
        // @object dEntity
        // @name has_ai
        // @input Element(Boolean)
        // @description
        // Sets whether this entity will use the default
        // Minecraft AI to roam and look around.
        // @tags
        // <e@entity.has_ai>
        // -->
        if ((mechanism.matches("has_ai") || mechanism.matches("toggle_ai")) && mechanism.requireBoolean()) {
            NMSHandler.getInstance().getEntityHelper().toggleAI(entity.getBukkitEntity(), mechanism.getValue().asBoolean());
        }
    }
}
