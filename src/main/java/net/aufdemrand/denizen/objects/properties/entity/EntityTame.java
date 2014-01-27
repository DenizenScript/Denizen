package net.aufdemrand.denizen.objects.properties.entity;


import net.aufdemrand.denizen.objects.*;
import net.aufdemrand.denizen.objects.properties.Property;
import net.aufdemrand.denizen.tags.Attribute;
import org.bukkit.entity.*;

public class EntityTame implements Property {


    public static boolean describes(dObject entity) {
        return entity instanceof dEntity &&
                ((dEntity) entity).getBukkitEntity() instanceof Tameable;
    }

    public static EntityTame getFrom(dObject entity) {
        if (!describes(entity)) return null;

        else return new EntityTame((dEntity) entity);
    }


    ///////////////////
    // Instance Fields and Methods
    /////////////

    private EntityTame(dEntity tame) {
        entity = tame;
    }

    dEntity entity;

    /////////
    // Property Methods
    ///////

    @Override
    public String getPropertyString() {
        return ((Tameable) entity.getBukkitEntity()).isTamed() ? "true": null;
    }

    @Override
    public String getPropertyId() {
        return "tame";
    }


    ///////////
    // dObject Attributes
    ////////

    @Override
    public String getAttribute(Attribute attribute) {

        if (attribute == null) return "null";

        // <--[tag]
        // @attribute <e@entity.is_tamed>
        // @returns Element(Boolean)
        // @description
        // Returns whether the entity has been tamed.
        // -->
        if (attribute.startsWith("is_tamed")) {
            if (entity instanceof Tameable)
                return new Element(((Tameable) entity.getBukkitEntity()).isTamed())
                        .getAttribute(attribute.fulfill(1));
            else
                return Element.FALSE
                        .getAttribute(attribute.fulfill(1));
        }

        // <--[tag]
        // @attribute <e@entity.get_owner>
        // @returns dPlayer
        // @description
        // Returns the owner of a tamed entity.
        // -->
        if (attribute.startsWith("get_owner")) {
            if (((Tameable) entity.getBukkitEntity()).isTamed())
                return new dPlayer((Player) ((Tameable) entity.getBukkitEntity()).getOwner())
                        .getAttribute(attribute.fulfill(1));
            else
                return Element.NULL
                        .getAttribute(attribute.fulfill(1));
        }

        return null;
    }

    @Override
    public void adjust(Mechanism mechanism) {

        // <--[mechanism]
        // @object dEntity
        // @name tame
        // @input Element(Boolean)
        // @description
        // Sets whether the entity has been tamed.
        // @tags
        // <e@entity.is_tamed>
        // <e@entity.is_tameable>
        // -->

        if (mechanism.matches("tame") && mechanism.requireBoolean()) {
            ((Tameable)entity.getBukkitEntity()).setTamed(mechanism.getValue().asBoolean());
        }

    }

}

