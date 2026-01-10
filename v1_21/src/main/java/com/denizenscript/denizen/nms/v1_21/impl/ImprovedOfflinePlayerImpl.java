package com.denizenscript.denizen.nms.v1_21.impl;

import com.denizenscript.denizen.nms.abstracts.ImprovedOfflinePlayer;
import com.denizenscript.denizen.nms.v1_21.Handler;
import com.denizenscript.denizen.nms.v1_21.helpers.NBTAdapter;
import com.denizenscript.denizencore.utilities.debugging.Debug;
import com.mojang.authlib.GameProfile;
import net.kyori.adventure.nbt.CompoundBinaryTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ClientInformation;
import net.minecraft.server.level.ParticleStatus;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.ItemStackWithSlot;
import net.minecraft.world.entity.EntityEquipment;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.DefaultAttributes;
import net.minecraft.world.entity.player.ChatVisiblity;
import net.minecraft.world.inventory.PlayerEnderChestContainer;
import net.minecraft.world.level.storage.ValueOutput;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_21_R7.CraftServer;
import org.bukkit.craftbukkit.v1_21_R7.CraftWorld;
import org.bukkit.craftbukkit.v1_21_R7.inventory.CraftInventory;
import org.bukkit.craftbukkit.v1_21_R7.inventory.CraftInventoryPlayer;
import org.bukkit.entity.HumanEntity;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.UUID;
import java.util.function.Consumer;

public class ImprovedOfflinePlayerImpl extends ImprovedOfflinePlayer {

    public ImprovedOfflinePlayerImpl(UUID playeruuid) {
        super(playeruuid);
    }

    public static class OfflinePlayerInventory extends net.minecraft.world.entity.player.Inventory {

        public OfflinePlayerInventory(net.minecraft.world.entity.player.Player entityhuman) {
            super(entityhuman, new EntityEquipment()); // TODO: 1.21.5: is the new Equipment right here?
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

    public void editData(Consumer<ValueOutput> editor) {
        this.compound = NBTAdapter.toAPI(Handler.useValueOutput(NBTAdapter.toNMS(this.compound), editor));
        markModified();
    }

    @Override
    public org.bukkit.inventory.PlayerInventory getInventory() {
        if (inventory == null) {
            net.minecraft.world.entity.player.Inventory newInv = new OfflinePlayerInventory(getFakeNmsPlayer());
            Handler.useValueInput(NBTAdapter.toNMS(this.compound), valueInput -> newInv.load(valueInput.listOrEmpty("Inventory", ItemStackWithSlot.CODEC)));
            inventory = new OfflineCraftInventoryPlayer(newInv);
        }
        return inventory;
    }

    @Override
    public void setInventory(org.bukkit.inventory.PlayerInventory inventory) {
        CraftInventoryPlayer inv = (CraftInventoryPlayer) inventory;
        editData(valueOutput -> inv.getInventory().save(valueOutput.list("Inventory", ItemStackWithSlot.CODEC)));
    }

    @Override
    public Inventory getEnderChest() {
        if (enderchest == null) {
            PlayerEnderChestContainer nmsEnderChest = new PlayerEnderChestContainer(getFakeNmsPlayer());
            Handler.useValueInput(NBTAdapter.toNMS(this.compound), valueInput -> nmsEnderChest.fromSlots(valueInput.listOrEmpty("EnderItems", ItemStackWithSlot.CODEC)));
            enderchest = new CraftInventory(nmsEnderChest);
        }
        return enderchest;
    }

    @Override
    public void setEnderChest(Inventory inventory) {
        editData(valueOutput -> ((PlayerEnderChestContainer) ((CraftInventory) inventory).getInventory()).storeAsSlots(valueOutput.list("EnderItems", ItemStackWithSlot.CODEC)));
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
        Handler.useValueInput(NBTAdapter.toNMS(this.compound), valueInput -> valueInput.read("attributes", AttributeInstance.Packed.LIST_CODEC).ifPresent(amb::apply));
        return amb;
    }

    public void setAttributes(AttributeMap attributes) {
        editData(valueOutput -> valueOutput.store("attributes", AttributeInstance.Packed.LIST_CODEC, attributes.pack()));
    }

    @Override
    protected boolean loadPlayerData(UUID uuid) {
        try {
            this.player = uuid;
            for (org.bukkit.World w : Bukkit.getWorlds()) {
                this.file = new File(w.getWorldFolder(), "playerdata" + File.separator + this.player + ".dat");
                if (this.file.exists()) {
                    this.compound = NBTAdapter.toAPI(NbtIo.readCompressed(new FileInputStream(this.file), NbtAccounter.unlimitedHeap()));
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
