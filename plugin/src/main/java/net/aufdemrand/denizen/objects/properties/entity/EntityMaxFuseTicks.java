package net.aufdemrand.denizen.objects.properties.entity;

import net.aufdemrand.denizen.objects.dEntity;
import net.aufdemrand.denizencore.objects.Element;
import net.aufdemrand.denizencore.objects.Mechanism;
import net.aufdemrand.denizencore.objects.dObject;
import net.aufdemrand.denizencore.objects.properties.Property;
import net.aufdemrand.denizencore.tags.Attribute;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.EntityType;

public class EntityMaxFuseTicks implements Property {

    public static boolean describes(dObject object) {
        return object instanceof dEntity && ((dEntity) object).getBukkitEntityType() == EntityType.CREEPER;
    }

    public static EntityMaxFuseTicks getFrom(dObject object) {
        if (!describes(object)) {
            return null;
        }
        else {
            return new EntityMaxFuseTicks((dEntity) object);
        }
    }

    public static final String[] handledTags = new String[] {
            "max_fuse_ticks"
    };

    public static final String[] handledMechs = new String[] {
            "max_fuse_ticks"
    };


    ///////////////////
    // Instance Fields and Methods
    /////////////

    private EntityMaxFuseTicks(dEntity entity) {
        this.entity = entity;
    }

    dEntity entity;

    /////////
    // Property Methods
    ///////

    @Override
    public String getPropertyString() {
        return String.valueOf(((Creeper) entity.getBukkitEntity()).getMaxFuseTicks());
    }

    @Override
    public String getPropertyId() {
        return "max_fuse_ticks";
    }

    @Override
    public String getAttribute(Attribute attribute) {

        if (attribute == null) {
            return null;
        }

        // <--[tag]
        // @attribute <e@entity.max_fuse_ticks>
        // @returns Element(Number)
        // @mechanism dEntity.max_fuse_ticks
        // @group properties
        // @description
        // Returns the default number of ticks until the creeper explodes when primed (NOT the time remaining if already primed).
        // -->
        if (attribute.startsWith("max_fuse_ticks")) {
            return new Element(((Creeper) entity.getBukkitEntity()).getMaxFuseTicks())
                    .getAttribute(attribute.fulfill(1));
        }

        return null;
    }

    @Override
    public void adjust(Mechanism mechanism) {

        // <--[mechanism]
        // @object dEntity
        // @name max_fuse_ticks
        // @input Element(Number)
        // @description
        // Sets the default number of ticks until the creeper explodes when primed (NOT the time remaining if already primed).
        // @tags
        // <e@entity.max_fuse_ticks>
        // -->
        if (mechanism.matches("max_fuse_ticks") && mechanism.requireInteger()) {
            ((Creeper) entity.getBukkitEntity()).setMaxFuseTicks(mechanism.getValue().asInt());
        }

    }
}
