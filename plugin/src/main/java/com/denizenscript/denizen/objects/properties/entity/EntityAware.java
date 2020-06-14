package com.denizenscript.denizen.objects.properties.entity;

import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.properties.Property;
import com.denizenscript.denizencore.objects.properties.PropertyParser;
import org.bukkit.entity.Mob;

public class EntityAware implements Property {

    public static boolean describes(ObjectTag entity) {
        return entity instanceof EntityTag && ((EntityTag) entity).getBukkitEntity() instanceof Mob;
    }

    public static EntityAware getFrom(ObjectTag entity) {
        if (!describes(entity)) {
            return null;
        }
        return new EntityAware((EntityTag) entity);
    }

    public static final String[] handledMechs = new String[] {
            "is_aware"
    };

    private EntityAware(EntityTag entity) {
        this.entity = entity;
    }

    EntityTag entity;

    @Override
    public String getPropertyString() {
        return String.valueOf(((Mob) entity.getBukkitEntity()).isAware());
    }

    @Override
    public String getPropertyId() {
        return "is_aware";
    }

    public static void registerTags() {

        // <--[tag]
        // @attribute <EntityTag.is_aware>
        // @returns ElementTag(Boolean)
        // @mechanism EntityTag.is_aware
        // @group attributes
        // @description
        // Returns whether the entity is aware of its surroundings.
        // Unaware entities will not perform any actions on their own, such as pathfinding or attacking.
        // Similar to <@link tag EntityTag.has_ai>, except allows the entity to be moved by gravity, being pushed or attacked, etc.
        // -->
        PropertyParser.<EntityAware>registerTag("is_aware", (attribute, entity) -> {
            return new ElementTag(((Mob) entity.entity.getBukkitEntity()).isAware());
        });
    }

    @Override
    public void adjust(Mechanism mechanism) {

        // <--[mechanism]
        // @object EntityTag
        // @name is_aware
        // @input ElementTag(Boolean)
        // @description
        // Sets whether the entity is aware of its surroundings.
        // Unaware entities will not perform any actions on their own, such as pathfinding or attacking.
        // Similar to <@link mechanism EntityTag.has_ai>, except allows the entity to be moved by gravity, being pushed or attacked, etc.
        // @tags
        // <EntityTag.is_aware>
        // -->
        if (mechanism.matches("is_aware") && mechanism.requireBoolean()) {
            ((Mob) entity.getBukkitEntity()).setAware(mechanism.getValue().asBoolean());
        }
    }
}
