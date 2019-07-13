package com.denizenscript.denizen.nms.impl;

import com.denizenscript.denizen.nms.abstracts.ImprovedOfflinePlayer;
import com.denizenscript.denizen.nms.impl.jnbt.CompoundTag_v1_14_R1;
import com.denizenscript.denizencore.utilities.debugging.dB;
import net.minecraft.server.v1_14_R1.*;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_14_R1.inventory.CraftInventory;
import org.bukkit.craftbukkit.v1_14_R1.inventory.CraftInventoryPlayer;
import org.bukkit.inventory.Inventory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.UUID;

public class ImprovedOfflinePlayer_v1_14_R1 extends ImprovedOfflinePlayer {

    public ImprovedOfflinePlayer_v1_14_R1(UUID playeruuid) {
        super(playeruuid);
    }

    @Override
    public org.bukkit.inventory.PlayerInventory getInventory() {
        if (offlineInventories.containsKey(getUniqueId())) {
            return offlineInventories.get(getUniqueId());
        }
        PlayerInventory inventory = new PlayerInventory(null);
        inventory.b(((CompoundTag_v1_14_R1) this.compound).toNMSTag().getList("Inventory", 10));
        org.bukkit.inventory.PlayerInventory inv = new CraftInventoryPlayer(inventory);
        offlineInventories.put(getUniqueId(), inv);
        return inv;
    }

    @Override
    public void setInventory(org.bukkit.inventory.PlayerInventory inventory) {
        CraftInventoryPlayer inv = (CraftInventoryPlayer) inventory;
        NBTTagCompound nbtTagCompound = ((CompoundTag_v1_14_R1) compound).toNMSTag();
        nbtTagCompound.set("Inventory", inv.getInventory().a(new NBTTagList()));
        this.compound = CompoundTag_v1_14_R1.fromNMSTag(nbtTagCompound);
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
        endchest.a(((CompoundTag_v1_14_R1) this.compound).toNMSTag().getList("EnderItems", 10));
        org.bukkit.inventory.Inventory inv = new CraftInventory(endchest);
        offlineEnderChests.put(getUniqueId(), inv);
        return inv;
    }

    @Override
    public void setEnderChest(Inventory inventory) {
        NBTTagCompound nbtTagCompound = ((CompoundTag_v1_14_R1) compound).toNMSTag();
        nbtTagCompound.set("EnderItems", ((InventoryEnderChest) ((CraftInventory) inventory).getInventory()).f());
        this.compound = CompoundTag_v1_14_R1.fromNMSTag(nbtTagCompound);
        if (this.autosave) {
            savePlayerData();
        }
    }

    @Override
    public double getMaxHealth() {
        AttributeInstance maxHealth = getAttributes().a(GenericAttributes.MAX_HEALTH);
        return maxHealth == null ? GenericAttributes.MAX_HEALTH.getDefault() : maxHealth.getValue();
    }

    @Override
    public void setMaxHealth(double input) {
        AttributeMapBase attributes = getAttributes();
        AttributeInstance maxHealth = attributes.a(GenericAttributes.MAX_HEALTH);
        if (maxHealth == null) {
            maxHealth = attributes.b(GenericAttributes.MAX_HEALTH);
        }
        maxHealth.setValue(input);
        setAttributes(attributes);
    }

    private AttributeMapBase getAttributes() {
        AttributeMapBase amb = new AttributeMapServer();
        initAttributes(amb);
        GenericAttributes.a(amb, ((CompoundTag_v1_14_R1) this.compound).toNMSTag().getList("Attributes", 10));
        return amb;
    }

    private void initAttributes(AttributeMapBase amb) {
        // --v from EntityHuman superclass (EntityLiving) v--
        amb.b(GenericAttributes.MAX_HEALTH);
        amb.b(GenericAttributes.KNOCKBACK_RESISTANCE);
        //this.getAttributeMap().b(GenericAttributes.MOVEMENT_SPEED); -- merged below to simplify code
        amb.b(GenericAttributes.ARMOR);
        amb.b(GenericAttributes.ARMOR_TOUGHNESS);
        // --v from EntityHuman v--
        amb.b(GenericAttributes.ATTACK_DAMAGE).setValue(1.0D);
        //this.getAttributeInstance(GenericAttributes.MOVEMENT_SPEED).setValue(0.10000000149011612D); -- merged below
        amb.b(GenericAttributes.MOVEMENT_SPEED).setValue(0.10000000149011612D);
        amb.b(GenericAttributes.ATTACK_SPEED);
        amb.b(GenericAttributes.LUCK);
    }

    public void setAttributes(AttributeMapBase attributes) {
        NBTTagCompound nbtTagCompound = ((CompoundTag_v1_14_R1) compound).toNMSTag();
        nbtTagCompound.set("Attributes", GenericAttributes.a(attributes));
        this.compound = CompoundTag_v1_14_R1.fromNMSTag(nbtTagCompound);
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
                    this.compound = CompoundTag_v1_14_R1.fromNMSTag(NBTCompressedStreamTools.a(new FileInputStream(this.file)));
                    return true;
                }
            }
        }
        catch (Exception e) {
            dB.echoError(e);
        }
        return false;
    }

    @Override
    public void savePlayerData() {
        if (this.exists) {
            try {
                NBTCompressedStreamTools.a(((CompoundTag_v1_14_R1) this.compound).toNMSTag(), new FileOutputStream(this.file));
            }
            catch (Exception e) {
                dB.echoError(e);
            }
        }
    }
}
