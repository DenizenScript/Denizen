package net.aufdemrand.denizen.objects.properties.entity;

import net.aufdemrand.denizen.nms.NMSHandler;
import net.aufdemrand.denizen.objects.dEntity;
import net.aufdemrand.denizen.objects.dItem;
import net.aufdemrand.denizencore.objects.Mechanism;
import net.aufdemrand.denizencore.objects.dObject;
import net.aufdemrand.denizencore.objects.properties.Property;
import net.aufdemrand.denizencore.tags.Attribute;
import org.bukkit.Material;
import org.bukkit.entity.Enderman;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Item;

public class EntityItem implements Property {

    public static boolean describes(dObject entity) {
        return entity instanceof dEntity &&
                (((dEntity) entity).getBukkitEntityType() == EntityType.DROPPED_ITEM
                        || ((dEntity) entity).getBukkitEntityType() == EntityType.ENDERMAN);
    }

    public static EntityItem getFrom(dObject entity) {
        if (!describes(entity)) {
            return null;
        }
        else {
            return new EntityItem((dEntity) entity);
        }
    }

    public static final String[] handledTags = new String[] {
            "item"
    };

    public static final String[] handledMechs = new String[] {
            "item"
    };


    ///////////////////
    // Instance Fields and Methods
    /////////////

    private EntityItem(dEntity entity) {
        item = entity;
    }

    dEntity item;

    public dItem getItem() {
        if (item.getBukkitEntity() instanceof Item) {
            return new dItem(((Item) item.getBukkitEntity()).getItemStack());
        }
        else {
            return new dItem(((Enderman) item.getBukkitEntity())
                    .getCarriedMaterial());
        }
    }

    /////////
    // Property Methods
    ///////

    @Override
    public String getPropertyString() {
        dItem item = getItem();
        if (item.getItemStack().getType() != Material.AIR) {
            return item.identify();
        }
        else {
            return null;
        }
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

        if (attribute == null) {
            return null;
        }

        // <--[tag]
        // @attribute <e@entity.item>
        // @returns dItem
        // @mechanism dEntity.item
        // @group properties
        // @description
        // If the entity is a dropped item or an Enderman, returns the dItem the entity holds.
        // -->
        if (attribute.startsWith("item")) {
            return getItem().getAttribute(attribute.fulfill(1));
        }

        return null;
    }

    @Override
    public void adjust(Mechanism mechanism) {

        // <--[mechanism]
        // @object dEntity
        // @name item
        // @input dItem
        // @description
        // Changes what item a dropped or an Enderman item holds.
        // @tags
        // <e@entity.item>
        // -->

        if (mechanism.matches("item") && mechanism.requireObject(dItem.class)) {
            if (item.getBukkitEntity() instanceof Item) {
                ((Item) item.getBukkitEntity()).setItemStack(mechanism.getValue()
                        .asType(dItem.class, mechanism.context).getItemStack());
            }
            else {
                NMSHandler.getInstance().getEntityHelper().setCarriedItem((Enderman) item.getBukkitEntity(),
                        mechanism.valueAsType(dItem.class).getItemStack());
            }
        }
    }
}
