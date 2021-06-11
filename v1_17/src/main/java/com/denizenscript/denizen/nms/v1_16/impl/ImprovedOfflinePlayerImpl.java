package com.denizenscript.denizen.nms.v1_16.impl;

import com.denizenscript.denizen.nms.abstracts.ImprovedOfflinePlayer;
import com.denizenscript.denizen.nms.v1_16.impl.jnbt.CompoundTagImpl;
import com.denizenscript.denizencore.utilities.debugging.Debug;
import net.minecraft.server.v1_17_R1.*;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_17_R1.inventory.CraftInventory;
import org.bukkit.craftbukkit.v1_17_R1.inventory.CraftInventoryPlayer;
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

    public class OfflinePlayerInventory extends PlayerInventory {

        public OfflinePlayerInventory(EntityHuman entityhuman) {
            super(entityhuman);
        }

        @Override
        public InventoryHolder getOwner() {
            return null;
        }
    }

    public class OfflineCraftInventoryPlayer extends CraftInventoryPlayer {

        public OfflineCraftInventoryPlayer(PlayerInventory inventory) {
            super(inventory);
        }

        @Override
        public HumanEntity getHolder() {
            return null;
        }
    }

    @Override
    public org.bukkit.inventory.PlayerInventory getInventory() {
        if (offlineInventories.containsKey(getUniqueId())) {
            return offlineInventories.get(getUniqueId());
        }
        PlayerInventory inventory = new OfflinePlayerInventory(null);
        inventory.b(((CompoundTagImpl) this.compound).toNMSTag().getList("Inventory", 10));
        org.bukkit.inventory.PlayerInventory inv = new OfflineCraftInventoryPlayer(inventory);
        offlineInventories.put(getUniqueId(), inv);
        return inv;
    }

    @Override
    public void setInventory(org.bukkit.inventory.PlayerInventory inventory) {
        CraftInventoryPlayer inv = (CraftInventoryPlayer) inventory;
        NBTTagCompound nbtTagCompound = ((CompoundTagImpl) compound).toNMSTag();
        nbtTagCompound.set("Inventory", inv.getInventory().a(new NBTTagList()));
        this.compound = CompoundTagImpl.fromNMSTag(nbtTagCompound);
        if (this.autosave) {
            savePlayerData();
        }
    }

    @Override
    public Inventory getEnderChest() {
        if (offlineEnderChests.containsKey(getUniqueId())) {
            return offlineEnderChests.get(getUniqueId());
        }
        InventoryEnderChest endchest = new InventoryEnderChest(null);
        endchest.a(((CompoundTagImpl) this.compound).toNMSTag().getList("EnderItems", 10));
        org.bukkit.inventory.Inventory inv = new CraftInventory(endchest);
        offlineEnderChests.put(getUniqueId(), inv);
        return inv;
    }

    @Override
    public void setEnderChest(Inventory inventory) {
        NBTTagCompound nbtTagCompound = ((CompoundTagImpl) compound).toNMSTag();
        nbtTagCompound.set("EnderItems", ((InventoryEnderChest) ((CraftInventory) inventory).getInventory()).g());
        this.compound = CompoundTagImpl.fromNMSTag(nbtTagCompound);
        if (this.autosave) {
            savePlayerData();
        }
    }

    @Override
    public double getMaxHealth() {
        AttributeModifiable maxHealth = getAttributes().a(GenericAttributes.MAX_HEALTH);
        return maxHealth == null ? GenericAttributes.MAX_HEALTH.getDefault() : maxHealth.getValue();
    }

    @Override
    public void setMaxHealth(double input) {
        AttributeMapBase attributes = getAttributes();
        AttributeModifiable maxHealth = attributes.a(GenericAttributes.MAX_HEALTH);
        if (maxHealth == null) {
            maxHealth = attributes.a(GenericAttributes.MAX_HEALTH);
        }
        maxHealth.setValue(input);
        setAttributes(attributes);
    }

    private AttributeMapBase getAttributes() {
        AttributeMapBase amb = new AttributeMapBase(AttributeDefaults.a(EntityTypes.PLAYER));
        amb.a(((CompoundTagImpl) this.compound).toNMSTag().getList("Attributes", 10));
        return amb;
    }

    public void setAttributes(AttributeMapBase attributes) {
        NBTTagCompound nbtTagCompound = ((CompoundTagImpl) compound).toNMSTag();
        nbtTagCompound.set("Attributes", attributes.c());
        this.compound = CompoundTagImpl.fromNMSTag(nbtTagCompound);
        if (this.autosave) {
            savePlayerData();
        }
    }

    @Override
    protected boolean loadPlayerData(UUID uuid) {
        try {
            this.player = uuid;
            for (org.bukkit.World w : Bukkit.getWorlds()) {
                this.file = new File(w.getWorldFolder(), "playerdata" + File.separator + this.player + ".dat");
                if (this.file.exists()) {
                    this.compound = CompoundTagImpl.fromNMSTag(NBTCompressedStreamTools.a(new FileInputStream(this.file)));
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
    public void savePlayerData() {
        if (this.exists) {
            try {
                NBTCompressedStreamTools.a(((CompoundTagImpl) this.compound).toNMSTag(), new FileOutputStream(this.file));
            }
            catch (Exception e) {
                Debug.echoError(e);
            }
        }
    }
}
