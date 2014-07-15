package net.aufdemrand.denizen.objects.properties.inventory;

import net.aufdemrand.denizen.objects.*;
import net.aufdemrand.denizen.utilities.depends.Depends;
import org.bukkit.Bukkit;
import org.bukkit.block.BlockState;
import org.bukkit.block.DoubleChest;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import net.aufdemrand.denizen.objects.properties.Property;
import net.aufdemrand.denizen.tags.Attribute;
import net.citizensnpcs.api.CitizensAPI;
import org.bukkit.event.inventory.InventoryType;

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
        if (inventory.getInventory() == null)
            return null;
        if (inventory.getIdType() != null
                && (inventory.getIdType().equals("player") || inventory.getIdType().equals("enderchest")))
            return dPlayer.valueOf(inventory.getIdHolder());
        org.bukkit.inventory.InventoryHolder holder = inventory.getInventory().getHolder();

        if (holder != null) {
            if (holder instanceof Entity && Depends.citizens != null && CitizensAPI.getNPCRegistry().isNPC((Entity) holder)) {
                return dNPC.fromEntity((Entity) holder);
            }
            else if (holder instanceof Player) {
                return new dPlayer((Player) holder);
            }
            else if (holder instanceof Entity) {
                return new dEntity((Entity) holder);
            }
            else if (holder instanceof DoubleChest) {
                return new dLocation(((DoubleChest) holder).getLocation());
            }
            else if (holder instanceof BlockState) {
                return new dLocation(((BlockState) holder).getLocation());
            }
        }
        else
            return new Element(inventory.getIdHolder());

        return null;
    }

    public void setHolder(dPlayer player) {
        if (inventory.getIdType().equals("enderchest"))
            inventory.setInventory(player.getBukkitEnderChest(), player);
        else
            inventory.setInventory(player.getBukkitInventory(), player);
    }

    public void setHolder(dEntity entity) {
        inventory.setInventory(entity.getBukkitInventory());
    }

    public void setHolder(dLocation location) {
        inventory.setInventory(location.getBukkitInventory());
    }

    public void setHolder(Element element) {
        if (element.matchesEnum(InventoryType.values()))
            inventory.setInventory(Bukkit.getServer().createInventory(null,
                    InventoryType.valueOf(element.asString().toUpperCase())));
    }


    /////////
    // Property Methods
    ///////

    @Override
    public String getPropertyString() {
        if (holder == null || (inventory.getIdType().equals("generic")
                && inventory.getIdHolder().equals("CHEST")))
            return null;
        else
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
        // @returns dObject
        // @group properties
        // @mechanism dInventory.holder
        // @description
        // Returns Denizen's holder ID for this inventory. (p@aufdemrand, l@123,321,123, etc.)
        // -->
        if (attribute.startsWith("id_holder")) {
            if (holder == null)
                return Element.NULL.getAttribute(attribute.fulfill(1));
            return holder.getAttribute(attribute.fulfill(1));
        }

        return null;

    }

    @Override
    public void adjust(Mechanism mechanism) {

        // <--[mechanism]
        // @object dInventory
        // @name holder
        // @input dObject
        // @description
        // Changes the holder of the dInventory, therefore completely reconfiguring
        // the inventory to that of the holder.
        // @tags
        // <in@inventory.id_holder>
        // -->
        if (mechanism.matches("holder")) {
            Element value = mechanism.getValue();
            if (value.matchesType(dPlayer.class)) setHolder(value.asType(dPlayer.class));
            else if (value.matchesType(dEntity.class)) setHolder(value.asType(dEntity.class));
            else if (value.matchesType(dLocation.class)) setHolder(value.asType(dLocation.class));
            else if (value.matchesEnum(InventoryType.values())) setHolder(value);
        }

    }

}
