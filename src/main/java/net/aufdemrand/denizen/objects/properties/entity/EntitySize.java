package net.aufdemrand.denizen.objects.properties.entity;


import net.aufdemrand.denizen.objects.*;
import net.aufdemrand.denizen.objects.properties.Property;
import net.aufdemrand.denizen.tags.Attribute;
import org.bukkit.entity.Slime;

public class EntitySize implements Property {


    public static boolean describes(dObject entity) {
        return entity instanceof dEntity &&
                ((dEntity) entity).getBukkitEntity() instanceof Slime;
    }

    public static EntitySize getFrom(dObject entity) {
        if (!describes(entity)) return null;

        else return new EntitySize((dEntity) entity);
    }


    ///////////////////
    // Instance Fields and Methods
    /////////////

    private EntitySize(dEntity tame) {
        entity = tame;
    }

    dEntity entity;

    /////////
    // Property Methods
    ///////

    @Override
    public String getPropertyString() {
        return String.valueOf(((Slime) entity.getBukkitEntity()).getSize());
    }

    @Override
    public String getPropertyId() {
        return "size";
    }


    ///////////
    // dObject Attributes
    ////////

    @Override
    public String getAttribute(Attribute attribute) {

        if (attribute == null) return "null";

        // <--[tag]
        // @attribute <e@entity.size>
        // @returns Element(Number)
        // @description
        // Returns the size of a slime-type entity (1-120).
        // To edit this, use <@link mechanism dEntity.size>
        // -->
        if (attribute.startsWith("size")) {
            return new Element(((Slime) entity.getBukkitEntity()).getSize())
                    .getAttribute(attribute.fulfill(1));
        }

        return null;
    }

    @Override
    public void adjust(Mechanism mechanism) {

        // <--[mechanism]
        // @object dEntity
        // @name size
        // @input Element(Number)
        // @description
        // Sets the size of a slime-type entity (1-120).
        // @tags
        // <e@entity.size>
        // <e@entity.is_sizeable>
        // -->

        if (mechanism.matches("size") && mechanism.requireInteger()) {
            ((Slime) entity.getBukkitEntity()).setSize(mechanism.getValue().asInt());
        }
    }
}

