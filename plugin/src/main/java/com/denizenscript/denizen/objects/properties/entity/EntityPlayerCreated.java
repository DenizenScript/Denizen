package com.denizenscript.denizen.objects.properties.entity;

import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.properties.Property;
import com.denizenscript.denizencore.tags.Attribute;
import org.bukkit.entity.IronGolem;

public class EntityPlayerCreated implements Property {

    public static boolean describes(ObjectTag entity) {
        return entity instanceof EntityTag
                && ((EntityTag) entity).getBukkitEntity() instanceof IronGolem;
    }

    public static EntityPlayerCreated getFrom(ObjectTag entity) {
        if (!describes(entity)) {
            return null;
        }
        else {
            return new EntityPlayerCreated((EntityTag) entity);
        }
    }

    public static final String[] handledTags = new String[] {
            "player_created"
    };

    public static final String[] handledMechs = new String[] {
            "player_created"
    };

    public EntityPlayerCreated(EntityTag entity) {
        dentity = entity;
    }

    EntityTag dentity;

    public IronGolem getGolem() {
        return (IronGolem) dentity.getBukkitEntity();
    }

    @Override
    public String getPropertyString() {
        return getGolem().isPlayerCreated() ? "true" : "false";
    }

    @Override
    public String getPropertyId() {
        return "player_created";
    }

    @Override
    public ObjectTag getObjectAttribute(Attribute attribute) {

        if (attribute == null) {
            return null;
        }

        // <--[tag]
        // @attribute <EntityTag.player_created>
        // @returns ElementTag(Boolean)
        // @mechanism EntityTag.player_created
        // @group properties
        // @description
        // Returns whether this Iron_Golem was created by a player.
        // -->
        if (attribute.startsWith("player_created")) {
            return new ElementTag(getGolem().isPlayerCreated())
                    .getObjectAttribute(attribute.fulfill(1));
        }

        return null;
    }

    @Override
    public void adjust(Mechanism mechanism) {

        // <--[mechanism]
        // @object EntityTag
        // @name player_created
        // @input ElementTag(Boolean)
        // @description
        // Sets whether this Iron_Golem was created by a player.
        // @tags
        // <EntityTag.player_created>
        // -->
        if (mechanism.matches("player_created") && mechanism.requireBoolean()) {
            getGolem().setPlayerCreated(mechanism.getValue().asBoolean());
        }
    }
}
