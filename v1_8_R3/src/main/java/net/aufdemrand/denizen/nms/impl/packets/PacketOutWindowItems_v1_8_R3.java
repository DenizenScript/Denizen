package net.aufdemrand.denizen.nms.impl.packets;

import net.aufdemrand.denizen.nms.interfaces.packets.PacketOutWindowItems;
import net.aufdemrand.denizen.nms.util.ReflectionHelper;
import net.aufdemrand.denizencore.utilities.debugging.dB;
import net.minecraft.server.v1_8_R3.ItemStack;
import net.minecraft.server.v1_8_R3.PacketPlayOutWindowItems;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack;

import java.lang.reflect.Field;
import java.util.Map;

public class PacketOutWindowItems_v1_8_R3 implements PacketOutWindowItems {

    private PacketPlayOutWindowItems internal;
    private org.bukkit.inventory.ItemStack[] contents;

    public PacketOutWindowItems_v1_8_R3(PacketPlayOutWindowItems internal) {
        this.internal = internal;
        try {
            ItemStack[] nms = (ItemStack[]) CONTENTS.get(internal);
            contents = new org.bukkit.inventory.ItemStack[nms.length];
            for (int i = 0; i < nms.length; i++) {
                contents[i] = CraftItemStack.asBukkitCopy(nms[i]);
            }
        }
        catch (Exception e) {
            dB.echoError(e);
        }
    }

    @Override
    public org.bukkit.inventory.ItemStack[] getContents() {
        return contents;
    }

    @Override
    public void setContents(org.bukkit.inventory.ItemStack[] contents) {
        ItemStack[] nms = new ItemStack[contents.length];
        for (int i = 0; i < contents.length; i++) {
            nms[i] = CraftItemStack.asNMSCopy(contents[i]);
        }
        try {
            CONTENTS.set(internal, nms);
        }
        catch (Exception e) {
            dB.echoError(e);
        }
    }

    private static final Field CONTENTS;

    static {
        Map<String, Field> fields = ReflectionHelper.getFields(PacketPlayOutWindowItems.class);
        CONTENTS = fields.get("b");
    }
}
