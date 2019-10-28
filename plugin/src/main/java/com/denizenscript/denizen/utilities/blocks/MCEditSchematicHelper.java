package com.denizenscript.denizen.utilities.blocks;

import com.denizenscript.denizen.nms.NMSHandler;
import com.denizenscript.denizen.nms.interfaces.BlockData;
import com.denizenscript.denizen.nms.util.jnbt.*;
import com.denizenscript.denizen.objects.MaterialTag;
import com.denizenscript.denizen.utilities.debugging.Debug;
import org.bukkit.util.BlockVector;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class MCEditSchematicHelper {

    public static CuboidBlockSet fromMCEditStream(InputStream is) {
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
            short width = (getChildTag(schematic, "Width", ShortTag.class).getValue());
            short length = (getChildTag(schematic, "Length", ShortTag.class).getValue());
            short height = (getChildTag(schematic, "Height", ShortTag.class).getValue());
            int originX = 0;
            int originY = 0;
            int originZ = 0;
            try {
                originX = getChildTag(schematic, "DenizenOriginX", IntTag.class).getValue();
                originY = getChildTag(schematic, "DenizenOriginY", IntTag.class).getValue();
                originZ = getChildTag(schematic, "DenizenOriginZ", IntTag.class).getValue();
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
            // Disregard Offset
            String materials = getChildTag(schematic, "Materials", StringTag.class).getValue();
            if (!materials.equals("Alpha")) {
                throw new Exception("Schematic file is not an Alpha schematic!");
            }
            byte[] blockId = getChildTag(schematic, "Blocks", ByteArrayTag.class).getValue();
            byte[] blockData = getChildTag(schematic, "Data", ByteArrayTag.class).getValue();
            byte[] addId = new byte[0];
            short[] blocks = new short[blockId.length];
            if (schematic.containsKey("AddBlocks")) {
                addId = getChildTag(schematic, "AddBlocks", ByteArrayTag.class).getValue();
            }
            for (int index = 0; index < blockId.length; index++) {
                if ((index >> 1) >= addId.length) {
                    blocks[index] = (short) (blockId[index] & 0xFF);
                }
                else {
                    if ((index & 1) == 0) {
                        blocks[index] = (short) (((addId[index >> 1] & 0x0F) << 8) + (blockId[index] & 0xFF));
                    }
                    else {
                        blocks[index] = (short) (((addId[index >> 1] & 0xF0) << 4) + (blockId[index] & 0xFF));
                    }
                }
            }
            List<Tag> tileEntities = getChildTag(schematic, "TileEntities", JNBTListTag.class).getValue();
            Map<BlockVector, Map<String, Tag>> tileEntitiesMap = new HashMap<>();
            for (Tag tag : tileEntities) {
                if (!(tag instanceof CompoundTag)) {
                    continue;
                }
                CompoundTag t = (CompoundTag) tag;
                int x = 0;
                int y = 0;
                int z = 0;
                Map<String, Tag> values = new HashMap<>();
                for (Map.Entry<String, Tag> entry : t.getValue().entrySet()) {
                    if (entry.getKey().equals("x")) {
                        if (entry.getValue() instanceof IntTag) {
                            x = ((IntTag) entry.getValue()).getValue();
                        }
                    }
                    else if (entry.getKey().equals("y")) {
                        if (entry.getValue() instanceof IntTag) {
                            y = ((IntTag) entry.getValue()).getValue();
                        }
                    }
                    else if (entry.getKey().equals("z")) {
                        if (entry.getValue() instanceof IntTag) {
                            z = ((IntTag) entry.getValue()).getValue();
                        }
                    }
                    values.put(entry.getKey(), entry.getValue());
                }
                BlockVector vec = new BlockVector(x, y, z);
                tileEntitiesMap.put(vec, values);
            }
            int finalIndex = 0;
            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    for (int z = 0; z < length; z++) {
                        int index = y * width * length + z * width + x;
                        BlockVector pt = new BlockVector(x, y, z);
                        MaterialTag dMat = OldMaterialsHelper.getMaterialFrom(OldMaterialsHelper.getLegacyMaterial(blocks[index]), blockData[index]);
                        BlockData block = dMat.getNmsBlockData();
                        if (tileEntitiesMap.containsKey(pt)) {
                            CompoundTag otag = NMSHandler.getInstance().createCompoundTag(tileEntitiesMap.get(pt));
                            block.setCompoundTag(otag);
                        }
                        cbs.blocks[finalIndex++] = block;
                    }
                }
            }
        }
        catch (Exception e) {
            Debug.echoError(e);
        }
        return cbs;
    }

    private static <T extends Tag> T getChildTag(Map<String, Tag> items, String key, Class<T> expected) throws Exception {
        if (!items.containsKey(key)) {
            throw new Exception("Schematic file is missing a '" + key + "' tag");
        }
        Tag tag = items.get(key);
        if (!expected.isInstance(tag)) {
            throw new Exception(key + " tag is not of tag type " + expected.getName());
        }
        return expected.cast(tag);
    }

    // Thanks to WorldEdit for sample code
    public static void saveMCEditFormatToStream(CuboidBlockSet blockSet, OutputStream os) {
        try {
            HashMap<String, Tag> schematic = new HashMap<>();
            schematic.put("Width", new ShortTag((short) (blockSet.x_width)));
            schematic.put("Length", new ShortTag((short) (blockSet.z_height)));
            schematic.put("Height", new ShortTag((short) (blockSet.y_length)));
            schematic.put("Materials", new StringTag("Alpha"));
            schematic.put("DenizenOriginX", new IntTag(blockSet.center_x));
            schematic.put("DenizenOriginY", new IntTag(blockSet.center_y));
            schematic.put("DenizenOriginZ", new IntTag(blockSet.center_z));
            schematic.put("WEOriginX", new IntTag(blockSet.center_x));
            schematic.put("WEOriginY", new IntTag(blockSet.center_y));
            schematic.put("WEOriginZ", new IntTag(blockSet.center_z));
            schematic.put("WEOffsetX", new IntTag(0));
            schematic.put("WEOffsetY", new IntTag(0));
            schematic.put("WEOffsetZ", new IntTag(0));
            byte[] blocks = new byte[((blockSet.x_width) * (blockSet.y_length) * (blockSet.z_height))];
            byte[] addBlocks = null;
            byte[] blockData = new byte[blocks.length];
            ArrayList<Tag> tileEntities = new ArrayList<>();
            int indexer = 0;
            for (int x = 0; x < blockSet.x_width; x++) {
                for (int y = 0; y < blockSet.y_length; y++) {
                    for (int z = 0; z < blockSet.z_height; z++) {
                        int index = (y * (blockSet.x_width) * (blockSet.z_height) + z * (blockSet.x_width) + x);
                        BlockData bd = blockSet.blocks[indexer];//blockAt(x, y, z);
                        indexer++;
                        int matId = NMSHandler.getBlockHelper().idFor(bd.getMaterial());
                        if (matId > 255) {
                            if (addBlocks == null) {
                                addBlocks = new byte[(blocks.length >> 1) + 1];
                            }
                            addBlocks[index >> 1] = (byte) (((index & 1) == 0) ?
                                    addBlocks[index >> 1] & 0xF0 | (matId >> 8) & 0xF
                                    : addBlocks[index >> 1] & 0xF | ((matId >> 8) & 0xF) << 4);
                        }
                        blocks[index] = (byte) matId;
                        blockData[index] = bd.getData();

                        CompoundTag rawTag = bd.getCompoundTag();
                        if (rawTag != null) {
                            HashMap<String, Tag> values = new HashMap<>();
                            for (Map.Entry<String, Tag> entry : rawTag.getValue().entrySet()) {
                                values.put(entry.getKey(), entry.getValue());
                            }
                            values.put("x", new IntTag(x));
                            values.put("y", new IntTag(y));
                            values.put("z", new IntTag(z));
                            CompoundTag tileEntityTag = NMSHandler.getInstance().createCompoundTag(values);
                            tileEntities.add(tileEntityTag);
                        }
                    }
                }
            }
            schematic.put("Blocks", new ByteArrayTag(blocks));
            schematic.put("Data", new ByteArrayTag(blockData));
            schematic.put("Entities", new JNBTListTag(CompoundTag.class, new ArrayList<>()));
            schematic.put("TileEntities", new JNBTListTag(CompoundTag.class, tileEntities));
            if (addBlocks != null) {
                schematic.put("AddBlocks", new ByteArrayTag(addBlocks));
            }
            CompoundTag schematicTag = NMSHandler.getInstance().createCompoundTag(schematic);
            NBTOutputStream stream = new NBTOutputStream(new GZIPOutputStream(os));
            stream.writeNamedTag("Schematic", schematicTag);
            os.flush();
            stream.close();
        }
        catch (Exception e) {
            Debug.echoError(e);
        }
    }
}
