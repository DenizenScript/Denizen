package com.denizenscript.denizen.objects.properties.bukkit;

import com.denizenscript.denizen.nms.util.jnbt.NBTOutputStream;
import com.denizenscript.denizen.nms.util.jnbt.Tag;
import com.denizenscript.denizen.objects.properties.item.ItemRawNBT;
import com.denizenscript.denizencore.utilities.debugging.Debug;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.core.BinaryTag;
import com.denizenscript.denizencore.objects.core.MapTag;
import com.denizenscript.denizencore.objects.properties.Property;
import com.denizenscript.denizencore.objects.properties.PropertyParser;

import java.io.ByteArrayOutputStream;

public class BukkitMapTagProperties implements Property {

    public static boolean describes(ObjectTag map) {
        return map instanceof MapTag;
    }

    public static BukkitMapTagProperties getFrom(ObjectTag map) {
        if (!describes(map)) {
            return null;
        }
        else {
            return new BukkitMapTagProperties((MapTag) map);
        }
    }

    private BukkitMapTagProperties(MapTag map) {
        this.map = map;
    }

    public static final String[] handledMechs = new String[] {
    }; // None

    public MapTag map;

    public static void registerTags() {

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
        // - define data <[something].map_to_nbt.gzip_compress>
        // - ~filewrite path:data/<player.uuid>.dat data:<[data]>
        // -->
        PropertyParser.registerStaticTag(BukkitMapTagProperties.class, BinaryTag.class, "map_to_nbt", (attribute, object) -> {
            try {
                Tag tag = ItemRawNBT.convertObjectToNbt(object.map.toString(), attribute.context, "(root).");
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

    @Override
    public String getPropertyString() {
        return null;
    }

    @Override
    public String getPropertyId() {
        return "BukkitMapTagProperties";
    }

    @Override
    public void adjust(Mechanism mechanism) {
        // None
    }
}
