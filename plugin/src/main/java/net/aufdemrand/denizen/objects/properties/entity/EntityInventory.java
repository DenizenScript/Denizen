package net.aufdemrand.denizen.objects.properties.entity;

import net.aufdemrand.denizen.objects.dEntity;
import net.aufdemrand.denizen.objects.dInventory;
import net.aufdemrand.denizen.objects.dItem;
import net.aufdemrand.denizen.objects.properties.inventory.InventoryContents;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.dList;
import com.denizenscript.denizencore.objects.dObject;
import com.denizenscript.denizencore.objects.properties.Property;
import com.denizenscript.denizencore.tags.Attribute;
import org.bukkit.inventory.InventoryHolder;

public class EntityInventory implements Property {

    public static boolean describes(dObject entity) {
        return entity instanceof dEntity
                && ((dEntity) entity).getBukkitEntity() instanceof InventoryHolder;
    }

    public static EntityInventory getFrom(dObject entity) {
        if (!describes(entity)) {
            return null;
        }
        else {
            return new EntityInventory((dEntity) entity);
        }
    }

    public static final String[] handledTags = new String[] {
            "inventory"
    };

    public static final String[] handledMechs = new String[] {
            "inventory_contents"
    };


    ///////////////////
    // Instance Fields and Methods
    /////////////

    private EntityInventory(dEntity ent) {
        entity = ent;
    }

    dEntity entity;

    /////////
    // Property Methods
    ///////

    @Override
    public String getPropertyString() {
        return new InventoryContents(entity.getInventory()).getContents(0).identify();
    }

    @Override
    public String getPropertyId() {
        return "inventory_contents";
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
        // @attribute <e@entity.inventory>
        // @returns dInventory
        // @group inventory
        // @description
        // Returns the entity's inventory, if it has one.
        // -->
        if (attribute.startsWith("inventory")) {
            dInventory inventory = entity.getInventory();
            if (inventory != null) {
                return inventory.getAttribute(attribute.fulfill(1));
            }
            else {
                return null;
            }
        }


        return null;
    }

    @Override
    public void adjust(Mechanism mechanism) {

        // <--[mechanism]
        // @object dEntity
        // @name inventory_contents
        // @input dList(dItem)
        // @description
        // Clears the entity's inventory and sets it's item list to match the input.
        // @tags
        // <e@entity.inventory>
        // <in@inventory.list_contents>
        // -->
        if (mechanism.matches("inventory_contents")) {
            dList list = dList.valueOf(mechanism.getValue().asString());
            dInventory inv = entity.getInventory();
            inv.clear();
            int i = 0;
            for (String str : list) {
                inv.setSlots(i, dItem.valueOf(str, mechanism.context).getItemStack());
                i++;
            }
        }
    }
}
