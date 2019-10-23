package com.denizenscript.denizen.nms.v1_14.impl.network.handlers;

import com.denizenscript.denizen.nms.util.ReflectionHelper;
import com.denizenscript.denizen.utilities.blocks.FakeBlock;
import com.denizenscript.denizen.utilities.debugging.Debug;
import io.netty.buffer.Unpooled;
import net.minecraft.server.v1_14_R1.*;
import org.bukkit.craftbukkit.v1_14_R1.block.data.CraftBlockData;

import java.lang.reflect.Field;
import java.util.List;

public class FakeBlockHelper {

    public static Field BITMASK_MAPCHUNK = ReflectionHelper.getFields(PacketPlayOutMapChunk.class).get("c");
    public static Field HEIGHTMAPS_MAPCHUNK = ReflectionHelper.getFields(PacketPlayOutMapChunk.class).get("d");
    public static Field DATA_MAPCHUNK = ReflectionHelper.getFields(PacketPlayOutMapChunk.class).get("e");
    public static Field BLOCKENTITIES_MAPCHUNK = ReflectionHelper.getFields(PacketPlayOutMapChunk.class).get("f");

    public static IBlockData getNMSState(FakeBlock block) {
        return ((CraftBlockData) block.material.getModernData().data).getState();
    }

    public static boolean anyBlocksInSection(List<FakeBlock> blocks, int y) {
        int minY = y << 4;
        int maxY = (y << 4) + 16;
        for (FakeBlock block : blocks) {
            int blockY = block.location.getBlockY();
            if (blockY >= minY && blockY < maxY) {
                return true;
            }
        }
        return false;
    }

    public static IBlockData blockInPalette(int paletteId) {
        return ChunkSection.GLOBAL_PALETTE.a(paletteId);
    }

    public static int indexInPalette(IBlockData data) {
        return ChunkSection.GLOBAL_PALETTE.a(data);
    }

    public static int blockArrayIndex(int x, int y, int z) {
        return y * (16 * 16) + z * 16 + x;
    }

    public static int getWideIntAt(byte[] data, int index, int width) {
        int result = 0;
        for (int i = 0; i < width; i++) {
            result |= data[index + i] << i;
        }
        return result;
    }

    public static int getPaletteSubId(int[] palette, int id) {
        for (int i = 0; i < palette.length; i++) {
            if (palette[i] == id) {
                return i;
            }
        }
        return -1;
    }

    public static void writeWideInt(PacketDataSerializer serial, int value, int width) {
        for (int i = 0; i < width; i++) {
            serial.writeByte((value >> i) & 0xFF);
        }
    }

    public static int getWidthFor(int value) {
        if (value < 256) {
            return 1;
        }
        if (value < 256 * 256) {
            return 2;
        }
        if (value < 256 * 256 * 256) {
            return 3;
        }
        return 4;
    }

    public static void handleMapChunkPacket(PacketPlayOutMapChunk packet, List<FakeBlock> blocks) {
        try {
            int bitmask = BITMASK_MAPCHUNK.getInt(packet);
            byte[] data = (byte[]) DATA_MAPCHUNK.get(packet);
            PacketDataSerializer serial = new PacketDataSerializer(Unpooled.wrappedBuffer(data));
            PacketDataSerializer outputSerial = new PacketDataSerializer(Unpooled.buffer(data.length));
            byte[] blockDataHelper = new byte[4 * 16 * 16 * 16]; // 16,384
            int[] blockListHelper = new int[16 * 16 * 16];

            for (int y = 0; y < 16; y++) {
                if ((bitmask & (1 << y)) != 0) {
                    int blockCount = serial.readShort();
                    int width = serial.readUnsignedByte();
                    int paletteLen = serial.i(); // readVarInt
                    int[] palette = new int[paletteLen];
                    for (int p = 0; p < paletteLen; p++) {
                        palette[p] = serial.i();
                    }
                    int dataLen = serial.i();
                    serial.readBytes(blockDataHelper, 0, dataLen * width);
                    if (!anyBlocksInSection(blocks, y)) {
                        outputSerial.writeShort(blockCount);
                        outputSerial.writeByte(width);
                        outputSerial.d(paletteLen); // writeVarInt
                        for (int p = 0; p < paletteLen; p++) {
                            outputSerial.d(palette[p]);
                        }
                        outputSerial.d(dataLen);
                        outputSerial.writeBytes(blockDataHelper, 0, dataLen * width);
                        continue;
                    }
                    for (int i = 0; i < blockListHelper.length; i++) {
                        blockListHelper[i] = getWideIntAt(blockDataHelper, i * width, width);
                    }
                    int minY = y << 4;
                    int maxY = (y << 4) + 16;
                    for (FakeBlock block : blocks) {
                        int blockY = block.location.getBlockY();
                        if (blockY >= minY && blockY < maxY) {
                            int blockX = block.location.getBlockX();
                            int blockZ = block.location.getBlockZ();
                            blockX -= (blockX >> 4) * 16;
                            blockY -= (blockY >> 4) * 16;
                            blockZ -= (blockZ >> 4) * 16;
                            int blockIndex = blockArrayIndex(blockX, blockY, blockZ);
                            IBlockData replacementData = getNMSState(block);
                            int globalPaletteIndex = indexInPalette(replacementData);
                            int subPaletteId = getPaletteSubId(palette, globalPaletteIndex);
                            if (subPaletteId == -1) {
                                int[] newPalette = new int[paletteLen + 1];
                                for (int p = 0; p < paletteLen; p++) {
                                    newPalette[p] = palette[p];
                                }
                                newPalette[paletteLen] = globalPaletteIndex;
                                subPaletteId = paletteLen;
                                paletteLen++;
                                palette = newPalette;
                            }
                            blockListHelper[blockIndex] = subPaletteId;
                        }
                    }
                    width = getWidthFor(paletteLen);
                    for (int i = 0; i < blockListHelper.length; i++) {
                        writeWideInt(outputSerial, blockListHelper[i], width);
                    }
                }
            }
            byte[] outputBytes = outputSerial.array();
            DATA_MAPCHUNK.set(packet, outputBytes);
        }
        catch (Exception ex) {
            Debug.echoError(ex);
        }
    }
}
