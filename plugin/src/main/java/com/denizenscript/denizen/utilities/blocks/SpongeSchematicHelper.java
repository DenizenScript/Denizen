package com.denizenscript.denizen.utilities.blocks;

import com.denizenscript.denizen.Denizen;
import com.denizenscript.denizen.nms.NMSHandler;
import com.denizenscript.denizen.nms.interfaces.BlockHelper;
import com.denizenscript.denizen.objects.MaterialTag;
import com.denizenscript.denizencore.objects.core.ListTag;
import com.denizenscript.denizencore.objects.core.MapTag;
import com.denizenscript.denizencore.utilities.CoreUtilities;
import com.denizenscript.denizencore.utilities.debugging.Debug;
import net.kyori.adventure.nbt.*;
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

public class SpongeSchematicHelper {

    public static String stringifyTag(BinaryTag tag) {
        if (tag instanceof StringBinaryTag stringTag) {
            return stringTag.value();
        }
        else if (tag instanceof ByteArrayBinaryTag byteArrayTag) {
            return new String(byteArrayTag.value(), StandardCharsets.UTF_8);
        }
        return tag.toString();
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
            Map.Entry<String, CompoundBinaryTag> rootTag = BinaryTagIO.reader().readNamed(is, BinaryTagIO.Compression.GZIP);
            if (!rootTag.getKey().equals("Schematic")) {
                throw new Exception("Tag 'Schematic' does not exist or is not first!");
            }
            CompoundBinaryTag schematic = rootTag.getValue();
            // TODO: adventure-nbt: compound tag contains
            if (schematic.keySet().contains("DenizenEntities")) {
                String entities = stringifyTag(schematic.get("DenizenEntities"));
                cbs.entities = ListTag.valueOf(entities, CoreUtilities.errorButNoDebugContext);
            }
            short width = getChildTag(schematic, "Width", BinaryTagTypes.SHORT).value();
            short length = getChildTag(schematic, "Length", BinaryTagTypes.SHORT).value();
            short height = getChildTag(schematic, "Height", BinaryTagTypes.SHORT).value();
            int originX = 0;
            int originY = 0;
            int originZ = 0;
            if (schematic.keySet().contains("DenizenOffset")) {
                // Note: "Offset" contains complete nonsense from WE, so just don't touch it.
                int[] offsetArr = getChildTag(schematic, "DenizenOffset", BinaryTagTypes.INT_ARRAY).value();
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
            CompoundBinaryTag paletteTag = getChildTag(schematic, "Palette", BinaryTagTypes.COMPOUND);
            HashMap<Integer, BlockData> palette = new HashMap<>(256);
            List<Map.Entry<Integer, String>> latePairs = isPrimary ? null : new ArrayList<>();
            for (String key : paletteTag.keySet()) {
                int id = getChildTag(paletteTag, key, BinaryTagTypes.INT).value();
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
            Map<BlockVector, CompoundBinaryTag> tileEntitiesMap = new HashMap<>();
            if (schematic.keySet().contains("BlockEntities")) {
                ListBinaryTag tileEntities = getChildTag(schematic, "BlockEntities", BinaryTagTypes.LIST);
                for (BinaryTag tag : tileEntities) {
                    if (!(tag instanceof CompoundBinaryTag compoundTag)) {
                        continue;
                    }
                    int[] pos = getChildTag(compoundTag, "Pos", BinaryTagTypes.INT_ARRAY).value();
                    int x = pos[0];
                    int y = pos[1];
                    int z = pos[2];
                    BlockVector vec = new BlockVector(x, y, z);
                    tileEntitiesMap.put(vec, compoundTag);
                }
            }
            byte[] blocks = getChildTag(schematic, "BlockData", BinaryTagTypes.BYTE_ARRAY).value();
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
                    block.tileEntityData = tileEntitiesMap.get(pt);
                }
                cbs.blocks[cbsIndex] = block;
                index++;
            }
            if (schematic.keySet().contains("DenizenFlags")) {
                CompoundBinaryTag flags = getChildTag(schematic, "DenizenFlags", BinaryTagTypes.COMPOUND);
                for (Map.Entry<String, ? extends BinaryTag> flagData : flags) {
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

    private static <T extends BinaryTag> T getChildTag(CompoundBinaryTag compoundTag, String key, BinaryTagType<T> expected) throws Exception {
        BinaryTag tag = compoundTag.get(key);
        if (tag == null) {
            throw new Exception("Schem file is missing a '" + key + "' tag");
        }
        if (tag.type() != expected) {
            throw new Exception(key + " tag is not of tag type " + expected);
        }
        return (T) tag;
    }

    public static void saveToSpongeStream(CuboidBlockSet blockSet, OutputStream os) {
        try {
            CompoundBinaryTag.Builder schematic = CompoundBinaryTag.builder();
            schematic.putShort("Width", (short) blockSet.x_width);
            schematic.putShort("Length", (short) blockSet.z_height);
            schematic.putShort("Height", (short) blockSet.y_length);
            schematic.putIntArray("DenizenOffset", new int[] {blockSet.center_x, blockSet.center_y, blockSet.center_z});
            if (blockSet.entities != null) {
                schematic.putByteArray("DenizenEntities", blockSet.entities.toString().getBytes(StandardCharsets.UTF_8));
            }
            Map<String, BinaryTag> palette = new HashMap<>();
            ByteArrayOutputStream blocksBuffer = new ByteArrayOutputStream((blockSet.x_width) * (blockSet.y_length) * (blockSet.z_height));
            ListBinaryTag.Builder<CompoundBinaryTag> tileEntities = ListBinaryTag.builder(BinaryTagTypes.COMPOUND);
            int paletteMax = 0;
            for (int y = 0; y < blockSet.y_length; y++) {
                for (int z = 0; z < blockSet.z_height; z++) {
                    for (int x = 0; x < blockSet.x_width; x++) {
                        int cbsIndex = z + y * blockSet.z_height + x * blockSet.z_height * blockSet.y_length;
                        FullBlockData bd = blockSet.blocks[cbsIndex];
                        String dataStr = bd.data.getAsString();
                        BinaryTag blockIdTag = palette.get(dataStr);
                        if (blockIdTag == null) {
                            blockIdTag = IntBinaryTag.intBinaryTag(paletteMax++);
                            palette.put(dataStr, blockIdTag);
                        }
                        int blockId = ((IntBinaryTag) blockIdTag).value();
                        while ((blockId & -128) != 0) {
                            blocksBuffer.write(blockId & 127 | 128);
                            blockId >>>= 7;
                        }
                        blocksBuffer.write(blockId);
                        CompoundBinaryTag rawTag = bd.tileEntityData;
                        if (rawTag != null) {
                            CompoundBinaryTag tileEntityTag = rawTag.putIntArray("Pos", new int[] { x, y, z });
                            tileEntities.add(tileEntityTag);
                        }
                    }
                }
            }
            schematic.putInt("PaletteMax", paletteMax);
            schematic.put("Palette", CompoundBinaryTag.from(palette));
            schematic.putByteArray("BlockData", blocksBuffer.toByteArray());
            schematic.put("BlockEntities", tileEntities.build());
            if (blockSet.hasFlags) {
                Map<String, BinaryTag> flagMap = new HashMap<>();
                for (int i = 0; i < blockSet.blocks.length; i++) {
                    if (blockSet.blocks[i].flags != null) {
                        flagMap.put(String.valueOf(i), ByteArrayBinaryTag.byteArrayBinaryTag(blockSet.blocks[i].flags.toString().getBytes(StandardCharsets.UTF_8)));
                    }
                }
                if (!flagMap.isEmpty()) {
                    schematic.put("DenizenFlags", CompoundBinaryTag.from(flagMap));
                }
            }
            BinaryTagIO.writer().writeNamed(Map.entry("Schematic", schematic.build()), os, BinaryTagIO.Compression.GZIP);
        }
        catch (Exception ex) {
            Debug.echoError(ex);
        }
    }
}
