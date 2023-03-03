package com.denizenscript.denizen.paper.properties;

import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.properties.Property;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.properties.PropertyParser;
import io.papermc.paper.entity.Frictional;
import net.kyori.adventure.util.TriState;

public class EntityFriction implements Property {

    public static boolean describes(ObjectTag entity) {
        return entity instanceof EntityTag
                && ((EntityTag) entity).getBukkitEntity() instanceof Frictional;
    }

    public static EntityFriction getFrom(ObjectTag entity) {
        if (!describes(entity)) {
            return null;
        }
        return new EntityFriction((EntityTag) entity);
    }

    private EntityFriction(EntityTag ent) {
        entity = ent;
    }

    EntityTag entity;

    @Override
    public String getPropertyString() {
        Boolean frictionState = ((Frictional) entity.getBukkitEntity()).getFrictionState().toBoolean();
        if (frictionState == null) {
            return null;
        }
        return String.valueOf(frictionState);
    }

    @Override
    public String getPropertyId() {
        return "has_friction";
    }

    public static void register() {

        // <--[tag]
        // @attribute <EntityTag.has_friction>
        // @returns ElementTag(Boolean)
        // @mechanism EntityTag.has_friction
        // @group properties
        // @Plugin Paper
        // @description
        // Returns an entity's friction state if one has been set.
        // -->
        PropertyParser.registerTag(EntityFriction.class, ElementTag.class, "has_friction", (attribute, object) -> {
            Boolean frictionState = ((Frictional) object.entity.getBukkitEntity()).getFrictionState().toBoolean();
            if (frictionState == null) {
                return null;
            }
            return new ElementTag(frictionState);
        });

        // <--[mechanism]
        // @object EntityTag
        // @name has_friction
        // @input ElementTag(Boolean)
        // @Plugin Paper
        // @description
        // Forces an entity into a friction state, so it either always or never experiences friction.
        // An entity with no friction will move in a direction forever until its velocity is changed or it impacts a block.
        // Does not work with players. Provide empty input to reset an entity back to its vanilla friction behavior.
        // @tags
        // <EntityTag.has_friction>
        // -->
        PropertyParser.registerMechanism(EntityFriction.class, ElementTag.class, "has_friction", (object, mechanism, input) -> {
            if (!mechanism.hasValue()) {
                ((Frictional) object.entity.getBukkitEntity()).setFrictionState(TriState.NOT_SET);
            }
            else if (mechanism.requireBoolean()) {
                ((Frictional) object.entity.getBukkitEntity()).setFrictionState(TriState.byBoolean(mechanism.getValue().asBoolean()));
            }
        });
    }
}