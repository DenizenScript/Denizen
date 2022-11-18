package com.denizenscript.denizen.objects.properties.entity;

import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.properties.Property;
import com.denizenscript.denizencore.objects.properties.PropertyParser;

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

    public static final String[] handledMechs = new String[] {
            "has_ai", "toggle_ai"
    };

    private EntityAI(EntityTag ent) {
        entity = ent;
    }

    EntityTag entity;

    @Override
    public String getPropertyString() {
        return String.valueOf(entity.getLivingEntity().hasAI());
    }

    @Override
    public String getPropertyId() {
        return "has_ai";
    }

    public static void register() {

        // <--[tag]
        // @attribute <EntityTag.has_ai>
        // @returns ElementTag(Boolean)
        // @mechanism EntityTag.has_ai
        // @group attributes
        // @description
        // Returns whether the entity uses the default Minecraft AI to roam and look around.
        // This tends to have implications for other vanilla functionality, including gravity.
        // This generally shouldn't be used with NPCs. NPCs do not have vanilla AI, regardless of what this tag returns.
        // Other programmatic methods of blocking AI might also not be accounted for by this tag.
        // -->
        PropertyParser.registerTag(EntityAI.class, ElementTag.class, "has_ai", (attribute, object) -> {
            return new ElementTag(object.entity.getLivingEntity().hasAI());
        });
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
            entity.getLivingEntity().setAI(mechanism.getValue().asBoolean());
        }
    }
}
