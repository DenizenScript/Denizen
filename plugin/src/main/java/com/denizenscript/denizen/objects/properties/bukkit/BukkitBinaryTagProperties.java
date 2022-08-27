package com.denizenscript.denizen.objects.properties.bukkit;

import com.denizenscript.denizen.nms.util.jnbt.NBTInputStream;
import com.denizenscript.denizen.nms.util.jnbt.NamedTag;
import com.denizenscript.denizen.objects.properties.item.ItemRawNBT;
import com.denizenscript.denizen.utilities.debugging.Debug;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.core.BinaryTag;
import com.denizenscript.denizencore.objects.properties.Property;
import com.denizenscript.denizencore.objects.properties.PropertyParser;

import java.io.ByteArrayInputStream;

public class BukkitBinaryTagProperties implements Property {

    public static boolean describes(ObjectTag data) {
        return data instanceof BinaryTag;
    }

    public static BukkitBinaryTagProperties getFrom(ObjectTag data) {
        if (!describes(data)) {
            return null;
        }
        else {
            return new BukkitBinaryTagProperties((BinaryTag) data);
        }
    }

    private BukkitBinaryTagProperties(BinaryTag data) {
        this.data = data;
    }

    public static final String[] handledMechs = new String[] {
    }; // None

    public BinaryTag data;

    public static void registerTags() {

        // <--[tag]
        // @attribute <BinaryTag.nbt_to_map>
        // @returns MapTag
        // @group conversion
        // @description
        // Converts raw NBT binary data to a MapTag.
        // This under some circumstances might not return a map, depending on the underlying data.
        // Refer to <@link language Raw NBT Encoding>
        // @example
        // # Reads a player ".dat" file's NBT data
        // - ~fileread path:data/<player.uuid>.dat save:x
        // - define data <entry[x].data.gzip_decompress.nbt_to_map>
        // # Now do something with "<[data]>"
        // -->
        PropertyParser.registerStaticTag(BukkitBinaryTagProperties.class, ObjectTag.class, "nbt_to_map", (attribute, object) -> {
            try {
                ByteArrayInputStream stream = new ByteArrayInputStream(object.data.data);
                NBTInputStream nbtStream = new NBTInputStream(stream);
                NamedTag tag = nbtStream.readNamedTag();
                nbtStream.close();
                stream.close();
                return ItemRawNBT.jnbtTagToObject(tag.getTag());
            }
            catch (Throwable ex) {
                Debug.echoError(ex);
                return null;
            }
        });
    }

    @Override
    public String getPropertyString() {
        return null;
    }

    @Override
    public String getPropertyId() {
        return "BukkitBinaryTagProperties";
    }

    @Override
    public void adjust(Mechanism mechanism) {
        // None
    }
}
