package com.denizenscript.denizen.v1_13.impl.packets;

import com.denizenscript.denizen.nms.interfaces.packets.PacketOutSetSlot;
import com.denizenscript.denizen.nms.util.ReflectionHelper;
import com.denizenscript.denizencore.utilities.debugging.Debug;
import net.minecraft.server.v1_13_R2.ItemStack;
import net.minecraft.server.v1_13_R2.PacketPlayOutSetSlot;
import org.bukkit.craftbukkit.v1_13_R2.inventory.CraftItemStack;

import java.lang.reflect.Field;
import java.util.Map;

public class PacketOutSetSlotImpl implements PacketOutSetSlot {

    private PacketPlayOutSetSlot internal;
    private org.bukkit.inventory.ItemStack itemStack;

    public PacketOutSetSlotImpl(PacketPlayOutSetSlot internal) {
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
        Map<String, Field> fields = ReflectionHelper.getFields(PacketPlayOutSetSlot.class);
        ITEM_STACK = fields.get("c");
    }
}
