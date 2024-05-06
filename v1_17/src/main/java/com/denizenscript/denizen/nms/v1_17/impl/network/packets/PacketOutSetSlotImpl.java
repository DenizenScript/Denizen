package com.denizenscript.denizen.nms.v1_17.impl.network.packets;

import com.denizenscript.denizen.nms.interfaces.packets.PacketOutSetSlot;
import com.denizenscript.denizencore.utilities.ReflectionHelper;
import com.denizenscript.denizencore.utilities.debugging.Debug;
import net.minecraft.network.protocol.game.ClientboundContainerSetSlotPacket;
import net.minecraft.world.item.ItemStack;
import org.bukkit.craftbukkit.v1_17_R1.inventory.CraftItemStack;

import java.lang.reflect.Field;
import java.util.Map;

public class PacketOutSetSlotImpl implements PacketOutSetSlot {

    private ClientboundContainerSetSlotPacket internal;
    private org.bukkit.inventory.ItemStack itemStack;

    public PacketOutSetSlotImpl(ClientboundContainerSetSlotPacket internal) {
        this.internal = internal;
        try {
            ItemStack nms = (ItemStack) ITEM_STACK.get(internal);
            itemStack = CraftItemStack.asBukkitCopy(nms);
        }
        catch (Exception e) {
            Debug.echoError(e);
        }
    }

    @Override
    public org.bukkit.inventory.ItemStack getItemStack() {
        return itemStack;
    }

    @Override
    public void setItemStack(org.bukkit.inventory.ItemStack itemStack) {
        try {
            ITEM_STACK.set(internal, CraftItemStack.asNMSCopy(itemStack));
        }
        catch (Exception e) {
            Debug.echoError(e);
        }
    }

    private static final Field ITEM_STACK;

    static {
        ITEM_STACK = ReflectionHelper.getFields(ClientboundContainerSetSlotPacket.class).get("c");
    }
}
