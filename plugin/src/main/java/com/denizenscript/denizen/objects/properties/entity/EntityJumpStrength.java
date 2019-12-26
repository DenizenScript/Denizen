package com.denizenscript.denizen.objects.properties.entity;

import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.properties.Property;
import com.denizenscript.denizencore.tags.Attribute;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Donkey;
import org.bukkit.entity.Horse;
import org.bukkit.entity.Mule;

public class EntityJumpStrength implements Property {

    public static boolean describes(ObjectTag entity) {
        return entity instanceof EntityTag &&
                ((EntityTag) entity).getBukkitEntityType() == EntityType.HORSE ||
                ((EntityTag) entity).getBukkitEntityType() == EntityType.DONKEY ||
                ((EntityTag) entity).getBukkitEntityType() == EntityType.MULE;
    }

    public static EntityJumpStrength getFrom(ObjectTag entity) {
        if (!describes(entity)) {
            return null;
        }
        else {
            return new EntityJumpStrength((EntityTag) entity);
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

    private EntityJumpStrength(EntityTag ent) {
        entity = ent;
    }

    EntityTag entity;

    /////////
    // Property Methods
    ///////

    public boolean isMule() {
        return entity.getBukkitEntityType() == EntityType.MULE;
    }

    public boolean isDonkey() {
        return entity.getBukkitEntityType() == EntityType.DONKEY;
    }

    public double getJumpStrength() {
        if (isMule()) {
            return ((Mule) entity.getBukkitEntity()).getJumpStrength();
        }
        else if (isDonkey()) {
            return ((Donkey) entity.getBukkitEntity()).getJumpStrength();
        }
        else {
            return ((Horse) entity.getBukkitEntity()).getJumpStrength();
        }
    }

    public void setJumpStrength(double i) {
        if (isMule()) {
            ((Mule) entity.getBukkitEntity()).setJumpStrength(i);
        }
        else if (isDonkey()) {
            ((Donkey) entity.getBukkitEntity()).setJumpStrength(i);
        }
        else {
            ((Horse) entity.getBukkitEntity()).setJumpStrength(i);
        }
    }

    @Override
    public String getPropertyString() {
        return String.valueOf(getJumpStrength());
    }

    @Override
    public String getPropertyId() {
        return "jump_strength";
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
        // @attribute <EntityTag.jump_strength>
        // @returns ElementTag(Number)
        // @mechanism EntityTag.jump_strength
        // @group properties
        // @description
        // Returns the power of a horse's jump.
        // -->
        if (attribute.startsWith("jump_strength")) {
            return new ElementTag(getJumpStrength())
                    .getObjectAttribute(attribute.fulfill(1));
        }
        return null;
    }

    @Override
    public void adjust(Mechanism mechanism) {

        // <--[mechanism]
        // @object EntityTag
        // @name jump_strength
        // @input ElementTag(Number)
        // @description
        // Sets the power of the horse's jump.
        // @tags
        // <EntityTag.jump_strength>
        // -->
        if (mechanism.matches("jump_strength") && mechanism.requireDouble()) {
            setJumpStrength(mechanism.getValue().asDouble());
        }
    }
}

