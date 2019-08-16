package com.denizenscript.denizen.nms.interfaces.packets;

import org.bukkit.inventory.ItemStack;

public interface PacketOutWindowItems {

    ItemStack[] getContents();

    void setContents(ItemStack[] contents);
}
