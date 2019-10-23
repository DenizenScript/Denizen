package com.denizenscript.denizen.nms.v1_14.impl.network.handlers;

import com.denizenscript.denizen.nms.util.ReflectionHelper;
import com.denizenscript.denizen.utilities.blocks.FakeBlock;
import com.denizenscript.denizen.utilities.debugging.Debug;
import io.netty.buffer.Unpooled;
import net.minecraft.server.v1_14_R1.*;
import org.bukkit.craftbukkit.v1_14_R1.block.data.CraftBlockData;

import java.lang.reflect.Field;
import java.util.Arrays;
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

    public static void pushByteArrayToLongArrayBE(byte[] bits, long[] longs) {
        for (int i = 0; i < bits.length; i++) {
            int longIndex = i >> 3;
            int startBit = longIndex * 8;
            if (longIndex >= longs.length) {
                break;
            }
            longs[longIndex] |= ((long) bits[i]) << (7 - (i - startBit));
        }
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

    public static void handleMapChunkPacket(PacketPlayOutMapChunk packet, List<FakeBlock> blocks) {
        try {
            // TODO: properly update HeightMap?
            int bitmask = BITMASK_MAPCHUNK.getInt(packet);
            byte[] data = (byte[]) DATA_MAPCHUNK.get(packet);
            PacketDataSerializer serial = new PacketDataSerializer(Unpooled.wrappedBuffer(data));
            PacketDataSerializer outputSerial = new PacketDataSerializer(Unpooled.buffer(data.length));
            boolean isFull = packet.f();
            // TODO: Handle blockEntities?
            //List<NBTTagCompound> blockEntities = (List<NBTTagCompound>) BLOCKENTITIES_MAPCHUNK.get(packet);
            //NBTTagList blockEntitiesList = new NBTTagList();
            //blockEntitiesList.addAll(blockEntities);
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
                    Debug.log("y: " + y + " count: " + blockCount + ", width: " + width + ", paletteLen: " + paletteLen + ", palette: " + Arrays.toString(palette) + ", dataLen: " + dataLen);
                    long[] blockListHelper = new long[dataLen];
                    for (int i = 0; i < blockListHelper.length; i++) {
                        blockListHelper[i] = serial.readLong();
                    }
                    Debug.log("Data: " + Arrays.toString(blockListHelper));
                    outputSerial.writeShort(blockCount);
                    if (!anyBlocksInSection(blocks, y)) {
                        outputSerial.writeByte(width);
                        outputSerial.d(paletteLen); // writeVarInt
                        for (int p = 0; p < paletteLen; p++) {
                            outputSerial.d(palette[p]);
                        }
                        outputSerial.a(blockListHelper); // writeLongs
                        continue;
                    }
                    DataBits bits = new DataBits(width, 4096, blockListHelper);
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
                                int newWdith = MathHelper.d(paletteLen);
                                if (newWdith > width) {
                                    DataBits newBits = new DataBits(newWdith, 4096);
                                    for (int i = 0; i < bits.b(); i++) {
                                        newBits.a(i, bits.a(i));
                                    }
                                    bits = newBits;
                                    width = newWdith;
                                }
                            }
                            bits.a(blockIndex, subPaletteId);
                        }
                    }
                    int[] testOut = new int[bits.b()];
                    for (int i = 0; i < testOut.length; i++) {
                        testOut[i] = bits.a(i);
                    }
                    Debug.log("Blocks: " + Arrays.toString(testOut));
                    outputSerial.writeByte(width);
                    outputSerial.d(paletteLen);
                    Debug.log("Palette: " + Arrays.toString(palette) + ", endWidth: " + width);
                    for (int p = 0; p < palette.length; p++) {
                        outputSerial.d(palette[p]);
                    }
                    outputSerial.a(bits.a());
                }
            }
            if (isFull) {
                // biomes
                outputSerial.writeBytes(serial, 256 * 4);
            }
            byte[] outputBytes = outputSerial.array();
            DATA_MAPCHUNK.set(packet, outputBytes);
        }
        catch (Exception ex) {
            Debug.echoError(ex);
        }
    }
}
