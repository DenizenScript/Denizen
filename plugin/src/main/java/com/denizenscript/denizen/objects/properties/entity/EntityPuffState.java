package com.denizenscript.denizen.objects.properties.entity;

import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.properties.Property;
import com.denizenscript.denizencore.objects.properties.PropertyParser;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.PufferFish;

public class EntityPuffState implements Property {

    public static boolean describes(ObjectTag entity) {
        if (!(entity instanceof EntityTag)) {
            return false;
        }
        return ((EntityTag) entity).getBukkitEntityType() == EntityType.PUFFERFISH;
    }

    public static EntityPuffState getFrom(ObjectTag entity) {
        if (!describes(entity)) {
            return null;
        }
        else {
            return new EntityPuffState((EntityTag) entity);
        }
    }

    private EntityPuffState(EntityTag entity) {
        this.entity = entity;
    }

    EntityTag entity;

    @Override
    public String getPropertyString() {
        return new ElementTag(getPufferFish().getPuffState()).identify();
    }

    @Override
    public String getPropertyId() {
        return "puffstate";
    }

    public static void registerTags() {

        // <---[tag]
        // @attribute <EntityTag.puffstate>
        // @returns ElementTag(Number)
        // @mechanism EntityTag.puffstate
        // @group properties
        // @description
        // Returns the puff state of a Pufferfish.
        // -->
        PropertyParser.registerTag(EntityPuffState.class, ElementTag.class, "puffstate", (attribute, object) -> {
            return new ElementTag(object.getPufferFish().getPuffState());
        });

        // <---[mechanism]
        // @object EntityTag
        // @name puffstate
        // @input ElementTag(Number)
        // @description
        // Sets the puff state of a Pufferfish.
        // If this is higher than 2 the Pufferfish keeps inflated.
        // @tags
        // <EntityTag.puffstate>
        // -->
        PropertyParser.registerMechanism(EntityPuffState.class, ElementTag.class, "puffstate", (object, mechanism, input) -> {
            int puffState = input.asInt();
            object.getPufferFish().setPuffState(puffState);
        });
    }

    public PufferFish getPufferFish() {
        return (PufferFish) entity.getBukkitEntity();
    }
}