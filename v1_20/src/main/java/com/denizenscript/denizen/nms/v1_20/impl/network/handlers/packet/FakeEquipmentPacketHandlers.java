package com.denizenscript.denizen.nms.v1_20.impl.network.handlers.packet;

import com.denizenscript.denizen.scripts.commands.entity.FakeEquipCommand;
import com.denizenscript.denizencore.utilities.debugging.Debug;
import com.mojang.datafixers.util.Pair;
import net.minecraft.core.NonNullList;
import net.minecraft.network.PacketSendListener;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundContainerSetContentPacket;
import net.minecraft.network.protocol.game.ClientboundContainerSetSlotPacket;
import net.minecraft.network.protocol.game.ClientboundEntityEventPacket;
import net.minecraft.network.protocol.game.ClientboundSetEquipmentPacket;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import org.bukkit.craftbukkit.v1_20_R1.inventory.CraftItemStack;

import java.util.ArrayList;
import java.util.List;

public class FakeEquipmentPacketHandlers {

    public static void registerHandlers() {

    }

    public boolean processEquipmentForPacket(Packet<?> packet, PacketSendListener genericfuturelistener) {
        if (FakeEquipCommand.overrides.isEmpty()) {
            return false;
        }
        try {
            if (packet instanceof ClientboundSetEquipmentPacket) {
                int eid = ((ClientboundSetEquipmentPacket) packet).getEntity();
                Entity ent = player.level().getEntity(eid);
                if (ent == null) {
                    return false;
                }
                FakeEquipCommand.EquipmentOverride override = FakeEquipCommand.getOverrideFor(ent.getUUID(), player.getBukkitEntity());
                if (override == null) {
                    return false;
                }
                List<Pair<EquipmentSlot, ItemStack>> equipment = new ArrayList<>(((ClientboundSetEquipmentPacket) packet).getSlots());
                ClientboundSetEquipmentPacket newPacket = new ClientboundSetEquipmentPacket(eid, equipment);
                for (int i = 0; i < equipment.size(); i++) {
                    Pair<net.minecraft.world.entity.EquipmentSlot, ItemStack> pair =  equipment.get(i);
                    ItemStack use = pair.getSecond();
                    switch (pair.getFirst()) {
                        case MAINHAND:
                            use = override.hand == null ? use : CraftItemStack.asNMSCopy(override.hand.getItemStack());
                            break;
                        case OFFHAND:
                            use = override.offhand == null ? use : CraftItemStack.asNMSCopy(override.offhand.getItemStack());
                            break;
                        case CHEST:
                            use = override.chest == null ? use : CraftItemStack.asNMSCopy(override.chest.getItemStack());
                            break;
                        case HEAD:
                            use = override.head == null ? use : CraftItemStack.asNMSCopy(override.head.getItemStack());
                            break;
                        case LEGS:
                            use = override.legs == null ? use : CraftItemStack.asNMSCopy(override.legs.getItemStack());
                            break;
                        case FEET:
                            use = override.boots == null ? use : CraftItemStack.asNMSCopy(override.boots.getItemStack());
                            break;
                    }
                    equipment.set(i, new Pair<>(pair.getFirst(), use));
                }
                oldManager.send(newPacket, genericfuturelistener);
                return true;
            }
            else if (packet instanceof ClientboundEntityEventPacket) {
                Entity ent = ((ClientboundEntityEventPacket) packet).getEntity(player.level());
                if (!(ent instanceof net.minecraft.world.entity.LivingEntity)) {
                    return false;
                }
                FakeEquipCommand.EquipmentOverride override = FakeEquipCommand.getOverrideFor(ent.getUUID(), player.getBukkitEntity());
                if (override == null || (override.hand == null && override.offhand == null)) {
                    return false;
                }
                if (((ClientboundEntityEventPacket) packet).getEventId() != (byte) 55) {
                    return false;
                }
                List<Pair<net.minecraft.world.entity.EquipmentSlot, ItemStack>> equipment = new ArrayList<>();
                ItemStack hand = override.hand != null ? CraftItemStack.asNMSCopy(override.hand.getItemStack()) : ((net.minecraft.world.entity.LivingEntity) ent).getMainHandItem();
                ItemStack offhand = override.offhand != null ? CraftItemStack.asNMSCopy(override.offhand.getItemStack()) : ((net.minecraft.world.entity.LivingEntity) ent).getOffhandItem();
                equipment.add(new Pair<>(net.minecraft.world.entity.EquipmentSlot.MAINHAND, hand));
                equipment.add(new Pair<>(net.minecraft.world.entity.EquipmentSlot.OFFHAND, offhand));
                ClientboundSetEquipmentPacket newPacket = new ClientboundSetEquipmentPacket(ent.getId(), equipment);
                oldManager.send(newPacket, genericfuturelistener);
                return true;
            }
            else if (packet instanceof ClientboundContainerSetContentPacket) {
                FakeEquipCommand.EquipmentOverride override = FakeEquipCommand.getOverrideFor(player.getUUID(), player.getBukkitEntity());
                if (override == null) {
                    return false;
                }
                int window = ((ClientboundContainerSetContentPacket) packet).getContainerId();
                if (window != 0) {
                    return false;
                }
                NonNullList<ItemStack> items = (NonNullList<ItemStack>) ((ClientboundContainerSetContentPacket) packet).getItems();
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
                    items.set(player.getInventory().selected + 36, CraftItemStack.asNMSCopy(override.hand.getItemStack()));
                }
                ClientboundContainerSetContentPacket newPacket = new ClientboundContainerSetContentPacket(window, ((ClientboundContainerSetContentPacket) packet).getStateId(), items, ((ClientboundContainerSetContentPacket) packet).getCarriedItem());
                oldManager.send(newPacket, genericfuturelistener);
                return true;
            }
            else if (packet instanceof ClientboundContainerSetSlotPacket) {
                FakeEquipCommand.EquipmentOverride override = FakeEquipCommand.getOverrideFor(player.getUUID(), player.getBukkitEntity());
                if (override == null) {
                    return false;
                }
                int window = ((ClientboundContainerSetSlotPacket) packet).getContainerId();
                if (window != 0) {
                    return false;
                }
                int slot = ((ClientboundContainerSetSlotPacket) packet).getSlot();
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
                else if (slot == player.getInventory().selected + 36 && override.hand != null) {
                    item = override.hand.getItemStack();
                }
                if (item == null) {
                    return false;
                }
                ClientboundContainerSetSlotPacket newPacket = new ClientboundContainerSetSlotPacket(window, ((ClientboundContainerSetSlotPacket) packet).getStateId(), slot, CraftItemStack.asNMSCopy(item));
                oldManager.send(newPacket, genericfuturelistener);
                return true;
            }
        }
        catch (Throwable ex) {
            Debug.echoError(ex);
        }
        return false;
    }
}
