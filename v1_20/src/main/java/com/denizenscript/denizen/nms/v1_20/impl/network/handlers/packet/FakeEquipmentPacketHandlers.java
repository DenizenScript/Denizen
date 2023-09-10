package com.denizenscript.denizen.nms.v1_20.impl.network.handlers.packet;

import com.denizenscript.denizen.nms.v1_20.impl.network.handlers.DenizenNetworkManagerImpl;
import com.denizenscript.denizen.scripts.commands.entity.FakeEquipCommand;
import com.mojang.datafixers.util.Pair;
import net.minecraft.core.NonNullList;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.*;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.bukkit.craftbukkit.v1_20_R1.inventory.CraftItemStack;

import java.util.ArrayList;
import java.util.List;

public class FakeEquipmentPacketHandlers {

    public static void registerHandlers() {
        DenizenNetworkManagerImpl.registerPacketHandler(ClientboundSetEquipmentPacket.class, FakeEquipmentPacketHandlers::processSetEquipmentPacket);
        DenizenNetworkManagerImpl.registerPacketHandler(ClientboundEntityEventPacket.class, FakeEquipmentPacketHandlers::processEntityEventPacket);
        DenizenNetworkManagerImpl.registerPacketHandler(ClientboundContainerSetContentPacket.class, FakeEquipmentPacketHandlers::processContainerSetContentPacket);
        DenizenNetworkManagerImpl.registerPacketHandler(ClientboundContainerSetSlotPacket.class, FakeEquipmentPacketHandlers::processContainerSetSlotPacket);
    }

    public static ClientboundSetEquipmentPacket processSetEquipmentPacket(DenizenNetworkManagerImpl networkManager, ClientboundSetEquipmentPacket setEquipmentPacket) {
        if (FakeEquipCommand.overrides.isEmpty()) {
            return setEquipmentPacket;
        }
        Entity entity = networkManager.player.level().getEntity(setEquipmentPacket.getEntity());
        if (entity == null) {
            return setEquipmentPacket;
        }
        FakeEquipCommand.EquipmentOverride override = FakeEquipCommand.getOverrideFor(entity.getUUID(), networkManager.player.getBukkitEntity());
        if (override == null) {
            return setEquipmentPacket;
        }
        List<Pair<EquipmentSlot, ItemStack>> equipment = new ArrayList<>(setEquipmentPacket.getSlots());
        for (int i = 0; i < equipment.size(); i++) {
            Pair<EquipmentSlot, ItemStack> pair = equipment.get(i);
            ItemStack use = switch (pair.getFirst()) {
                case MAINHAND -> override.hand == null ? pair.getSecond() : CraftItemStack.asNMSCopy(override.hand.getItemStack());
                case OFFHAND -> override.offhand == null ? pair.getSecond() : CraftItemStack.asNMSCopy(override.offhand.getItemStack());
                case CHEST -> override.chest == null ? pair.getSecond() : CraftItemStack.asNMSCopy(override.chest.getItemStack());
                case HEAD -> override.head == null ? pair.getSecond() : CraftItemStack.asNMSCopy(override.head.getItemStack());
                case LEGS -> override.legs == null ? pair.getSecond() : CraftItemStack.asNMSCopy(override.legs.getItemStack());
                case FEET -> override.boots == null ? pair.getSecond() : CraftItemStack.asNMSCopy(override.boots.getItemStack());
            };
            equipment.set(i, new Pair<>(pair.getFirst(), use));
        }
        return new ClientboundSetEquipmentPacket(setEquipmentPacket.getEntity(), equipment);
    }

