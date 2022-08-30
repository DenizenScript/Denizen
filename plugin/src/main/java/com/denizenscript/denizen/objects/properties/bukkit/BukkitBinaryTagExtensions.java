package com.denizenscript.denizen.objects.properties.bukkit;

import com.denizenscript.denizen.nms.util.jnbt.NBTInputStream;
import com.denizenscript.denizen.nms.util.jnbt.NamedTag;
import com.denizenscript.denizen.objects.properties.item.ItemRawNBT;
import com.denizenscript.denizencore.utilities.debugging.Debug;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.core.BinaryTag;

import java.io.ByteArrayInputStream;

public class BukkitBinaryTagExtensions {

    public static void register() {

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
        BinaryTag.tagProcessor.registerStaticTag(ObjectTag.class, "nbt_to_map", (attribute, object) -> {
            try {
                ByteArrayInputStream stream = new ByteArrayInputStream(object.data);
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
}
