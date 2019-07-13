package com.denizenscript.denizen.objects.properties.inventory;

import com.denizenscript.denizen.objects.*;
import com.denizenscript.denizen.utilities.depends.Depends;
import com.denizenscript.denizencore.objects.ElementTag;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.ScriptTag;
import com.denizenscript.denizencore.objects.properties.Property;
import com.denizenscript.denizencore.tags.Attribute;
import net.citizensnpcs.api.CitizensAPI;
import org.bukkit.Bukkit;
import org.bukkit.block.BlockState;
import org.bukkit.block.DoubleChest;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.CraftingInventory;
import org.bukkit.inventory.Inventory;

public class InventoryHolder implements Property {

    public static boolean describes(ObjectTag inventory) {
        // All inventories should have a holder
        return inventory instanceof dInventory;
    }

    public static InventoryHolder getFrom(ObjectTag inventory) {
        if (!describes(inventory)) {
            return null;
        }
        return new InventoryHolder((dInventory) inventory);
    }

    public static final String[] handledTags = new String[] {
            "id_holder"
    };

    public static final String[] handledMechs = new String[] {
            "holder"
    };

    ///////////////////
    // Instance Fields and Methods
    /////////////

    dInventory inventory;
    ObjectTag holder;

    public InventoryHolder(dInventory inventory) {
        this.inventory = inventory;
        this.holder = getHolder();
    }

    public ObjectTag getHolder() {
        if (inventory.getInventory() == null) {
            return null;
        }
        if (inventory.getIdType() != null
                && (inventory.getIdType().equals("player") || inventory.getIdType().equals("enderchest"))) {
            return dPlayer.valueOf(inventory.getIdHolder());
        }
        else if (inventory.getIdType() != null && inventory.getIdType().equalsIgnoreCase("script")
                && ScriptTag.matches(inventory.getIdHolder())) {
            return ScriptTag.valueOf(inventory.getIdHolder());
        }
        org.bukkit.inventory.InventoryHolder holder = inventory.getInventory().getHolder();

        if (holder != null) {
            if (holder instanceof dNPC) {
                return (dNPC) holder;
            }
            else if (holder instanceof Player) {
                if (Depends.citizens != null && CitizensAPI.getNPCRegistry().isNPC((Player) holder)) {
                    return new dNPC(CitizensAPI.getNPCRegistry().getNPC((Player) holder));
                }
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
        else {
            return new ElementTag(inventory.getIdHolder());
        }

        return null;
    }

    public void setHolder(dPlayer player) {
        if (inventory.getIdType().equals("enderchest")) {
            inventory.setInventory(player.getBukkitEnderChest(), player);
        }
        else if (inventory.getIdType().equals("workbench")) {
            inventory.setInventory(player.getBukkitWorkbench(), player);
        }
        else if (inventory.getIdType().equals("crafting")) {
            Inventory opened = player.getPlayerEntity().getOpenInventory().getTopInventory();
            if (opened instanceof CraftingInventory) {
                inventory.setInventory(opened, player);
            }
            else {
                inventory.setIdType("player");
                inventory.setInventory(player.getBukkitInventory(), player);
            }
        }
        else {
            inventory.setInventory(player.getBukkitInventory(), player);
        }
    }

    public void setHolder(dNPC npc) {
        inventory.setInventory(npc.getInventory());
    }

    public void setHolder(dEntity entity) {
        inventory.setInventory(entity.getBukkitInventory());
    }

    public void setHolder(dLocation location) {
        inventory.setInventory(location.getBukkitInventory());
    }

    public void setHolder(ElementTag element) {
        if (element.matchesEnum(InventoryType.values())) {
            inventory.setInventory(Bukkit.getServer().createInventory(null,
                    InventoryType.valueOf(element.asString().toUpperCase())));
        }
    }


    /////////
    // Property Methods
    ///////

    @Override
    public String getPropertyString() {
        if (holder == null || (inventory.getIdType().equals("generic")
                && inventory.getIdHolder().equals("CHEST"))) {
            return null;
        }
        else {
            return holder.identify();
        }
    }

    @Override
    public String getPropertyId() {
        return "holder";
    }


    ///////////
    // ObjectTag Attributes
    ////////

    @Override
    public String getAttribute(Attribute attribute) {

        if (attribute == null) {
            return null;
        }

        // <--[tag]
        // @attribute <in@inventory.id_holder>
        // @returns ObjectTag
        // @group properties
        // @mechanism dInventory.holder
        // @description
        // Returns Denizen's holder ID for this inventory. (p@aufdemrand, l@123,321,123, etc.)
        // -->
        if (attribute.startsWith("id_holder")) {
            if (holder == null) {
                return null;
            }
            return holder.getAttribute(attribute.fulfill(1));
        }

        return null;

    }

    @Override
    public void adjust(Mechanism mechanism) {

        // <--[mechanism]
        // @object dInventory
        // @name holder
        // @input ObjectTag
        // @description
        // Changes the holder of the dInventory, therefore completely reconfiguring
        // the inventory to that of the holder.
        // @tags
        // <in@inventory.id_holder>
        // -->
        if (mechanism.matches("holder")) {
            if (mechanism.getValue().matchesEnum(InventoryType.values())) {
                setHolder(mechanism.getValue());
            }
            else if (mechanism.getValue().matchesType(dPlayer.class)) {
                setHolder(mechanism.valueAsType(dPlayer.class));
            }
            else if (Depends.citizens != null && mechanism.getValue().matchesType(dNPC.class)) {
                setHolder(mechanism.valueAsType(dNPC.class));
            }
            else if (mechanism.getValue().matchesType(dEntity.class)) {
                setHolder(mechanism.valueAsType(dEntity.class));
            }
            else if (mechanism.getValue().matchesType(dLocation.class)) {
                setHolder(mechanism.valueAsType(dLocation.class));
            }
        }

    }
}
