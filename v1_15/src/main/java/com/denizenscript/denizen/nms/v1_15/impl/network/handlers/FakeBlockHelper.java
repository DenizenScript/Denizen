package com.denizenscript.denizen.nms.v1_15.impl.network.handlers;

import com.denizenscript.denizen.nms.util.ReflectionHelper;
import com.denizenscript.denizen.objects.LocationTag;
import com.denizenscript.denizen.utilities.blocks.FakeBlock;
import com.denizenscript.denizen.utilities.debugging.Debug;
import io.netty.buffer.Unpooled;
import net.minecraft.server.v1_15_R1.*;
import org.bukkit.craftbukkit.v1_15_R1.block.data.CraftBlockData;

import java.lang.reflect.Field;
import java.util.List;
import java.util.ListIterator;

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

    public static int indexInPalette(IBlockData data) {
        return ChunkSection.GLOBAL_PALETTE.a(data);
    }

    public static int blockArrayIndex(int x, int y, int z) {
        return y * (16 * 16) + z * 16 + x;
    }

    public static int getPaletteSubId(int[] palette, int id) {
        for (int i = 0; i < palette.length; i++) {
            if (palette[i] == id) {
                return i;
            }
        }
        return -1;
    }

    public static void handleMapChunkPacket(PacketPlayOutMapChunk packet, List<FakeBlock> blocks) {
        try {
            // TODO: properly update HeightMap?
            int bitmask = BITMASK_MAPCHUNK.getInt(packet);
            byte[] data = (byte[]) DATA_MAPCHUNK.get(packet);
            PacketDataSerializer serial = new PacketDataSerializer(Unpooled.wrappedBuffer(data));
            PacketDataSerializer outputSerial = new PacketDataSerializer(Unpooled.buffer(data.length));
            boolean isFull = packet.f();
            List<NBTTagCompound> blockEntities = (List<NBTTagCompound>) BLOCKENTITIES_MAPCHUNK.get(packet);
            ListIterator<NBTTagCompound> iterator = blockEntities.listIterator();
            while (iterator.hasNext()) {
                NBTTagCompound blockEnt = iterator.next();
                int x = blockEnt.getInt("x");
                int y = blockEnt.getInt("y");
                int z = blockEnt.getInt("z");
                for (FakeBlock block : blocks) {
                    LocationTag loc = block.location;
                    if (loc.getBlockX() == x && loc.getBlockY() == y && loc.getBlockZ() == z) {
                        iterator.remove();
                        break;
                    }
                }
            }
            for (FakeBlock block : blocks) {
                LocationTag loc = block.location;
                NBTTagCompound newCompound = new NBTTagCompound();
                newCompound.setInt("x", loc.getBlockX());
                newCompound.setInt("y", loc.getBlockY());
                newCompound.setInt("z", loc.getBlockZ());
                newCompound.setString("id", block.material.getMaterial().getKey().toString());
                blockEntities.add(newCompound);
            }
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
                    long[] blockListHelper = new long[dataLen];
                    for (int i = 0; i < blockListHelper.length; i++) {
                        blockListHelper[i] = serial.readLong();
                    }
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
                    outputSerial.writeByte(width);
                    outputSerial.d(paletteLen);
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
