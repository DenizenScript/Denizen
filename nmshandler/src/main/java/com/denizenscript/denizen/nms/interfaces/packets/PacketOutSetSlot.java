package com.denizenscript.denizen.nms.interfaces.packets;

import org.bukkit.inventory.ItemStack;

public interface PacketOutSetSlot {

    ItemStack getItemStack();

    void setItemStack(ItemStack itemStack);
}
