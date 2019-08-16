package com.denizenscript.denizen.nms.v1_13.impl;

import com.denizenscript.denizen.nms.abstracts.ImprovedOfflinePlayer;
import com.denizenscript.denizen.nms.v1_13.impl.jnbt.CompoundTagImpl;
import com.denizenscript.denizencore.utilities.debugging.Debug;
import net.minecraft.server.v1_13_R2.*;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_13_R2.inventory.CraftInventory;
import org.bukkit.craftbukkit.v1_13_R2.inventory.CraftInventoryPlayer;
import org.bukkit.inventory.Inventory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.UUID;

public class ImprovedOfflinePlayerImpl extends ImprovedOfflinePlayer {

    public ImprovedOfflinePlayerImpl(UUID playeruuid) {
        super(playeruuid);
    }

    @Override
    public org.bukkit.inventory.PlayerInventory getInventory() {
        if (offlineInventories.containsKey(getUniqueId())) {
            return offlineInventories.get(getUniqueId());
        }
        PlayerInventory inventory = new PlayerInventory(null);
        inventory.b(((CompoundTagImpl) this.compound).toNMSTag().getList("Inventory", 10));
        org.bukkit.inventory.PlayerInventory inv = new CraftInventoryPlayer(inventory);
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
        nbtTagCompound.set("EnderItems", ((InventoryEnderChest) ((CraftInventory) inventory).getInventory()).i());
        this.compound = CompoundTagImpl.fromNMSTag(nbtTagCompound);
        if (this.autosave) {
            savePlayerData();
        }
    }

    @Override
    public double getMaxHealth() {
        AttributeInstance maxHealth = getAttributes().a(GenericAttributes.maxHealth);
        return maxHealth == null ? GenericAttributes.maxHealth.getDefault() : maxHealth.getValue();
    }

    @Override
    public void setMaxHealth(double input) {
        AttributeMapBase attributes = getAttributes();
        AttributeInstance maxHealth = attributes.a(GenericAttributes.maxHealth);
        if (maxHealth == null) {
            maxHealth = attributes.b(GenericAttributes.maxHealth);
        }
        maxHealth.setValue(input);
        setAttributes(attributes);
    }

    private AttributeMapBase getAttributes() {
        AttributeMapBase amb = new AttributeMapServer();
        initAttributes(amb);
        GenericAttributes.a(amb, ((CompoundTagImpl) this.compound).toNMSTag().getList("Attributes", 10));
        return amb;
    }

    private void initAttributes(AttributeMapBase amb) {
        // --v from EntityHuman superclass (EntityLiving) v--
        amb.b(GenericAttributes.maxHealth);
        amb.b(GenericAttributes.c);
        //this.getAttributeMap().b(GenericAttributes.MOVEMENT_SPEED); -- merged below to simplify code
        amb.b(GenericAttributes.h);
        amb.b(GenericAttributes.i);
        // --v from EntityHuman v--
        amb.b(GenericAttributes.ATTACK_DAMAGE).setValue(1.0D);
        //this.getAttributeInstance(GenericAttributes.MOVEMENT_SPEED).setValue(0.10000000149011612D); -- merged below
        amb.b(GenericAttributes.MOVEMENT_SPEED).setValue(0.10000000149011612D);
        amb.b(GenericAttributes.g);
        amb.b(GenericAttributes.j);
    }

    public void setAttributes(AttributeMapBase attributes) {
        NBTTagCompound nbtTagCompound = ((CompoundTagImpl) compound).toNMSTag();
        nbtTagCompound.set("Attributes", GenericAttributes.a(attributes));
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
