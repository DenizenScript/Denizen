package com.denizenscript.denizen.nms.v1_20.impl.network.handlers.packet;

import com.denizenscript.denizen.nms.v1_20.impl.network.handlers.DenizenNetworkManagerImpl;
import com.denizenscript.denizen.objects.ItemTag;
import com.denizenscript.denizen.scripts.commands.entity.FakeEquipCommand;
import com.mojang.datafixers.util.Pair;
import net.minecraft.core.NonNullList;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.*;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.bukkit.craftbukkit.v1_20_R4.inventory.CraftItemStack;

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
                case BODY -> pair.getSecond(); // TODO: 1.20.6: is this actually used here? do we want to allow overriding it?
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
        if (setContentPacket.getContainerId() != 0) {
            return setContentPacket;
        }
        FakeEquipCommand.EquipmentOverride override = FakeEquipCommand.getOverrideFor(networkManager.player.getUUID(), networkManager.player.getBukkitEntity());
        if (override == null) {
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
            items.set(getMainHandSlot(networkManager.player), CraftItemStack.asNMSCopy(override.hand.getItemStack()));
        }
        return new ClientboundContainerSetContentPacket(setContentPacket.getContainerId(), setContentPacket.getStateId(), items, setContentPacket.getCarriedItem());
    }

    public static ClientboundContainerSetSlotPacket processContainerSetSlotPacket(DenizenNetworkManagerImpl networkManager, ClientboundContainerSetSlotPacket setSlotPacket) {
        if (FakeEquipCommand.overrides.isEmpty()) {
            return setSlotPacket;
        }
        if (setSlotPacket.getContainerId() != 0) {
            return setSlotPacket;
        }
        FakeEquipCommand.EquipmentOverride override = FakeEquipCommand.getOverrideFor(networkManager.player.getUUID(), networkManager.player.getBukkitEntity());
        if (override == null) {
            return setSlotPacket;
        }
        ItemTag item = switch (setSlotPacket.getSlot()) {
            case 5 -> override.head;
            case 6 -> override.chest;
            case 7 -> override.legs;
            case 8 -> override.boots;
            case 45 -> override.offhand;
            default -> setSlotPacket.getSlot() == getMainHandSlot(networkManager.player) ? override.hand : null;
        };
        if (item == null) {
            return setSlotPacket;
        }
        return new ClientboundContainerSetSlotPacket(setSlotPacket.getContainerId(), setSlotPacket.getStateId(), setSlotPacket.getSlot(), CraftItemStack.asNMSCopy(item.getItemStack()));
    }

    public static int getMainHandSlot(ServerPlayer player) {
        return player.getInventory().selected + 36;
    }
}
