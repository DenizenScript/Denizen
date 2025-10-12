package com.denizenscript.denizen.nms.v1_19.impl;

import com.denizenscript.denizen.nms.abstracts.ImprovedOfflinePlayer;
import com.denizenscript.denizen.nms.v1_19.helpers.NBTAdapter;
import com.denizenscript.denizencore.utilities.debugging.Debug;
import net.kyori.adventure.nbt.BinaryTagTypes;
import net.kyori.adventure.nbt.CompoundBinaryTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.DefaultAttributes;
import net.minecraft.world.inventory.PlayerEnderChestContainer;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_19_R3.inventory.CraftInventory;
import org.bukkit.craftbukkit.v1_19_R3.inventory.CraftInventoryPlayer;
import org.bukkit.entity.HumanEntity;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.UUID;

public class ImprovedOfflinePlayerImpl extends ImprovedOfflinePlayer {

    public ImprovedOfflinePlayerImpl(UUID playeruuid) {
        super(playeruuid);
    }

    public static class OfflinePlayerInventory extends net.minecraft.world.entity.player.Inventory {

        public OfflinePlayerInventory(net.minecraft.world.entity.player.Player entityhuman) {
            super(entityhuman);
        }

        @Override
        public InventoryHolder getOwner() {
            return null;
        }
    }

    public static class OfflineCraftInventoryPlayer extends CraftInventoryPlayer {

        public OfflineCraftInventoryPlayer(net.minecraft.world.entity.player.Inventory inventory) {
            super(inventory);
        }

        @Override
        public HumanEntity getHolder() {
            return null;
        }
    }

    @Override
    public org.bukkit.inventory.PlayerInventory getInventory() {
        if (inventory == null) {
            net.minecraft.world.entity.player.Inventory newInv = new OfflinePlayerInventory(null);
            newInv.load(NBTAdapter.toNMS(this.compound.getList("Inventory", BinaryTagTypes.COMPOUND)));
            inventory = new OfflineCraftInventoryPlayer(newInv);
        }
        return inventory;
    }

    @Override
    public void setInventory(org.bukkit.inventory.PlayerInventory inventory) {
        CraftInventoryPlayer inv = (CraftInventoryPlayer) inventory;
        this.compound = compound.put("Inventory", NBTAdapter.toAPI(inv.getInventory().save(new ListTag())));
        markModified();
    }

    @Override
    public Inventory getEnderChest() {
        if (enderchest == null) {
            PlayerEnderChestContainer endchest = new PlayerEnderChestContainer(null);
            endchest.fromTag(NBTAdapter.toNMS(this.compound.getList("EnderItems", BinaryTagTypes.COMPOUND)));
            enderchest = new CraftInventory(endchest);
        }
        return enderchest;
    }

    @Override
    public void setEnderChest(Inventory inventory) {
        this.compound = compound.put("EnderItems", NBTAdapter.toAPI(((PlayerEnderChestContainer) ((CraftInventory) inventory).getInventory()).createTag()));
        markModified();
    }

    @Override
    public double getMaxHealth() {
        AttributeInstance maxHealth = getAttributes().getInstance(Attributes.MAX_HEALTH);
        return maxHealth == null ? Attributes.MAX_HEALTH.getDefaultValue() : maxHealth.getValue();
    }

    @Override
    public void setMaxHealth(double input) {
        AttributeMap attributes = getAttributes();
        AttributeInstance maxHealth = attributes.getInstance(Attributes.MAX_HEALTH);
        maxHealth.setBaseValue(input);
        setAttributes(attributes);
    }

    private AttributeMap getAttributes() {
        AttributeMap amb = new AttributeMap(DefaultAttributes.getSupplier(net.minecraft.world.entity.EntityType.PLAYER));
        amb.load(NBTAdapter.toNMS(this.compound.getList("Attributes", BinaryTagTypes.COMPOUND)));
        return amb;
    }

    public void setAttributes(AttributeMap attributes) {
        this.compound = compound.put("Attributes", NBTAdapter.toAPI(attributes.save()));
        markModified();
    }

    @Override
    protected boolean loadPlayerData(UUID uuid) {
        try {
            this.player = uuid;
            for (org.bukkit.World w : Bukkit.getWorlds()) {
                this.file = new File(w.getWorldFolder(), "playerdata" + File.separator + this.player + ".dat");
                if (this.file.exists()) {
                    this.compound = NBTAdapter.toAPI(NbtIo.readCompressed(new FileInputStream(this.file)));
                    return true;
                }
            }
        }
        catch (Exception e) {
            Debug.echoError(e);
        }
        return false;
    }

    @Override
    public void saveInternal(CompoundBinaryTag compound) {
        try {
            NbtIo.writeCompressed(NBTAdapter.toNMS(compound), new FileOutputStream(this.file));
        }
        catch (Exception e) {
            Debug.echoError(e);
        }
    }
}
