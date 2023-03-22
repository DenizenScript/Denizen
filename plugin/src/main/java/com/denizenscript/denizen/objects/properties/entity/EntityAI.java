package com.denizenscript.denizen.objects.properties.entity;

import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.properties.PropertyParser;

public class EntityAI extends EntityProperty {

    public static boolean describes(EntityTag entity) {
        return entity.isLivingEntityType();
    }

    @Override
    public ElementTag getPropertyValue() {
        return new ElementTag(getLivingEntity().hasAI());
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
        PropertyParser.registerTag(EntityAI.class, ElementTag.class, "has_ai", (attribute, prop) -> {
            return new ElementTag(prop.getLivingEntity().hasAI());
        });

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
        PropertyParser.registerMechanism(EntityAI.class, ElementTag.class, "has_ai", (prop, mechanism, param) -> {
            if (mechanism.requireBoolean()) {
                prop.getLivingEntity().setAI(param.asBoolean());
            }
        }, "toggle_ai");
    }
}
