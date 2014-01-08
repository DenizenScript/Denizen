package net.aufdemrand.denizen.objects.properties.inventory;

import org.bukkit.block.BlockState;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import net.aufdemrand.denizen.objects.Element;
import net.aufdemrand.denizen.objects.dEntity;
import net.aufdemrand.denizen.objects.dInventory;
import net.aufdemrand.denizen.objects.dLocation;
import net.aufdemrand.denizen.objects.dNPC;
import net.aufdemrand.denizen.objects.dObject;
import net.aufdemrand.denizen.objects.dPlayer;
import net.aufdemrand.denizen.objects.properties.Property;
import net.aufdemrand.denizen.tags.Attribute;
import net.citizensnpcs.api.CitizensAPI;

public class InventoryHolder implements Property {

    public static boolean describes(dObject inventory) {
        // All inventories should have a holder
        return inventory instanceof dInventory;
    }

    public static InventoryHolder getFrom(dObject inventory) {
        if (!describes(inventory)) return null;
        return new InventoryHolder((dInventory) inventory);
    }


    ///////////////////
    // Instance Fields and Methods
    /////////////

    dInventory inventory;
    dObject holder;

    public InventoryHolder(dInventory inventory) {
        this.inventory = inventory;
        this.holder = getHolder();
    }

    public dObject getHolder() {
        org.bukkit.inventory.InventoryHolder holder =
                inventory.getInventory().getHolder();
        
        if (holder != null) {
            if (holder instanceof Entity && CitizensAPI.getNPCRegistry().isNPC((Entity) holder)) {
                return new dNPC(CitizensAPI.getNPCRegistry().getNPC((Entity) holder));
            }
            else if (holder instanceof Player) {
                return new dPlayer((Player) holder);
            }
            else if (holder instanceof Entity) {
                return new dEntity((Entity) holder);
            }
            else if (holder instanceof BlockState) {
                return new dLocation(((BlockState) holder).getLocation());
            }
        }
        else {
            return new Element(inventory.getInventory().getType().name());
        }

        return null;
    }


    /////////
    // Property Methods
    ///////

    @Override
    public String getPropertyString() {
        return holder.identify();
    }

    @Override
    public String getPropertyId() {
        return "holder";
    }


    ///////////
    // dObject Attributes
    ////////

    @Override
    public String getAttribute(Attribute attribute) {

        if (attribute == null) return "null";

        // <--[tag]
        // @attribute <in@inventory.id_holder>
        // @returns Element
        // @description
        // Returns Denizen's holder ID for this inventory. (p@aufdemrand, l@123,321,123, etc.)
        // -->
        if (attribute.startsWith("id_holder")) {
            if (holder == null)
                return null;
            return holder.getAttribute(attribute.fulfill(1));
        }

        return null;

    }

}
