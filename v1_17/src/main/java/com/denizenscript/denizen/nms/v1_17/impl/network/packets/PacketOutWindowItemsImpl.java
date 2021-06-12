package com.denizenscript.denizen.nms.v1_17.impl.network.packets;

import com.denizenscript.denizen.nms.interfaces.packets.PacketOutWindowItems;
import com.denizenscript.denizencore.utilities.ReflectionHelper;
import com.denizenscript.denizencore.utilities.debugging.Debug;
import net.minecraft.core.NonNullList;
import net.minecraft.network.protocol.game.ClientboundContainerSetContentPacket;
import net.minecraft.world.item.ItemStack;
import org.bukkit.craftbukkit.v1_17_R1.inventory.CraftItemStack;

import java.lang.reflect.Field;
import java.util.List;

public class PacketOutWindowItemsImpl implements PacketOutWindowItems {

    private ClientboundContainerSetContentPacket internal;
    private List<org.bukkit.inventory.ItemStack> contents;

    public PacketOutWindowItemsImpl(ClientboundContainerSetContentPacket internal) {
        this.internal = internal;
        try {
            List<ItemStack> nms = (List<ItemStack>) CONTENTS.get(internal);
            contents = NonNullList.create();
            for (ItemStack itemStack : nms) {
                contents.add(CraftItemStack.asBukkitCopy(itemStack));
            }
        }
        catch (Exception e) {
            Debug.echoError(e);
        }
    }

    @Override
    public org.bukkit.inventory.ItemStack[] getContents() {
        return contents.toArray(new org.bukkit.inventory.ItemStack[0]);
    }

    @Override
    public void setContents(org.bukkit.inventory.ItemStack[] contents) {
        List<ItemStack> nms = NonNullList.create();
        for (org.bukkit.inventory.ItemStack content : contents) {
            nms.add(CraftItemStack.asNMSCopy(content));
        }
        try {
            CONTENTS.set(internal, nms);
        }
        catch (Exception e) {
            Debug.echoError(e);
        }
    }

    private static final Field CONTENTS = ReflectionHelper.getFields(ClientboundContainerSetContentPacket.class).getFirstOfType(List.class);
}
