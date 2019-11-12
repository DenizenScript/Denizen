package com.denizenscript.denizen.objects.properties.entity;

import com.denizenscript.denizen.nms.NMSHandler;
import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.properties.Property;
import com.denizenscript.denizencore.tags.Attribute;

public class EntityAI implements Property {

    public static boolean describes(ObjectTag entity) {
        return entity instanceof EntityTag
                && ((EntityTag) entity).isLivingEntity();
    }

    public static EntityAI getFrom(ObjectTag entity) {
        if (!describes(entity)) {
            return null;
        }
        else {
            return new EntityAI((EntityTag) entity);
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

    private EntityAI(EntityTag ent) {
        entity = ent;
    }

    EntityTag entity;

    /////////
    // Property Methods
    ///////

    @Override
    public String getPropertyString() {
        return String.valueOf(!NMSHandler.getEntityHelper().isAIDisabled(entity.getBukkitEntity()));
    }

    @Override
    public String getPropertyId() {
        return "has_ai";
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
        // @attribute <EntityTag.has_ai>
        // @returns ElementTag(Boolean)
        // @group attributes
        // @description
        // Returns whether the entity uses the default Minecraft AI to roam and look around.
        // This tends to have implications for other vanilla functionality, including gravity.
        // This generally shouldn't be used with NPCs. NPCs do not have vanilla AI, regardless of what this tag returns.
        // Other programmatic methods of blocking AI might also not be accounted for by this tag.
        // -->
        if (attribute.startsWith("has_ai")) {
            return new ElementTag(!NMSHandler.getEntityHelper().isAIDisabled(entity.getBukkitEntity()))
                    .getObjectAttribute(attribute.fulfill(1));
        }


        return null;
    }

    @Override
    public void adjust(Mechanism mechanism) {

        // <--[mechanism]
        // @object EntityTag
        // @name has_ai
        // @input ElementTag(Boolean)
        // @description
        // Sets whether this entity will use the default Minecraft AI to roam and look around.
        // This tends to have implications for other vanilla functionality, including gravity.
        // @tags
        // <EntityTag.has_ai>
        // -->
        if ((mechanism.matches("has_ai") || mechanism.matches("toggle_ai")) && mechanism.requireBoolean()) {
            NMSHandler.getEntityHelper().toggleAI(entity.getBukkitEntity(), mechanism.getValue().asBoolean());
        }
    }
}
