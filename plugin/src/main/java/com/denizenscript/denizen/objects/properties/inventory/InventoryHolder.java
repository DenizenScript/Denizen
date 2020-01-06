package com.denizenscript.denizen.objects.properties.inventory;

import com.denizenscript.denizen.objects.*;
import com.denizenscript.denizen.utilities.depends.Depends;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.core.ScriptTag;
import com.denizenscript.denizencore.objects.properties.Property;
import com.denizenscript.denizencore.objects.properties.PropertyParser;
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
        return inventory instanceof InventoryTag;
    }

    public static InventoryHolder getFrom(ObjectTag inventory) {
        if (!describes(inventory)) {
            return null;
        }
        return new InventoryHolder((InventoryTag) inventory);
    }

    public static final String[] handledMechs = new String[] {
            "holder"
    };

    InventoryTag inventory;
    ObjectTag holder;

    public InventoryHolder(InventoryTag inventory) {
        this.inventory = inventory;
        this.holder = getHolder();
    }

    public ObjectTag getHolder() {
        if (inventory.getInventory() == null) {
            return null;
        }
        if (inventory.getIdType() != null
                && (inventory.getIdType().equals("player") || inventory.getIdType().equals("enderchest"))) {
            return PlayerTag.valueOf(inventory.getIdHolder());
        }
        else if (inventory.getIdType() != null && inventory.getIdType().equalsIgnoreCase("script")
                && ScriptTag.matches(inventory.getIdHolder())) {
            return ScriptTag.valueOf(inventory.getIdHolder());
        }
        org.bukkit.inventory.InventoryHolder holder = inventory.getInventory().getHolder();

        if (holder != null) {
            if (holder instanceof NPCTag) {
                return (NPCTag) holder;
            }
            else if (holder instanceof Player) {
                if (Depends.citizens != null && CitizensAPI.getNPCRegistry().isNPC((Player) holder)) {
                    return new NPCTag(CitizensAPI.getNPCRegistry().getNPC((Player) holder));
                }
                return new PlayerTag((Player) holder);
            }
            else if (holder instanceof Entity) {
                return new EntityTag((Entity) holder);
            }
            else if (holder instanceof DoubleChest) {
                return new LocationTag(((DoubleChest) holder).getLocation());
            }
            else if (holder instanceof BlockState) {
                return new LocationTag(((BlockState) holder).getLocation());
            }
        }
        else {
            return new ElementTag(inventory.getIdHolder());
        }

        return null;
    }

    public void setHolder(PlayerTag player) {
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

    public void setHolder(NPCTag npc) {
        inventory.setInventory(npc.getInventory());
    }

    public void setHolder(EntityTag entity) {
        inventory.setInventory(entity.getBukkitInventory());
    }

    public void setHolder(LocationTag location) {
        if (!location.isChunkLoadedSafe()) {
            return;
        }
        inventory.setInventory(location.getBukkitInventory());
    }

    public void setHolder(ElementTag element) {
        if (element.matchesEnum(InventoryType.values())) {
            inventory.setInventory(Bukkit.getServer().createInventory(null,
                    InventoryType.valueOf(element.asString().toUpperCase())));
        }
    }

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

    public static void registerTags() {

        // <--[tag]
        // @attribute <InventoryTag.id_holder>
        // @returns ObjectTag
        // @group properties
        // @mechanism InventoryTag.holder
        // @description
        // Returns Denizen's holder ID for this inventory. (player object, location object, etc.)
        // -->
        PropertyParser.<InventoryHolder>registerTag("id_holder", (attribute, object) -> {
            ObjectTag holder = object.holder;
            return holder;
        });
    }

    @Override
    public void adjust(Mechanism mechanism) {

        // <--[mechanism]
        // @object InventoryTag
        // @name holder
        // @input ObjectTag
        // @description
        // Changes the holder of the InventoryTag, therefore completely reconfiguring
        // the inventory to that of the holder.
        // @tags
        // <InventoryTag.id_holder>
        // -->
        if (mechanism.matches("holder")) {
            if (mechanism.getValue().matchesEnum(InventoryType.values())) {
                setHolder(mechanism.getValue());
            }
            else if (mechanism.getValue().matchesType(PlayerTag.class)) {
                setHolder(mechanism.valueAsType(PlayerTag.class));
            }
            else if (Depends.citizens != null && mechanism.getValue().matchesType(NPCTag.class)) {
                setHolder(mechanism.valueAsType(NPCTag.class));
            }
            else if (mechanism.getValue().matchesType(EntityTag.class)) {
                setHolder(mechanism.valueAsType(EntityTag.class));
            }
            else if (mechanism.getValue().matchesType(LocationTag.class)) {
                setHolder(mechanism.valueAsType(LocationTag.class));
            }
        }

    }
}
