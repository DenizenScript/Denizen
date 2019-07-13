package com.denizenscript.denizen.objects.properties.entity;

import com.denizenscript.denizen.objects.dEntity;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.properties.Property;
import com.denizenscript.denizencore.tags.Attribute;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Horse;

public class EntityJumpStrength implements Property {


    public static boolean describes(ObjectTag entity) {
        return entity instanceof dEntity &&
                ((dEntity) entity).getBukkitEntityType() == EntityType.HORSE;
    }

    public static EntityJumpStrength getFrom(ObjectTag entity) {
        if (!describes(entity)) {
            return null;
        }
        else {
            return new EntityJumpStrength((dEntity) entity);
        }
    }

    public static final String[] handledTags = new String[] {
            "jump_strength"
    };

    public static final String[] handledMechs = new String[] {
            "jump_strength"
    };


    ///////////////////
    // Instance Fields and Methods
    /////////////

    private EntityJumpStrength(dEntity ent) {
        entity = ent;
    }

    dEntity entity;

    /////////
    // Property Methods
    ///////

    @Override
    public String getPropertyString() {
        return String.valueOf(((Horse) entity.getBukkitEntity()).getJumpStrength());
    }

    @Override
    public String getPropertyId() {
        return "jump_strength";
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
        // @attribute <e@entity.jump_strength>
        // @returns ElementTag(Number)
        // @mechanism dEntity.jump_strength
        // @group properties
        // @description
        // Returns the power of a horse's jump.
        // -->
        if (attribute.startsWith("jump_strength")) {
            return new ElementTag(((Horse) entity.getBukkitEntity()).getJumpStrength())
                    .getAttribute(attribute.fulfill(1));
        }

        return null;
    }

    @Override
    public void adjust(Mechanism mechanism) {

        // <--[mechanism]
        // @object dEntity
        // @name jump_strength
        // @input Element(Number)
        // @description
        // Sets the power of the horse's jump.
        // @tags
        // <e@entity.jump_strength>
        // -->

        if (mechanism.matches("jump_strength") && mechanism.requireDouble()) {
            ((Horse) entity.getBukkitEntity()).setJumpStrength(mechanism.getValue().asDouble());
        }
    }
}

