package net.aufdemrand.denizen.objects.properties.entity;

import net.aufdemrand.denizen.objects.*;
import net.aufdemrand.denizen.objects.properties.Property;
import net.aufdemrand.denizen.tags.Attribute;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Item;

public class EntityItem implements Property {

    public static boolean describes(dObject entity) {
        return entity instanceof dEntity && ((dEntity)entity).getEntityType() == EntityType.DROPPED_ITEM;
    }

    public static EntityItem getFrom(dObject entity) {
        if (!describes(entity)) return null;

        else return new EntityItem((dEntity) entity);
    }

    ///////////////////
    // Instance Fields and Methods
    /////////////

    private EntityItem(dEntity entity) {
        item = entity;
    }

    dEntity item;

    /////////
    // Property Methods
    ///////

    @Override
    public String getPropertyString() {
        return new dItem(((Item)item.getBukkitEntity()).getItemStack()).identify();
    }

    @Override
    public String getPropertyId() {
        return "item";
    }

    ///////////
    // dObject Attributes
    ////////

    @Override
    public String getAttribute(Attribute attribute) {

        if (attribute == null) return "null";

        // <--[tag]
        // @attribute <e@entity.item>
        // @returns dItem
        // @mechanism dEntity.item
        // @group properties
        // @description
        // If the entity is a dropped item, returns the dItem the entity holds.
        // -->
        if (attribute.startsWith("item"))
            return new dItem(((Item)item.getBukkitEntity()).getItemStack())
                    .getAttribute(attribute.fulfill(1));

        return null;
    }

    @Override
    public void adjust(Mechanism mechanism) {

        // <--[mechanism]
        // @object dEntity
        // @name item
        // @input dItem
        // @description
        // Changes what item a dropped item holds.
        // @tags
        // <e@entity.item>
        // -->

        if (mechanism.matches("item") && mechanism.requireObject(dItem.class)) {
            ((Item)item.getBukkitEntity()).setItemStack(mechanism.getValue().asType(dItem.class).getItemStack());
        }
    }
}
