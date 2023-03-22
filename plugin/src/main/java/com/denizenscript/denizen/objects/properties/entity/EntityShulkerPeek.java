package com.denizenscript.denizen.objects.properties.entity;

import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.properties.Property;
import com.denizenscript.denizencore.tags.Attribute;
import org.bukkit.entity.Shulker;

public class EntityShulkerPeek implements Property {

    public static boolean describes(ObjectTag entity) {
        return entity instanceof EntityTag &&
                ((EntityTag) entity).getBukkitEntity() instanceof Shulker;
    }

    public static EntityShulkerPeek getFrom(ObjectTag entity) {
        if (!describes(entity)) {
            return null;
        }
        else {
            return new EntityShulkerPeek((EntityTag) entity);
        }
    }

    public static final String[] handledTags = new String[] {
            "shulker_peek"
    };

    public static final String[] handledMechs = new String[] {
            "shulker_peek"
    };

    public EntityShulkerPeek(EntityTag ent) {
        entity = ent;
    }

    EntityTag entity;

    @Override
    public String getPropertyString() {
        return String.valueOf(getPeek());
    }

    @Override
    public String getPropertyId() {
        return "shulker_peek";
    }

    public int getPeek() {
        return (int) (((Shulker) entity.getBukkitEntity()).getPeek() * 100);
    }

    @Override
    public ObjectTag getObjectAttribute(Attribute attribute) {

        if (attribute == null) {
            return null;
        }

        // <--[tag]
        // @attribute <EntityTag.shulker_peek>
        // @returns ElementTag(Number)
        // @mechanism EntityTag.shulker_peek
        // @group properties
        // @description
        // Returns the peek value of a shulker box (where 0 is fully closed, 100 is fully open).
        // -->
        if (attribute.startsWith("shulker_peek")) {
            return new ElementTag(getPeek()).getObjectAttribute(attribute.fulfill(1));
        }

        return null;
    }

    @Override
    public void adjust(Mechanism mechanism) {

        // <--[mechanism]
        // @object EntityTag
        // @name shulker_peek
        // @input ElementTag(Number)
        // @description
        // Sets the peek value of a shulker box (where 0 is fully closed, 100 is fully open).
        // @tags
        // <EntityTag.shulker_peek>
        // -->
        if (mechanism.matches("shulker_peek") && mechanism.requireInteger()) {
            ((Shulker) entity.getBukkitEntity()).setPeek(mechanism.getValue().asFloat() / 100);
        }
    }
}
