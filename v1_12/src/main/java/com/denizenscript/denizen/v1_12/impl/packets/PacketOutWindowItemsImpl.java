package com.denizenscript.denizen.v1_12.impl.packets;

import com.denizenscript.denizen.nms.interfaces.packets.PacketOutWindowItems;
import com.denizenscript.denizen.nms.util.ReflectionHelper;
import com.denizenscript.denizencore.utilities.debugging.Debug;
import net.minecraft.server.v1_12_R1.ItemStack;
import net.minecraft.server.v1_12_R1.NonNullList;
import net.minecraft.server.v1_12_R1.PacketPlayOutWindowItems;
import org.bukkit.craftbukkit.v1_12_R1.inventory.CraftItemStack;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

public class PacketOutWindowItemsImpl implements PacketOutWindowItems {

    private PacketPlayOutWindowItems internal;
    private List<org.bukkit.inventory.ItemStack> contents;

    public PacketOutWindowItemsImpl(PacketPlayOutWindowItems internal) {
        this.internal = internal;
        try {
            List<ItemStack> nms = (List<ItemStack>) CONTENTS.get(internal);
            contents = NonNullList.a();
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
        return contents.toArray(new org.bukkit.inventory.ItemStack[contents.size()]);
    }

    @Override
    public void setContents(org.bukkit.inventory.ItemStack[] contents) {
        List<ItemStack> nms = NonNullList.a();
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

    private static final Field CONTENTS;

    static {
        Map<String, Field> fields = ReflectionHelper.getFields(PacketPlayOutWindowItems.class);
        CONTENTS = fields.get("b");
    }
}
