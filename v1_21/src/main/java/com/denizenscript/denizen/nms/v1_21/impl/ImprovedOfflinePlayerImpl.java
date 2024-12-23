package com.denizenscript.denizen.nms.v1_21.impl;

import com.denizenscript.denizen.nms.abstracts.ImprovedOfflinePlayer;
import com.denizenscript.denizen.nms.util.jnbt.CompoundTag;
import com.denizenscript.denizen.nms.v1_21.impl.jnbt.CompoundTagImpl;
import com.denizenscript.denizencore.utilities.debugging.Debug;
import com.mojang.authlib.GameProfile;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ClientInformation;
import net.minecraft.server.level.ParticleStatus;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.DefaultAttributes;
import net.minecraft.world.entity.player.ChatVisiblity;
import net.minecraft.world.inventory.PlayerEnderChestContainer;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_21_R3.CraftRegistry;
import org.bukkit.craftbukkit.v1_21_R3.CraftServer;
import org.bukkit.craftbukkit.v1_21_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_21_R3.inventory.CraftInventory;
import org.bukkit.craftbukkit.v1_21_R3.inventory.CraftInventoryPlayer;
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

    public static ServerPlayer fakeNmsPlayer;

    public static ServerPlayer getFakeNmsPlayer() {
        if (fakeNmsPlayer == null) {
            MinecraftServer server = ((CraftServer)Bukkit.getServer()).getServer();
            World world = Bukkit.getWorlds().getFirst();
            GameProfile fakeProfile = new GameProfile(new UUID(0, 0xABC123), "fakeplayer");
            ClientInformation fakeClientInfo = new ClientInformation("en", 0, ChatVisiblity.HIDDEN, false, 0, HumanoidArm.LEFT, true, false, ParticleStatus.MINIMAL);
            fakeNmsPlayer = new ServerPlayer(server, ((CraftWorld) world).getHandle(), fakeProfile, fakeClientInfo);
        }
        return fakeNmsPlayer;
    }

    @Override
    public org.bukkit.inventory.PlayerInventory getInventory() {
        if (inventory == null) {
            net.minecraft.world.entity.player.Inventory newInv = new OfflinePlayerInventory(getFakeNmsPlayer());
            newInv.load(((CompoundTagImpl) this.compound).toNMSTag().getList("Inventory", 10));
            inventory = new OfflineCraftInventoryPlayer(newInv);
        }
        return inventory;
    }

    @Override
    public void setInventory(org.bukkit.inventory.PlayerInventory inventory) {
        CraftInventoryPlayer inv = (CraftInventoryPlayer) inventory;
        net.minecraft.nbt.CompoundTag nbtTagCompound = ((CompoundTagImpl) compound).toNMSTag();
        nbtTagCompound.put("Inventory", inv.getInventory().save(new ListTag()));
        this.compound = CompoundTagImpl.fromNMSTag(nbtTagCompound);
        markModified();
    }

    @Override
    public Inventory getEnderChest() {
        if (enderchest == null) {
            PlayerEnderChestContainer endchest = new PlayerEnderChestContainer(null);
            endchest.fromTag(((CompoundTagImpl) this.compound).toNMSTag().getList("EnderItems", 10), CraftRegistry.getMinecraftRegistry());
            enderchest = new CraftInventory(endchest);
        }
        return enderchest;
    }

    @Override
    public void setEnderChest(Inventory inventory) {
        net.minecraft.nbt.CompoundTag nbtTagCompound = ((CompoundTagImpl) compound).toNMSTag();
        nbtTagCompound.put("EnderItems", ((PlayerEnderChestContainer) ((CraftInventory) inventory).getInventory()).createTag(CraftRegistry.getMinecraftRegistry()));
        this.compound = CompoundTagImpl.fromNMSTag(nbtTagCompound);
        markModified();
    }

    @Override
    public double getMaxHealth() {
        AttributeInstance maxHealth = getAttributes().getInstance(Attributes.MAX_HEALTH);
        return maxHealth == null ? Attributes.MAX_HEALTH.value().getDefaultValue() : maxHealth.getValue();
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
        amb.load(((CompoundTagImpl) this.compound).toNMSTag().getList("Attributes", 10));
        return amb;
    }

    public void setAttributes(AttributeMap attributes) {
        net.minecraft.nbt.CompoundTag nbtTagCompound = ((CompoundTagImpl) compound).toNMSTag();
        nbtTagCompound.put("Attributes", attributes.save());
        this.compound = CompoundTagImpl.fromNMSTag(nbtTagCompound);
        markModified();
    }

    @Override
    protected boolean loadPlayerData(UUID uuid) {
        try {
            this.player = uuid;
            for (org.bukkit.World w : Bukkit.getWorlds()) {
                this.file = new File(w.getWorldFolder(), "playerdata" + File.separator + this.player + ".dat");
                if (this.file.exists()) {
                    this.compound = CompoundTagImpl.fromNMSTag(NbtIo.readCompressed(new FileInputStream(this.file), NbtAccounter.unlimitedHeap()));
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
    public void saveInternal(CompoundTag compound) {
        try {
            NbtIo.writeCompressed(((CompoundTagImpl) compound).toNMSTag(), new FileOutputStream(this.file));
        }
        catch (Exception e) {
            Debug.echoError(e);
        }
    }
}
