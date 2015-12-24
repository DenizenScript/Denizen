package net.aufdemrand.denizen.objects.properties.entity;

import net.aufdemrand.denizen.objects.dEntity;
import net.aufdemrand.denizencore.objects.Element;
import net.aufdemrand.denizencore.objects.Mechanism;
import net.aufdemrand.denizencore.objects.dObject;
import net.aufdemrand.denizencore.objects.properties.Property;
import net.aufdemrand.denizencore.tags.Attribute;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Horse;

public class EntityChestCarrier implements Property {

    public static boolean describes(dObject entity) {
        return entity instanceof dEntity
                && ((dEntity) entity).getBukkitEntityType() == EntityType.HORSE;
    }

    public static EntityChestCarrier getFrom(dObject entity) {
        if (!describes(entity)) return null;

        else return new EntityChestCarrier((dEntity) entity);
    }

    ///////////////////
    // Instance Fields and Methods
    /////////////

    private EntityChestCarrier(dEntity entity) {
        this.entity = entity;
    }

    dEntity entity;

    /////////
    // Property Methods
    ///////

    @Override
    public String getPropertyString() {
        return ((Horse) entity.getBukkitEntity()).isCarryingChest() ? "true" : "false";
    }

    @Override
    public String getPropertyId() {
        return "carries_chest";
    }

    ///////////
    // dObject Attributes
    ////////

    @Override
    public String getAttribute(Attribute attribute) {

        if (attribute == null) {
            return null;
        }

        // <--[tag]
        // @attribute <e@entity.carries_chest>
        // @returns Element(Boolean)
        // @mechanism dEntity.carries_chest
        // @group properties
        // @description
        // If the entity is a horse, returns whether it is carrying a chest.
        // -->
        if (attribute.startsWith("carries_chest"))
            return new Element(((Horse) entity.getBukkitEntity()).isCarryingChest())
                    .getAttribute(attribute.fulfill(1));

        return null;
    }

    @Override
    public void adjust(Mechanism mechanism) {

        // <--[mechanism]
        // @object dEntity
        // @name carries_chest
        // @input Element(Boolean)
        // @description
        // Changes whether a Horse carries a chest.
        // @tags
        // <e@entity.carries_chest>
        // -->

        if (mechanism.matches("carries_chest") && mechanism.requireBoolean()) {
            ((Horse) entity.getBukkitEntity()).setCarryingChest(mechanism.getValue().asBoolean());
        }
    }
}
