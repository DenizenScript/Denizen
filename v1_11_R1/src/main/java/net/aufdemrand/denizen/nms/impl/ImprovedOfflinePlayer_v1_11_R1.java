package net.aufdemrand.denizen.nms.impl;

import net.aufdemrand.denizen.nms.abstracts.ImprovedOfflinePlayer;
import net.aufdemrand.denizen.nms.impl.jnbt.CompoundTag_v1_11_R1;
import net.minecraft.server.v1_11_R1.*;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_11_R1.inventory.CraftInventory;
import org.bukkit.craftbukkit.v1_11_R1.inventory.CraftInventoryPlayer;
import org.bukkit.inventory.Inventory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.UUID;

public class ImprovedOfflinePlayer_v1_11_R1 extends ImprovedOfflinePlayer {

    public ImprovedOfflinePlayer_v1_11_R1(UUID playeruuid) {
        super(playeruuid);
    }

    @Override
    public org.bukkit.inventory.PlayerInventory getInventory() {
        if (offlineInventories.containsKey(getUniqueId())) {
            return offlineInventories.get(getUniqueId());
        }
        PlayerInventory inventory = new PlayerInventory(null);
        inventory.b(((CompoundTag_v1_11_R1) this.compound).toNMSTag().getList("Inventory", 10));
        org.bukkit.inventory.PlayerInventory inv = new CraftInventoryPlayer(inventory);
        offlineInventories.put(getUniqueId(), inv);
        return inv;
    }

    @Override
    public void setInventory(org.bukkit.inventory.PlayerInventory inventory) {
        CraftInventoryPlayer inv = (CraftInventoryPlayer) inventory;
        NBTTagCompound nbtTagCompound = ((CompoundTag_v1_11_R1) compound).toNMSTag();
        nbtTagCompound.set("Inventory", inv.getInventory().a(new NBTTagList()));
        this.compound = CompoundTag_v1_11_R1.fromNMSTag(nbtTagCompound);
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
        endchest.a(((CompoundTag_v1_11_R1) this.compound).toNMSTag().getList("EnderItems", 10));
        org.bukkit.inventory.Inventory inv = new CraftInventory(endchest);
        offlineEnderChests.put(getUniqueId(), inv);
        return inv;
    }

    @Override
    public void setEnderChest(Inventory inventory) {
        NBTTagCompound nbtTagCompound = ((CompoundTag_v1_11_R1) compound).toNMSTag();
        nbtTagCompound.set("EnderItems", ((InventoryEnderChest) ((CraftInventory) inventory).getInventory()).i());
        this.compound = CompoundTag_v1_11_R1.fromNMSTag(nbtTagCompound);
        if (this.autosave) {
            savePlayerData();
        }
    }

    @Override
    public double getMaxHealth() {
        return getAttributes().a("generic.maxHealth").getValue();
    }

    @Override
    public void setMaxHealth(double input) {
        AttributeMapBase attributes = getAttributes();
        attributes.a("generic.maxHealth").setValue(input);
        setAttributes(attributes);
    }

    private AttributeMapBase getAttributes() {
        AttributeMapBase amb = new AttributeMapServer();
        GenericAttributes.a(amb, ((CompoundTag_v1_11_R1) this.compound).toNMSTag().getList("Attributes", 0));
        return amb;
    }

    public void setAttributes(AttributeMapBase attributes) {
        NBTTagCompound nbtTagCompound = ((CompoundTag_v1_11_R1) compound).toNMSTag();
        nbtTagCompound.set("Attributes", GenericAttributes.a(attributes));
        this.compound = CompoundTag_v1_11_R1.fromNMSTag(nbtTagCompound);
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
                    this.compound = CompoundTag_v1_11_R1.fromNMSTag(NBTCompressedStreamTools.a(new FileInputStream(this.file)));
                    return true;
                }
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public void savePlayerData() {
        if (this.exists) {
            try {
                NBTCompressedStreamTools.a(((CompoundTag_v1_11_R1) this.compound).toNMSTag(), new FileOutputStream(this.file));
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