    public static Packet<ClientGamePacketListener> processEntityEventPacket(DenizenNetworkManagerImpl networkManager, ClientboundEntityEventPacket entityEventPacket) {
        if (FakeEquipCommand.overrides.isEmpty()) {
            return entityEventPacket;
        }
        if (entityEventPacket.getEventId() != 55) {
            return entityEventPacket;
        }
        if (!(entityEventPacket.getEntity(networkManager.player.level()) instanceof LivingEntity livingEntity)) {
            return entityEventPacket;
        }
        FakeEquipCommand.EquipmentOverride override = FakeEquipCommand.getOverrideFor(livingEntity.getUUID(), networkManager.player.getBukkitEntity());
        if (override == null || (override.hand == null && override.offhand == null)) {
            return entityEventPacket;
        }
        ItemStack hand = override.hand != null ? CraftItemStack.asNMSCopy(override.hand.getItemStack()) : livingEntity.getMainHandItem();
        ItemStack offhand = override.offhand != null ? CraftItemStack.asNMSCopy(override.offhand.getItemStack()) : livingEntity.getOffhandItem();
        return new ClientboundSetEquipmentPacket(livingEntity.getId(), List.of(new Pair<>(EquipmentSlot.MAINHAND, hand), new Pair<>(EquipmentSlot.OFFHAND, offhand)));
    }

    public static ClientboundContainerSetContentPacket processContainerSetContentPacket(DenizenNetworkManagerImpl networkManager, ClientboundContainerSetContentPacket setContentPacket) {
        if (FakeEquipCommand.overrides.isEmpty()) {
            return setContentPacket;
        }
        FakeEquipCommand.EquipmentOverride override = FakeEquipCommand.getOverrideFor(networkManager.player.getUUID(), networkManager.player.getBukkitEntity());
        if (override == null) {
            return setContentPacket;
        }
        int window = setContentPacket.getContainerId();
        if (window != 0) {
            return setContentPacket;
        }
        NonNullList<ItemStack> items = (NonNullList<ItemStack>) setContentPacket.getItems();
        if (override.head != null) {
            items.set(5, CraftItemStack.asNMSCopy(override.head.getItemStack()));
        }
        if (override.chest != null) {
            items.set(6, CraftItemStack.asNMSCopy(override.chest.getItemStack()));
        }
        if (override.legs != null) {
            items.set(7, CraftItemStack.asNMSCopy(override.legs.getItemStack()));
        }
        if (override.boots != null) {
            items.set(8, CraftItemStack.asNMSCopy(override.boots.getItemStack()));
        }
        if (override.offhand != null) {
            items.set(45, CraftItemStack.asNMSCopy(override.offhand.getItemStack()));
        }
        if (override.hand != null) {
            items.set(networkManager.player.getInventory().selected + 36, CraftItemStack.asNMSCopy(override.hand.getItemStack()));
        }
        ClientboundContainerSetContentPacket newPacket = new ClientboundContainerSetContentPacket(window, setContentPacket.getStateId(), items, setContentPacket.getCarriedItem());
        return newPacket;
    }

    public static ClientboundContainerSetSlotPacket processContainerSetSlotPacket(DenizenNetworkManagerImpl networkManager, ClientboundContainerSetSlotPacket setSlotPacket) {
        if (FakeEquipCommand.overrides.isEmpty()) {
            return setSlotPacket;
        }
        FakeEquipCommand.EquipmentOverride override = FakeEquipCommand.getOverrideFor(networkManager.player.getUUID(), networkManager.player.getBukkitEntity());
        if (override == null) {
            return setSlotPacket;
        }
        int window = setSlotPacket.getContainerId();
        if (window != 0) {
            return setSlotPacket;
        }
        int slot = setSlotPacket.getSlot();
        org.bukkit.inventory.ItemStack item = null;
        if (slot == 5 && override.head != null) {
            item = override.head.getItemStack();
        }
        else if (slot == 6 && override.chest != null) {
            item = override.chest.getItemStack();
        }
        else if (slot == 7 && override.legs != null) {
            item = override.legs.getItemStack();
        }
        else if (slot == 8 && override.boots != null) {
            item = override.boots.getItemStack();
        }
        else if (slot == 45 && override.offhand != null) {
            item = override.offhand.getItemStack();
        }
        else if (slot == networkManager.player.getInventory().selected + 36 && override.hand != null) {
            item = override.hand.getItemStack();
        }
        if (item == null) {
            return setSlotPacket;
        }
        ClientboundContainerSetSlotPacket newPacket = new ClientboundContainerSetSlotPacket(window, setSlotPacket.getStateId(), slot, CraftItemStack.asNMSCopy(item));
        return newPacket;
    }
}
