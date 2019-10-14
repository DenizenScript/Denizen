package com.denizenscript.denizen.utilities.blocks;

import com.denizenscript.denizen.nms.NMSHandler;
import com.denizenscript.denizen.nms.interfaces.BlockData;
import com.denizenscript.denizen.nms.util.jnbt.*;
import com.denizenscript.denizen.utilities.debugging.Debug;
import org.bukkit.util.BlockVector;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPInputStream;

public class SpongeSchematicHelper {

    // Referenced from WorldEdit source and Sponge schematic format v2 documentation
    public static CuboidBlockSet fromSpongeStream(InputStream is) {
        CuboidBlockSet cbs = new CuboidBlockSet();
        try {
            NBTInputStream nbtStream = new NBTInputStream(new GZIPInputStream(is));
            NamedTag rootTag = nbtStream.readNamedTag();
            nbtStream.close();
            if (!rootTag.getName().equals("Schematic")) {
                throw new Exception("Tag 'Schematic' does not exist or is not first!");
            }
            CompoundTag schematicTag = (CompoundTag) rootTag.getTag();
            Map<String, Tag> schematic = schematicTag.getValue();
            short width = getChildTag(schematic, "Width", ShortTag.class).getValue();
            short length = getChildTag(schematic, "Length", ShortTag.class).getValue();
            short height = getChildTag(schematic, "Height", ShortTag.class).getValue();
            int originX = 0;
            int originY = 0;
            int originZ = 0;
            try {
                // Note: "Offset" contains complete nonsense from WE, so just don't touch it.
                int[] offsetArr = getChildTag(schematic, "DenizenOffset", IntArrayTag.class).getValue();
                originX = offsetArr[0];
                originY = offsetArr[1];
                originZ = offsetArr[2];
            }
            catch (Exception e) {
                // Default origin, why not
            }
            cbs.x_width = width;
            cbs.z_height = length;
            cbs.y_length = height;
            cbs.center_x = originX;
            cbs.center_y = originY;
            cbs.center_z = originZ;
            cbs.blocks = new BlockData[width * length * height];
            Map<String, Tag> paletteMap = getChildTag(schematic, "Palette", CompoundTag.class).getValue();
            HashMap<Integer, ModernBlockData> palette = new HashMap<>(256);
            for (String key : paletteMap.keySet()) {
                int id = getChildTag(paletteMap, key, IntTag.class).getValue();
                ModernBlockData data = NMSHandler.getBlockHelper().parseBlockData(key);
                palette.put(id, data);
            }
            Map<BlockVector, Map<String, Tag>> tileEntitiesMap = new HashMap<>();
            if (schematic.containsKey("BlockEntities")) {
                List<Tag> tileEntities = getChildTag(schematic, "BlockEntities", ListTag.class).getValue();
                for (Tag tag : tileEntities) {
                    if (!(tag instanceof CompoundTag)) {
                        continue;
                    }
                    CompoundTag t = (CompoundTag) tag;
                    int[] pos = getChildTag(t.getValue(), "Pos", IntArrayTag.class).getValue();
                    int x = pos[0];
                    int y = pos[1];
                    int z = pos[2];
                    BlockVector vec = new BlockVector(x, y, z);
                    tileEntitiesMap.put(vec, t.getValue());
                }
            }
            byte[] blocks = getChildTag(schematic, "BlockData", ByteArrayTag.class).getValue();
            int i = 0;
            int index = 0;
            while (i < blocks.length) {
                int value = 0;
                int varintLength = 0;
                while (true) {
                    value |= (blocks[i] & 127) << (varintLength++ * 7);
                    if (varintLength > 5) {
                        throw new Exception("Schem file blocks tag data corrupted");
                    }
                    if ((blocks[i] & 128) != 128) {
                        i++;
                        break;
                    }
                    i++;
                }
                BlockData block = NMSHandler.getBlockHelper().getBlockData(palette.get(value));
                int y = index / (width * length);
                int z = (index % (width * length)) / width;
                int x = (index % (width * length)) % width;
                int cbsIndex = z + y * cbs.z_height + x * cbs.z_height * cbs.y_length;
                BlockVector pt = new BlockVector(x, y, z);
                if (tileEntitiesMap.containsKey(pt)) {
                    CompoundTag otag = NMSHandler.getInstance().createCompoundTag(tileEntitiesMap.get(pt));
                    block.setCompoundTag(otag);
                }
                cbs.blocks[cbsIndex] = block;
                index++;
            }
        }
        catch (Exception e) {
            Debug.echoError(e);
        }
        return cbs;
    }

    private static <T extends Tag> T getChildTag(Map<String, Tag> items, String key, Class<T> expected) throws Exception {
        if (!items.containsKey(key)) {
            throw new Exception("Schem file is missing a '" + key + "' tag");
        }
        Tag tag = items.get(key);
        if (!expected.isInstance(tag)) {
            throw new Exception(key + " tag is not of tag type " + expected.getName());
        }
        return expected.cast(tag);
    }

    public static void saveToSpongeStream(CuboidBlockSet blockSet, OutputStream os) {
        // TODO
    }
}
