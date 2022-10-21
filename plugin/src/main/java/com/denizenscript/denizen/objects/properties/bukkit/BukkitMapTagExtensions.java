package com.denizenscript.denizen.objects.properties.bukkit;

import com.denizenscript.denizen.nms.util.jnbt.NBTOutputStream;
import com.denizenscript.denizen.nms.util.jnbt.Tag;
import com.denizenscript.denizen.objects.properties.item.ItemRawNBT;
import com.denizenscript.denizencore.objects.core.MapTag;
import com.denizenscript.denizencore.utilities.debugging.Debug;
import com.denizenscript.denizencore.objects.core.BinaryTag;

import java.io.ByteArrayOutputStream;

public class BukkitMapTagExtensions {

    public static void register() {

        // <--[tag]
        // @attribute <MapTag.map_to_nbt>
        // @returns BinaryTag
        // @group conversion
        // @description
        // Converts the NBT-formatted MapTag to raw binary NBT.
        // Refer to <@link language Raw NBT Encoding>
        // @example
        // # Stores a player ".dat" file's NBT data
        // # NOTE: replace 'something' with your map data
        // - define playerdata something
        // - define data <[playerdata].map_to_nbt.gzip_compress>
        // - ~filewrite path:data/<player.uuid>.dat data:<[data]>
        // -->
        MapTag.tagProcessor.registerStaticTag(BinaryTag.class, "map_to_nbt", (attribute, object) -> {
            try {
                Tag tag = ItemRawNBT.convertObjectToNbt(object.toString(), attribute.context, "(root).");
                ByteArrayOutputStream output = new ByteArrayOutputStream();
                NBTOutputStream nbtStream = new NBTOutputStream(output);
                nbtStream.writeNamedTag("", tag);
                nbtStream.close();
                byte[] data = output.toByteArray();
                output.close();
                return new BinaryTag(data);
            }
            catch (Throwable ex) {
                Debug.echoError(ex);
                return null;
            }
        });
    }
}
