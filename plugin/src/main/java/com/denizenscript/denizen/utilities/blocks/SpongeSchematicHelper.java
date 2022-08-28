package com.denizenscript.denizen.utilities.blocks;

import com.denizenscript.denizen.Denizen;
import com.denizenscript.denizen.nms.NMSHandler;
import com.denizenscript.denizen.nms.interfaces.BlockHelper;
import com.denizenscript.denizen.nms.util.jnbt.*;
import com.denizenscript.denizen.objects.MaterialTag;
import com.denizenscript.denizencore.utilities.debugging.Debug;
import com.denizenscript.denizencore.objects.core.ListTag;
import com.denizenscript.denizencore.objects.core.MapTag;
import com.denizenscript.denizencore.utilities.CoreUtilities;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.data.BlockData;
import org.bukkit.util.BlockVector;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class SpongeSchematicHelper {

    public static String stringifyTag(Tag t) {
        if (t instanceof StringTag) {
            return ((StringTag) t).getValue();
        }
        else if (t instanceof ByteArrayTag) {
            return new String(((ByteArrayTag) t).getValue(), StandardCharsets.UTF_8);
        }
        return t.toString();
    }

    public static ConcurrentHashMap<String, BlockData> blockDataCache = new ConcurrentHashMap<>();

    public static BlockData unstableParseMaterial(String key) {
        BlockData data;
        try {
            data = NMSHandler.blockHelper.parseBlockData(key);
        }
        catch (Exception ex) {
            Debug.echoError(ex);
            MaterialTag material = MaterialTag.valueOf(BlockHelper.getMaterialNameFromBlockData(key), CoreUtilities.noDebugContext);
            data = (material == null ? new MaterialTag(Material.AIR) : material).getModernData();
        }
        return data;
    }

    public static class BoolHolder {
        public boolean bool;
    }

    // Referenced from WorldEdit source and Sponge schematic format v2 documentation
    // Some values are custom and specific to Denizen
    public static CuboidBlockSet fromSpongeStream(InputStream is) {
        boolean isPrimary = Bukkit.isPrimaryThread();
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
            if (schematic.containsKey("DenizenEntities")) {
                String entities = stringifyTag(schematic.get("DenizenEntities"));
                cbs.entities = ListTag.valueOf(entities, CoreUtilities.errorButNoDebugContext);
            }
            short width = getChildTag(schematic, "Width", ShortTag.class).getValue();
            short length = getChildTag(schematic, "Length", ShortTag.class).getValue();
            short height = getChildTag(schematic, "Height", ShortTag.class).getValue();
            int originX = 0;
            int originY = 0;
            int originZ = 0;
            if (schematic.containsKey("DenizenOffset")) {
                // Note: "Offset" contains complete nonsense from WE, so just don't touch it.
                int[] offsetArr = getChildTag(schematic, "DenizenOffset", IntArrayTag.class).getValue();
                originX = offsetArr[0];
                originY = offsetArr[1];
                originZ = offsetArr[2];
            }
            cbs.x_width = width;
            cbs.z_height = length;
            cbs.y_length = height;
            cbs.center_x = originX;
            cbs.center_y = originY;
            cbs.center_z = originZ;
            cbs.blocks = new FullBlockData[width * length * height];
            Map<String, Tag> paletteMap = getChildTag(schematic, "Palette", CompoundTag.class).getValue();
            HashMap<Integer, BlockData> palette = new HashMap<>(256);
            List<Map.Entry<Integer, String>> latePairs = isPrimary ? null : new ArrayList<>();
            for (String key : paletteMap.keySet()) {
                int id = getChildTag(paletteMap, key, IntTag.class).getValue();
                if (isPrimary) {
                    palette.put(id, blockDataCache.computeIfAbsent(key, SpongeSchematicHelper::unstableParseMaterial));
                }
                else {
                    BlockData entry = blockDataCache.get(key);
                    if (entry != null) {
                        palette.put(id, entry);
                    }
                    else {
                        latePairs.add(new AbstractMap.SimpleEntry<>(id, key));
                    }
                }
            }
            if (!isPrimary && !latePairs.isEmpty()) {
                BoolHolder bool = new BoolHolder();
                Bukkit.getScheduler().runTask(Denizen.getInstance(), () -> {
                    for (Map.Entry<Integer, String> pair : latePairs) {
                        palette.put(pair.getKey(), blockDataCache.computeIfAbsent(pair.getValue(), SpongeSchematicHelper::unstableParseMaterial));
                    }
                    bool.bool = true;
                });
                for (int i = 0; i < 1000; i++) {
                    Thread.sleep(50);
                    if (bool.bool) {
                        break;
                    }
                }
            }
            Map<BlockVector, Map<String, Tag>> tileEntitiesMap = new HashMap<>();
            if (schematic.containsKey("BlockEntities")) {
                List<Tag> tileEntities = getChildTag(schematic, "BlockEntities", JNBTListTag.class).getValue();
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
                FullBlockData block = new FullBlockData(palette.get(value));
                int y = index / (width * length);
                int z = (index % (width * length)) / width;
                int x = (index % (width * length)) % width;
                int cbsIndex = z + y * cbs.z_height + x * cbs.z_height * cbs.y_length;
                BlockVector pt = new BlockVector(x, y, z);
                if (tileEntitiesMap.containsKey(pt)) {
                    block.tileEntityData = NMSHandler.instance.createCompoundTag(tileEntitiesMap.get(pt));
                }
                cbs.blocks[cbsIndex] = block;
                index++;
            }
            if (schematic.containsKey("DenizenFlags")) {
                Map<String, Tag> flags = getChildTag(schematic, "DenizenFlags", CompoundTag.class).getValue();
                for (Map.Entry<String, Tag> flagData : flags.entrySet()) {
                    int flagIndex = Integer.parseInt(flagData.getKey());
                    cbs.blocks[flagIndex].flags = MapTag.valueOf(stringifyTag(flagData.getValue()), CoreUtilities.noDebugContext);
                }
            }
        }
        catch (Exception e) {
            throw new RuntimeException("Failed to load Sponge-format schematic file", e);
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
        try {
            HashMap<String, Tag> schematic = new HashMap<>();
            schematic.put("Width", new ShortTag((short) (blockSet.x_width)));
            schematic.put("Length", new ShortTag((short) (blockSet.z_height)));
            schematic.put("Height", new ShortTag((short) (blockSet.y_length)));
            schematic.put("DenizenOffset", new IntArrayTag(new int[] {blockSet.center_x, blockSet.center_y, blockSet.center_z}));
            if (blockSet.entities != null) {
                schematic.put("DenizenEntities", new ByteArrayTag(blockSet.entities.toString().getBytes(StandardCharsets.UTF_8)));
            }
            Map<String, Tag> palette = new HashMap<>();
            ByteArrayOutputStream blocksBuffer = new ByteArrayOutputStream((blockSet.x_width) * (blockSet.y_length) * (blockSet.z_height));
            ArrayList<Tag> tileEntities = new ArrayList<>();
            int paletteMax = 0;
            for (int y = 0; y < blockSet.y_length; y++) {
                for (int z = 0; z < blockSet.z_height; z++) {
                    for (int x = 0; x < blockSet.x_width; x++) {
                        int cbsIndex = z + y * blockSet.z_height + x * blockSet.z_height * blockSet.y_length;
                        FullBlockData bd = blockSet.blocks[cbsIndex];
                        String dataStr = bd.data.getAsString();
                        Tag blockIdTag = palette.get(dataStr);
                        if (blockIdTag == null) {
                            blockIdTag = new IntTag(paletteMax++);
                            palette.put(dataStr, blockIdTag);
                        }
                        int blockId = ((IntTag) blockIdTag).getValue();
                        while ((blockId & -128) != 0) {
                            blocksBuffer.write(blockId & 127 | 128);
                            blockId >>>= 7;
                        }
                        blocksBuffer.write(blockId);
                        CompoundTag rawTag = bd.tileEntityData;
                        if (rawTag != null) {
                            HashMap<String, Tag> values = new HashMap<>(rawTag.getValue());
                            values.put("Pos", new IntArrayTag(new int[] { x, y, z }));
                            CompoundTag tileEntityTag = NMSHandler.instance.createCompoundTag(values);
                            tileEntities.add(tileEntityTag);
                        }
                    }
                }
            }
            schematic.put("PaletteMax", new IntTag(paletteMax));
            schematic.put("Palette", NMSHandler.instance.createCompoundTag(palette));
            schematic.put("BlockData", new ByteArrayTag(blocksBuffer.toByteArray()));
            schematic.put("BlockEntities", new JNBTListTag(CompoundTag.class, tileEntities));
            if (blockSet.hasFlags) {
                Map<String, Tag> flagMap = new HashMap<>();
                for (int i = 0; i < blockSet.blocks.length; i++) {
                    if (blockSet.blocks[i].flags != null) {
                        flagMap.put(String.valueOf(i), new ByteArrayTag(blockSet.blocks[i].flags.toString().getBytes(StandardCharsets.UTF_8)));
                    }
                }
                if (!flagMap.isEmpty()) {
                    schematic.put("DenizenFlags", NMSHandler.instance.createCompoundTag(flagMap));
                }
            }
            CompoundTag schematicTag = NMSHandler.instance.createCompoundTag(schematic);
            NBTOutputStream stream = new NBTOutputStream(new GZIPOutputStream(os));
            stream.writeNamedTag("Schematic", schematicTag);
            os.flush();
            stream.close();
        }
        catch (Exception ex) {
            Debug.echoError(ex);
        }
    }
}
